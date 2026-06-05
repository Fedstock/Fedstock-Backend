# Fedstock Backend

Fedstock 서비스의 백엔드 API 서버입니다.
실제 매장 운영 서비스를 가정해 만든 포트폴리오 MVP이며, 현재 단계는 프론트엔드 API와 AI 서버 Gateway 역할을 중심으로 구성했습니다.
사용자 흐름, AI 분석 연동, 컨테이너 배포 구조를 빠르게 검증하는 데 초점을 두고 있으며, 인증/인가와 운영 보안 정책은 MVP 범위 밖으로 두었습니다.

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

현재 API는 데모 가능한 서비스 흐름을 우선해 공개 엔드포인트로 구성되어 있습니다. 실제 운영 전환 시에는 Spring Security와 권한 모델을 추가하는 것을 전제로 합니다.

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
├── v1
│   ├── ai
│   │   ├── api
│   │   └── application
│   ├── auth
│   │   └── api
│   ├── forecast
│   │   ├── api
│   │   ├── application
│   │   └── infrastructure
│   ├── item
│   │   ├── api
│   │   └── application
│   ├── localai
│   │   ├── api
│   │   └── application
│   ├── shared
│   │   └── ai
│   └── weather
│       ├── api
│       └── application
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

## V1 API

프론트엔드 연동용 v1 API입니다. 포트폴리오 MVP 단계에서는 시연과 AI 연동 검증을 우선해 모든 API를 별도 인증 절차 없이 호출합니다.
로그인 API는 화면 흐름과 매장 정보 응답을 맞추기 위한 형태로 유지하며, 실제 사용자 인증 수단으로 사용하지 않습니다.

```text
1. POST /api/v1/auth/login
2. GET  /api/v1/auth/me
3. POST /api/v1/auth/logout
4. GET  /api/v1/ai/health
5. POST /api/v1/ai/analyze-csv
6. POST /api/v1/ai/clients/register
7. GET  /api/v1/ai/clients/{clientId}/fl-model
8. GET  /api/v1/local-ai/health
9. POST /api/v1/forecast/analyze-csv
10. GET /api/v1/forecast/results/{analysisId}
11. GET /api/v1/forecast/results/{analysisId}/products/{itemId}/chart
12. GET /api/v1/forecast/results/{analysisId}/top-products
13. GET /api/v1/forecast/results/{analysisId}/flow-change
14. GET /api/v1/weather/insight
15. GET /api/v1/items/display-map
```

### 1. 로그인

```http
POST /api/v1/auth/login
```

```json
{
  "storeId": "owner@example.com",
  "password": "password12"
}
```

응답:

```json
{
  "store": {
    "storeId": "owner@example.com",
    "storeName": "Owner",
    "role": "STORE_MANAGER"
  }
}
```

### 2. 내 로그인 정보 조회

```http
GET /api/v1/auth/me
```

응답:

```json
{
  "storeId": "owner@example.com",
  "storeName": "Owner",
  "role": "STORE_MANAGER"
}
```

### 3. 로그아웃

```http
POST /api/v1/auth/logout
```

응답:

```json
{
  "success": true
}
```

### 4. AI 서버 상태 확인

AI Gateway API입니다. 프론트엔드는 아래 API를 호출하고, 백엔드는 `AI_BACKEND_URL` 기준으로 AI 서버의 `/health`에 요청을 전달합니다.

```http
GET /api/v1/ai/health
```

AI 서버 응답 예시:

```json
{
  "ok": true,
  "time": "2026-06-04 12:00:00",
  "summary": {
    "referenceRunDir": "/path/to/reference_run",
    "storageDir": "/path/to/storage",
    "selectedFeatures": [
      "lag_7",
      "lag_14",
      "rolling_mean_7"
    ],
    "clientCount": 48,
    "bubbleCount": 5,
    "isolatedCount": 3,
    "aggregatedModelCount": 2
  }
}
```

### 5. CSV AI 분석

AI Gateway API입니다. 프론트엔드는 CSV 파일을 보내고, 백엔드는 `AI_BACKEND_URL` 기준으로 AI 서버의 `/analyze-csv`에 `multipart/form-data` 요청을 전달합니다.

