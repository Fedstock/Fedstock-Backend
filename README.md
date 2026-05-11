# Fedstock Backend

Fedstock 서비스의 백엔드 API 서버입니다.
현재 단계는 MVP 도메인 API를 빠르게 붙여 프론트엔드와 데이터 흐름을 맞출 수 있도록 구성한 상태입니다.

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
| API Docs | springdoc-openapi, Swagger UI |
| Monitoring | Spring Boot Actuator |
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

## Project Structure

```text
db
└── init
    └── 001_mvp_schema.sql
src/main/java/com/fedstock/backend
├── auth
│   ├── api
│   │   └── dto
│   ├── application
│   └── infrastructure
├── store
│   ├── api
│   │   └── dto
│   ├── application
│   └── infrastructure
├── product
│   ├── api
│   │   └── dto
│   ├── application
│   └── infrastructure
├── sale
│   ├── api
│   │   └── dto
│   ├── application
│   └── infrastructure
├── prediction
│   ├── api
│   │   └── dto
│   ├── application
│   └── infrastructure
├── main
│   ├── api
│   ├── config
│   └── error
├── demo
│   ├── api
│   │   └── dto
│   ├── application
│   ├── domain
│   └── infrastructure
└── example_reservation
    ├── api
    ├── application
    ├── domain
    └── infrastructure
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
stores
store_members
products
inventory
sales
inventory_predictions
```

샘플 계정:

```text
email: owner@example.com
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
JWT_SECRET=fedstock-local-development-secret-change-me
JWT_EXPIRATION_HOURS=24
```

Spring profile:

| Profile | Purpose |
| --- | --- |
| `local` | 로컬 개발 |
| `docker` | Docker Compose 실행 |

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

## API Docs

서버 실행 후 Swagger UI에서 API를 확인합니다.

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Health Check

```bash
curl http://localhost:8080/health
```

Actuator health:

```bash
curl http://localhost:8080/actuator/health
```

## MVP API

인증이 필요한 API는 `Authorization: Bearer {token}` 헤더를 사용합니다.
회원가입 또는 로그인 응답의 `token`을 그대로 전달하면 됩니다.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@example.com",
    "password": "password12"
  }'
```

구현된 API:

```text
GET    /health
POST   /api/auth/register
POST   /api/auth/login
GET    /api/auth/me

GET    /api/stores
POST   /api/stores
GET    /api/stores/{storeId}
PATCH  /api/stores/{storeId}
GET    /api/stores/{storeId}/members
POST   /api/stores/{storeId}/members

GET    /api/stores/{storeId}/products
POST   /api/stores/{storeId}/products
GET    /api/stores/{storeId}/products/{productId}
PATCH  /api/stores/{storeId}/products/{productId}
PUT    /api/stores/{storeId}/products/{productId}/inventory

POST   /api/stores/{storeId}/sales
GET    /api/stores/{storeId}/sales

GET    /api/stores/{storeId}/predictions/latest
GET    /api/stores/{storeId}/predictions
POST   /api/stores/{storeId}/predictions
```

권한 기준:

- 매장 조회, 상품, 재고, 판매, 예측 조회: 매장 멤버 `OWNER` 또는 `STAFF`
- 매장 수정, 멤버 추가, 예측 생성: `OWNER`

## Demo API

서버 동작, DB 연결, Swagger 문서화를 확인하기 위한 샘플 CRUD입니다.

```bash
curl -X POST http://localhost:8080/api/demos \
  -H "Content-Type: application/json" \
  -d '{
    "title": "first demo",
    "content": "demo content"
  }'
```

```bash
curl http://localhost:8080/api/demos
curl http://localhost:8080/api/demos/1
```

```bash
curl -X PUT http://localhost:8080/api/demos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "updated demo",
    "content": "updated content"
  }'
```

```bash
curl -X DELETE http://localhost:8080/api/demos/1
```

## Commit Convention

커밋 메시지는 영어 한 줄로 간결하게 작성합니다.
scope 괄호는 사용하지 않습니다.

```text
feat: add demo crud api
fix: handle demo not found
docs: update backend readme
```

## Before Real Domain Work

실제 API 개발 전 필요한 결정 사항:

- 인증/인가 방식 확정
- 운영 DB 계정 및 배포 환경 변수 분리
- 마이그레이션 도구 적용 여부 결정
