# Fedstock Backend

Fedstock 백엔드 서버입니다.  
초기 목표는 팀원이 빠르게 실행하고, 같은 구조로 기능을 추가할 수 있는 Spring Boot 기반 API 서버를 만드는 것입니다.

## 사용 스택

| 영역 | 기술 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Build | Gradle Wrapper |
| Container | Docker, Docker Compose |
| Web | Spring Web |
| Validation | Spring Validation |
| Persistence | Spring Data JPA |
| Local DB | H2 Database |
| Monitoring | Spring Boot Actuator |
| Test | JUnit 5, Spring Boot Test |

## 디자인 아키텍처

이 프로젝트는 **Modular Monolith + Clean Architecture Lite** 구조를 사용합니다.

졸업 프로젝트 단계에서는 마이크로서비스보다 하나의 서버 안에서 기능을 명확히 나누는 방식이 더 현실적입니다.  
기능별 패키지를 모듈처럼 관리하고, 각 기능 내부는 `api -> application -> domain <- infrastructure` 흐름으로 분리합니다.

```text
Client
  ↓
api
  ↓
application
  ↓
domain
  ↑
infrastructure
```

### 선택 이유

- 초급~중급 개발자가 이해하기 쉽습니다.
- Controller에 로직이 몰리는 문제를 줄일 수 있습니다.
- DB, 외부 API, 캐시 같은 기술 구현을 나중에 바꾸기 쉽습니다.
- 기능이 늘어나도 패키지 단위로 책임을 추적하기 쉽습니다.

## 현재 폴더 구조

```text
src/main/java/com/fedstock/backend
├── FedstockBackendApplication.java
├── main
│   ├── api
│   └── error
└── example_reservation
    ├── api
    │   └── dto
    ├── application
    ├── domain
    └── infrastructure
```

자세한 폴더링 규칙과 코드 컨벤션은 [docs/CONVENTION.md](docs/CONVENTION.md)를 확인합니다.

## 실행 방법

JDK 21이 필요합니다.

```bash
./gradlew bootRun
```

서버 기본 포트는 `8080`입니다.

```bash
curl http://localhost:8080/api/main/health
```

## Docker 실행 방법

Docker Desktop 또는 Docker Engine이 필요합니다.

```bash
docker compose up --build
```

백그라운드로 실행하려면 다음 명령을 사용합니다.

```bash
docker compose up --build -d
```

종료:

```bash
docker compose down
```

Docker 실행 시 `docker` 프로필이 적용됩니다.
현재는 팀원이 바로 실행해볼 수 있도록 H2 인메모리 DB를 사용합니다.

## 예시 API

예약 생성:

```bash
curl -X POST http://localhost:8080/api/example-reservations \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Kim",
    "reservationDate": "2026-05-01",
    "guestCount": 2
  }'
```

예약 목록 조회:

```bash
curl http://localhost:8080/api/example-reservations
```

예약 확정:

```bash
curl -X PATCH http://localhost:8080/api/example-reservations/1/confirm
```

## 개발 원칙

- 새 기능은 `example_reservation`과 같은 구조로 만듭니다.
- Controller는 요청과 응답만 담당합니다.
- Service는 하나의 유스케이스를 표현합니다.
- Domain은 핵심 규칙을 담습니다.
- Infrastructure는 DB, 외부 API 등 기술 구현을 담당합니다.
- 커밋 메시지는 영어 한 줄로 간결하게 작성합니다.

커밋 예시:

```text
feat: add reservation create api
fix: handle reservation not found
docs: update foldering guide
```