```http
POST /api/v1/ai/analyze-csv
Content-Type: multipart/form-data
```

Form fields:

```text
file=sales.csv
```

AI 서버 응답 예시:

```json
{
  "status": {
    "state": "loaded",
    "fileName": "sales.csv",
    "rowCount": 1234,
    "productCount": 20,
    "dateRange": "2026. 05. 01. - 2026. 05. 31.",
    "uploadedAt": "2026. 06. 01. 18:30:12",
    "validation": [],
    "issues": []
  },
  "data": {
    "source": "ai",
    "overviewMetrics": [],
    "salesTrend": [],
    "topProducts": [],
    "forecastSeries": [],
    "inventoryMetrics": [],
    "inventoryItems": [],
    "orderMetrics": [],
    "orderRecommendations": []
  },
  "model": {
    "paths": [],
    "modelCount": 1,
    "selectedFeatures": [],
    "sequenceLength": 14,
    "stockAvailable": true
  }
}
```

### 6. AI Client 등록

AI Gateway API입니다. 프론트엔드는 모델/importance 정보를 보내고, 백엔드는 `AI_BACKEND_URL` 기준으로 AI 서버의 `/clients/register`에 `multipart/form-data` 요청을 전달합니다.

```http
POST /api/v1/ai/clients/register
Content-Type: multipart/form-data
```

Form fields:

```text
client_id=NEW_CLIENT_01
model_file=client_NEW_CLIENT_01.pt
importance_file=NEW_CLIENT_01_importance.json
importance_json={"noisyImportance":[0.12,0.03,0.55,0.07]}
sample_weight=10
```

`importance_file` 또는 `importance_json` 중 하나는 반드시 필요합니다.

### 7. AI Client FL Model 다운로드

AI Gateway API입니다. 백엔드는 `AI_BACKEND_URL` 기준으로 AI 서버의 `/clients/{client_id}/fl-model`을 호출하고, PyTorch 모델 binary stream을 그대로 반환합니다.

```http
GET /api/v1/ai/clients/{clientId}/fl-model
```

응답 헤더:

```text
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="client_{client_id}_FL.pt"
```

### 8. 로컬 AI 서버 상태 확인 Legacy

```http
GET /api/v1/local-ai/health
```

백엔드는 `AI_BACKEND_URL` 기준으로 AI 서버의 `/health`를 확인합니다.

### 9. CSV 업로드 및 판매량 예측 실행 Legacy

```http
POST /api/v1/forecast/analyze-csv
Content-Type: multipart/form-data
```

Form fields:

```text
file=sales_history.csv
storeId=owner@example.com
```

백엔드는 `AI_BACKEND_URL` 기준으로 AI 서버의 `/analyze-csv`를 호출하고, 응답의 `analysisId`로 결과를 임시 저장합니다.

### 10. 예측 결과 상세 조회

```http
GET /api/v1/forecast/results/{analysisId}
```

### 11. 상품별 일별 예측 그래프 조회

```http
GET /api/v1/forecast/results/{analysisId}/products/{itemId}/chart
```

### 12. 상위 판매 예상 상품 조회

```http
GET /api/v1/forecast/results/{analysisId}/top-products?limit=8
```

### 13. 최근 판매 흐름 변화율 조회

```http
GET /api/v1/forecast/results/{analysisId}/flow-change?limit=10
```

### 14. 날씨 기반 운영 인사이트 조회

```http
GET /api/v1/weather/insight?location=서울특별시%20동작구
```

Query parameters:

```text
location  optional, default 서울특별시 동작구
latitude  optional
longitude optional
```

### 15. 상품 표시명 매핑 조회

```http
GET /api/v1/items/display-map?itemIds=FOODS_3_090,FOODS_3_586
```

응답:

```json
{
  "items": [
    {
      "itemId": "FOODS_3_090",
      "itemName": "식품 FOODS_3_090",
      "category": "식품",
      "mappingSource": "ITEM_MASTER"
    }
  ]
}
```

## Commit Convention

커밋 메시지는 영어 한 줄로 간결하게 작성합니다.
scope 괄호는 사용하지 않습니다.

```text
feat: add demo crud api
fix: handle demo not found
docs: update backend readme
```
