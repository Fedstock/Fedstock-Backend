# Fedstock Backend

Fedstock 서비스의 백엔드 API 서버입니다.
현재 단계는 JWT 인증 후 AI 레포로 요청을 검증/전달하는 프록시 API와 최소 사용자 API만 유지합니다.

## Tech Stack

| Category | Stack |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Build | Gradle Wrapper |
| API | Spring Web |
| Validation | Spring Validation |
| Database | PostgreSQL |
| ORM | Spring Data JPA, Hibernate |
| Security | Spring Security, JWT |
| Container | Docker, Docker Compose |
| Test | JUnit 5, Spring Boot Test |

## Architecture

이 프로젝트는 **Modular Monolith + Clean Architecture Lite** 구조를 사용합니다.

```text
client
  -> api
  -> application
  -> domain
  <- infrastructure
```

기능별 패키지를 하나의 작은 모듈처럼 관리하고, 각 기능 내부에서 요청 처리, 유스케이스, 도메인 규칙, 기술 구현을 분리합니다.
자세한 폴더링과 코드 작성 규칙은 [docs/CONVENTION.md](docs/CONVENTION.md)를 확인합니다.

현재 API는 JWT 기반 인증이 적용되어 있으며, 로그인/회원가입을 제외한 실제 비즈니스 API는 Bearer token이 필요합니다.

## Project Structure

```text
db
└── init
    └── 001_mvp_schema.sql
src/main/java/com/fedstock/backend
├── ai
│   ├── api
│   │   └── dto
│   ├── application
│   └── infrastructure
├── auth
│   ├── api
│   │   └── dto
│   ├── application
│   └── infrastructure
├── main
│   ├── api
│   ├── config
│   └── error
└── v1
    └── auth
        └── api
```

## Database Schema

PostgreSQL 초기 스키마와 테스트 데이터는 `db/init/001_mvp_schema.sql`에 있습니다.
Docker Compose의 PostgreSQL 컨테이너는 이 폴더를 `/docker-entrypoint-initdb.d`로 마운트합니다.

주의: PostgreSQL 공식 이미지의 init SQL은 DB 볼륨이 처음 만들어질 때만 실행됩니다.
이미 `postgres-data` 볼륨이 있는 상태에서 스키마를 다시 먹이려면 아래처럼 볼륨까지 삭제한 뒤 다시 실행합니다.

```bash
./run.sh clean
./run.sh up
```

현재 MVP 테이블:

```text
users
```

샘플 계정:

```text
email: owner@example.com
storeId: CA_1_FOODS_3
password: password12
```

## Environment

로컬 실행 기본값은 `.env.example`과 같습니다.

```text
DB_HOST=localhost
DB_PORT=5432
DB_NAME=fedstock
DB_USERNAME=fedstock
DB_PASSWORD=fedstock
AI_BACKEND_URL=http://localhost:8000
```

Spring profile:

| Profile | Purpose |
| --- | --- |
| `local` | 로컬 개발 |
| `docker` | Docker Compose 실행 |
| `prod` | ECS/RDS 운영 실행 |

운영 ECS 환경은 아래 값을 컨테이너 환경변수 또는 secret으로 주입합니다.

```text
SPRING_PROFILES_ACTIVE=prod
AWS_REGION=ap-northeast-2
ARTIFACT_BUCKET=<artifact-bucket>
DB_HOST=<rds-endpoint>
DB_PORT=5432
DB_NAME=app
DB_USERNAME=app
DB_PASSWORD=<secret>
AI_BACKEND_URL=http://<alb-dns>/ai
```

## Run with Docker

```bash
./run.sh up
```

백그라운드 실행:

```bash
./run.sh up-bg
```

종료:

```bash
./run.sh down
```

PostgreSQL 데이터까지 삭제:

```bash
./run.sh clean
```

## Run Locally

JDK 21과 PostgreSQL이 필요합니다.

```bash
./gradlew bootRun
```

Docker Compose로 DB만 먼저 실행하고 애플리케이션은 로컬에서 실행할 수도 있습니다.

```bash
./run.sh db
./run.sh app
```

## Health Check

```bash
curl http://localhost:8080/health
```

## API Docs

서버 실행 후 Swagger UI에서 현재 API를 확인합니다.

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## API

현재 공개/보호 API는 아래 7개만 유지합니다.

```text
1. GET  /health
2. POST /api/auth/signup
3. POST /api/auth/login
4. GET  /api/users/me
5. POST /api/auth/logout
6. POST /api/ai/clients/cluster-assignment
7. POST /api/ai/clients/{clientId}/fl-model
```

`/api/auth/signup`, `/api/auth/login`은 인증 없이 호출합니다.
나머지 보호 API는 `Authorization: Bearer <accessToken>` 헤더가 필요합니다.

### 회원가입

```http
POST /api/auth/signup
Content-Type: application/json
```

```json
{
  "email": "junu120707@gachon.ac.kr",
  "storeId": "junu120707@gachon.ac.kr",
  "username": "junu120707@gachon.ac.kr",
  "name": "CA_1_FOODS_3",
  "password": "password123"
}
```

### 로그인

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "junu120707@gachon.ac.kr",
  "storeId": "junu120707@gachon.ac.kr",
  "username": "junu120707@gachon.ac.kr",
  "password": "password123"
}
```

### 내 정보

```http
GET /api/users/me
Authorization: Bearer <accessToken>
```

### 로그아웃

```http
POST /api/auth/logout
Authorization: Bearer <accessToken>
```

### 초기 클러스터링 배정

```http
POST /api/ai/clients/cluster-assignment
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "scope": "single_client",
  "roundId": "initial-clustering-20260613-001",
  "clientId": "CA1_Foods_3",
  "sampleCount": 13004,
  "featureNames": ["rolling_mean_28", "rolling_mean_7", "lag_7"],
  "featureImportance": [0.1421, 0.1318, 0.0974],
  "expectedClientCount": null
}
```

Spring은 토큰을 확인하고, `scope`, 필수값, feature vector 길이, 등록된 `clientId`를 검증합니다.
`single_client`는 AI 레포의 `/clients/cluster-assignment`로 즉시 전달합니다.
`all_clients`는 DB 큐에 저장하고 전체 등록 유저 수만큼 같은 `roundId`가 모이면 AI 레포의 `/clients/cluster-assignment/batch`로 한 번에 전달합니다.

### 할당 클러스터 FL 모델 다운로드

```http
POST /api/ai/clients/{clientId}/fl-model
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data
```

```text
client_id=CA1_Foods_3
scope=single_client
round_id=fl-sync-20260613-001
sample_weight=13004
model_file=@client_CA1_Foods_3.pt
```

Spring은 토큰을 확인하고, path/body `client_id` 일치, 등록된 `client_id`, `.pt` 파일 형식, 필수 multipart 필드를 검증합니다.
`single_client`는 AI 레포의 `/clients/{clientId}/fl-model`로 즉시 전달합니다.
`all_clients`는 DB 큐에 저장하고 전체 등록 유저 수만큼 같은 `round_id`가 모이면 AI 레포의 `/clients/fl-model/batch`로 한 번에 전달합니다.

상세 데모 명세는 [`docs/ai-proxy-api-spec.md`](/Users/kento/Desktop/Project/fedstock/Fedstock-Backend/docs/ai-proxy-api-spec.md)를 봅니다.

## Commit Convention

커밋 메시지는 영어 한 줄로 간결하게 작성합니다.
scope 괄호는 사용하지 않습니다.

```text
feat: add demo crud api
fix: handle demo not found
docs: update backend readme
```
