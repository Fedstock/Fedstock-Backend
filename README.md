# Fedstock Backend

## Overview

Fedstock 서비스에서 백엔드는 일반적인 상품/재고 CRUD 서버가 아니라, 인증된 클라이언트 요청을 검증하고 AI 레포로 전달하는 API 게이트웨이 역할을 담당합니다.

주요 역할은 다음과 같습니다.

- JWT 기반 회원가입, 로그인, 사용자 인증 처리
- 클라이언트별 AI 요청의 `clientId`, `scope`, 필수 데이터 검증
- `single_client` 요청은 AI 서버로 즉시 프록시 전달
- `all_clients` 요청은 PostgreSQL 큐에 저장한 뒤, 같은 round의 전체 클라이언트 요청이 모이면 AI batch API로 전달
- AI가 생성한 FL 모델 다운로드 응답을 인증된 클라이언트에게 프록시
- 운영 확인용 S3 artifact bucket 조회 및 전체 삭제 API 제공

즉, 현재 백엔드는 단순 CRUD보다 **인증, 검증, 큐잉, 배치 전송, AI 연동**에 초점을 둔 서버입니다.

## Tech Stack & Role & Architecture

| Category | Content |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Database | PostgreSQL |
| ORM | Spring Data JPA, Hibernate |
| Auth | Spring Security, JWT |
| AI Integration | Spring Web multipart/json proxy |
| Storage Admin | AWS S3 SDK |
| Container | Docker, Docker Compose |
| Build | Gradle Wrapper |
| 담당자 | 안재현, backend repository 100% |

Architecture:

```text
Modular Monolith + Clean Architecture Lite

client
  -> api
  -> application
  -> infrastructure
  -> external AI backend / PostgreSQL / S3
```

패키지는 기능 단위로 나누고, 각 기능 안에서 controller, application service, persistence/client 구현을 분리합니다.

```text
src/main/java/com/fedstock/backend
├── admin/s3        # S3 artifact bucket admin API
├── ai              # AI proxy, queue, batch forwarding
├── auth            # JWT auth, user API, security
├── main            # health, config, global error handling
└── v1/auth         # logout compatibility endpoint
```

## Environment Variables

| Name | Role |
| --- | --- |
| `DB_HOST` | PostgreSQL host |
| `DB_PORT` | PostgreSQL port |
| `DB_NAME` | PostgreSQL database name |
| `DB_USERNAME` | PostgreSQL user |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | JWT signing secret, required in `prod` |
| `AI_BACKEND_URL` | Downstream AI backend base URL |
| `ARTIFACT_BUCKET` | S3 artifact bucket name |
| `S3_ADMIN_PASSWORD` | Swagger-callable S3 admin API password |
| `AWS_REGION` | AWS S3 region |

Local defaults are defined in `.env.example` and `src/main/resources/application.yml`.

## Run, Command

Run with Docker Compose:

```bash
./run.sh up
```

Run in background:

```bash
./run.sh up-bg
```

Stop containers:

```bash
./run.sh down
```

Reset PostgreSQL volume and re-run init SQL:

```bash
./run.sh clean
./run.sh up
```

Run locally with JDK 21:

```bash
./gradlew bootRun
```

Run PostgreSQL only, then start the app locally:

```bash
./run.sh db
./run.sh app
```

Health check:

```bash
curl http://localhost:8080/health
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## API

현재 API는 일반 CRUD가 아니라 인증, AI 프록시, 큐 기반 batch 전송, S3 운영 확인에 맞춰져 있습니다.

| Method | Path | Auth | Role |
| --- | --- | --- | --- |
| `GET` | `/health` | Public | 서버 상태 확인 |
| `POST` | `/api/auth/signup` | Public | 사용자 등록 |
| `POST` | `/api/auth/login` | Public | JWT 발급 |
| `GET` | `/api/users/me` | Bearer token | 현재 사용자 확인 |
| `POST` | `/api/auth/logout` | Bearer token | 로그아웃 호환 API |
| `POST` | `/api/ai/clients/cluster-assignment` | Bearer token | 클러스터 배정 요청 검증 및 AI 전달 |
| `POST` | `/api/ai/clients/{clientId}/fl-model` | Bearer token | 클라이언트 FL 모델 업로드 검증 및 AI 전달 |
| `GET` | `/api/ai/clients/{clientId}/fl-model` | Bearer token | AI가 생성한 할당 FL 모델 다운로드 프록시 |
| `POST` | `/api/admin/s3/objects` | Bearer token + password body | S3 artifact bucket 객체 목록 조회 |
| `POST` | `/api/admin/s3/objects/delete-all` | Bearer token + password body | S3 object version, delete marker 포함 전체 삭제 |

## AI Proxy Flow

### `single_client`

`single_client`는 클라이언트 하나의 요청을 검증한 뒤 AI 서버로 즉시 전달합니다.

```text
client
  -> Spring Backend
  -> validate JWT, clientId, scope, payload
  -> AI Backend
```

사용되는 downstream AI API:

```text
POST {AI_BACKEND_URL}/clients/cluster-assignment
POST {AI_BACKEND_URL}/clients/{clientId}/fl-model
GET  {AI_BACKEND_URL}/clients/{clientId}/fl-model
```

### `all_clients`

`all_clients`는 바로 AI로 보내지 않고 PostgreSQL에 큐로 저장합니다.
같은 `roundId` 또는 `round_id`에 대해 등록된 전체 클라이언트 요청이 모이면 Spring이 AI batch API를 한 번 호출합니다.

```text
client A -> Spring -> DB queue
client B -> Spring -> DB queue
client C -> Spring -> DB queue
                    -> all clients ready
                    -> AI batch API
```

사용되는 downstream AI batch API:

```text
POST {AI_BACKEND_URL}/clients/cluster-assignment/batch
POST {AI_BACKEND_URL}/clients/fl-model/batch
```

## 개발 특징 -> DB Queue Batch

이 레포의 핵심 구현은 CRUD가 아니라 DB queue 기반 batch 제어입니다.

| Feature | Implementation |
| --- | --- |
| 클러스터 배정 all_clients | `ai_cluster_assignment_queue`에 client별 payload 저장 |
| FL 모델 all_clients | `ai_fl_model_queue`에 `.pt` multipart 파일을 byte 배열로 저장 |
| Round 상태 | `ai_sync_rounds`에서 round별 기대 client 수, 상태, 에러, 전달 시점 관리 |
| 중복 제출 | 같은 round/client 요청은 최신 payload로 갱신 |
| 대기 응답 | 전체 client가 모이기 전에는 `202 Accepted`와 queue 상태 반환 |
| batch 전송 | 전체 client가 모이면 Spring이 AI batch endpoint로 한 번에 전달 |

주요 테이블:

```text
users
ai_sync_rounds
ai_cluster_assignment_queue
ai_cluster_assignment_feature_names
ai_cluster_assignment_feature_importance
ai_fl_model_queue
```

## S3 Artifact Admin

S3 API는 서비스 CRUD가 아니라 운영 확인용 admin endpoint입니다.
Swagger에서 JWT를 넣고 `pw` body를 함께 보내면 `ARTIFACT_BUCKET` 상태를 확인하거나 비울 수 있습니다.

```json
{
  "pw": "22"
}
```

지원 기능:

- S3 object 목록, 개수, 총 byte 조회
- versioned bucket의 object version 삭제
- delete marker 삭제

## Commit Convention

커밋 메시지는 영어 한 줄로 작성하고, scope 괄호는 사용하지 않습니다.

```text
feat: add ai proxy queue
fix: handle assigned fl model download
docs: update backend readme
```
