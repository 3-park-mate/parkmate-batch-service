# batchservice

## 소개
**batchservice**는 Parkmate 플랫폼의 배치 및 실시간 데이터 집계, 매출 통계, 이벤트 처리를 담당하는 Spring Boot 기반 백엔드 서비스입니다.  
대용량 데이터의 효율적 집계와, 주차장/호스트/이벤트별 통계 자동화가 주요 목적입니다.

---

## 주요 기능

### 1. 리뷰 요약 (평점, 개수 집계)
- **목적:**  
  각 주차장(parkingLotUuid)별로 등록된 리뷰의 평균 평점과 총 리뷰 수를 집계합니다.
- **집계 방식:**  
  - **실시간 집계(`ReviewSummaryRealtime`)**:  
    Kafka 등 이벤트 기반으로 리뷰가 생성될 때마다 실시간으로 집계 데이터가 갱신됩니다.
  - **배치 집계(`ReviewSummary`)**:  
    일정 주기(예: 매일 새벽)로 전체 리뷰 데이터를 스캔하여 집계값을 재계산합니다.
  - **최종 응답:**  
    실시간 집계와 배치 집계를 합산하여,  
    - `averageRating`(평균 평점): (실시간 평점×실시간 리뷰수 + 배치 평점×배치 리뷰수) ÷ 전체 리뷰수  
    - `totalReviews`(총 리뷰 수): 실시간 리뷰수 + 배치 리뷰수  
    로 계산합니다.
- **API 엔드포인트:**  
  ```
  GET /internal/batch/review-summary?parkingLotUuid={uuid}
  ```
- **응답 예시:**
  ```json
  {
    "averageRating": 4.5,
    "totalReviews": 123
  }
  ```
- **관련 주요 클래스:**  
  - `ReviewSummary`, `ReviewSummaryRealtime` (도메인)
  - `ReviewSummaryServiceImpl` (비즈니스 로직)
  - `ReviewSummaryInternalController` (API)

---

### 2. 호스트 매출 집계 (일매출, 월매출)
- **목적:**  
  각 호스트별로 일매출, 월매출을 집계하여 조회할 수 있도록 데이터를 배치로 생성합니다.
- **집계 방식:**  
  - **일매출 집계:**  
    하루 단위로 호스트별 매출 데이터를 집계하여 저장합니다.
  - **월매출 집계:**  
    월 단위로 호스트별 매출 데이터를 집계하여 저장합니다.
- **API 엔드포인트:**  
  - **일매출 조회**
    ```
    GET /internal/settlements/daily
    헤더: X-Host-UUID: {hostUuid}
    파라미터: parkingLotUuid, date(YYYY-MM-DD)
    ```
    - **응답 예시:**
      ```json
      {
        "date": "2024-06-01",
        "totalSalesAmount": 150000
      }
      ```
  - **월매출 조회**
    ```
    GET /internal/settlements/monthly
    헤더: X-Host-UUID: {hostUuid}
    파라미터: parkingLotUuid, year, month, cycle
    ```
    - **응답 예시:**
      ```json
      {
        "yearMonth": "2024-06",
        "totalSalesAmount": 3200000
      }
      ```
- **관련 주요 클래스:**  
  - `HostSettlement`, `SettlementCycle` (도메인)
  - `HostSettlementServiceImpl` (비즈니스 로직)
  - `HostSettlementInternalController` (API)
  - 응답 DTO: `DailySalesResponseDto`, `MonthlySalesResponseDto`

---

### 3. Kafka 이벤트 처리
- **목적:**  
  리뷰 생성, 결제 등 주요 이벤트를 Kafka로 수신하여, 실시간 집계 및 후속 처리를 수행합니다.
- **구성:**  
  - `kafka/event`: 이벤트 객체 정의
  - `kafka/consumer`: Kafka Consumer 구현
  - `kafka/config`: Kafka 설정

---

## 폴더 구조 및 역할

- **reviewsummary**  
  리뷰 요약 관련 전체 기능 (presentation, domain, application, infrastructure, dto)
- **hostsettlement**  
  호스트 매출 집계 관련 전체 기능 (presentation, application, domain, infrastructure, dto, vo)
- **kafka**  
  Kafka 이벤트 처리 (event, config, consumer)
- **common**  
  공통 엔티티, 예외, 응답 포맷, 설정 등

---

## 기술 스택 및 환경

- **Java 17**
- **Spring Boot 3.x**
- **Spring Batch**: 대용량 데이터 배치 처리
- **Spring Data JPA**: ORM, DB 연동
- **Spring Kafka**: 이벤트 기반 실시간 처리
- **Spring Cloud (Eureka, OpenFeign)**: 마이크로서비스 연동
- **MySQL, MongoDB**: 데이터 저장소
- **Gradle**: 빌드/의존성 관리

---

## 실행 방법

1. **환경 변수 및 DB/Kafka 준비**
   - `src/main/resources/application.yml`에서 DB, Kafka 등 설정 확인 및 수정
   - MySQL, Kafka, Eureka 서버 등 필요 서비스 실행

2. **빌드 및 실행**
   ```bash
   ./gradlew build
   ./gradlew bootRun
   ```

3. **API 문서**
   - Swagger UI: [http://localhost:8087/swagger-ui.html](http://localhost:8087/swagger-ui.html)
   - OpenAPI: [http://localhost:8087/v3/api-docs](http://localhost:8087/v3/api-docs)

---

## 기여 방법

1. 이슈 등록
2. Fork & PR 생성
3. 코드 리뷰 및 병합

---

## 참고/문의
- 각 모듈별 상세 구현은 소스코드를 참고해 주세요.
- 추가 문의는 담당자 또는 이슈 게시판을 이용해 주세요.