# parkmate-batch-service

## 소개
batchservice는 Parkmate 플랫폼의 배치 작업을 처리하는 Spring Boot 기반의 서비스입니다. 리뷰 요약(평점, 개수 집계), 호스트 정산, Kafka 이벤트 처리 등 다양한 배치 및 실시간 집계 기능을 제공합니다.

## 주요 기능
- **리뷰 요약(평점, 개수 집계)**
  - 각 주차장(parkingLotUuid)별로 리뷰의 평균 평점과 총 리뷰 수를 집계합니다.
  - 실시간 집계(`ReviewSummaryRealtime`)와 배치 집계(`ReviewSummary`)를 결합하여, 최신성과 신뢰성을 모두 확보합니다.
  - API 엔드포인트: `/internal/batch/review-summary?parkingLotUuid=...`
  - 응답 예시:
    ```json
    {
      "averageRating": 4.5,
      "totalReviews": 123
    }
    ```
- **호스트 정산**
  - 호스트별 정산 내역 배치 처리
- **Kafka 이벤트 처리**
  - 리뷰 생성 등 이벤트 기반 데이터 처리

## 폴더 구조
- `reviewsummary`: 리뷰 요약 관련 (presentation, domain, application, infrastructure, dto)
- `hostsettlement`: 호스트 정산 관련 (presentation, infrastructure, dto, domain, application, vo)
- `kafka`: Kafka 이벤트 처리 (event, config, consumer)
- `common`: 공통 설정, 예외, 엔티티 등

## 기술 스택
- Java 17
- Spring Boot 3
- Spring Batch, Spring Data JPA, Spring Kafka, Spring Cloud (Eureka, OpenFeign)
- MySQL, MongoDB
- Gradle

## 실행 방법
```bash
./gradlew build
./gradlew bootRun
```

## 환경설정
- 데이터베이스, Kafka 등 주요 설정은 `src/main/resources/application.yml`에서 관리합니다.
- 기본 포트: 8087

## API 문서
- Swagger UI: `/swagger-ui.html`
- OpenAPI: `/v3/api-docs`

## 기여 방법
1. 이슈 등록
2. PR 생성

---
문의 및 자세한 내용은 각 모듈의 소스코드를 참고해 주세요.
