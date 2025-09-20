# Chaos Engineering 실험 플랫폼

Spring Boot + Kotlin 기반의 Chaos Engineering 실험 애플리케이션입니다. E-commerce 도메인 모델을 통해 실제 운영 환경과 유사한 장애 시나리오를 테스트할 수 있습니다.

## 🏗️ 프로젝트 개요

이 애플리케이션은 Netflix의 Chaos Engineering 원칙을 바탕으로 설계되었으며, 다음과 같은 특징을 갖습니다:

- **실제적인 도메인 모델**: 상품, 주문, 재고, 결제 시스템으로 구성된 E-commerce 플랫폼
- **체계적인 장애 주입**: chaos-monkey-spring-boot 라이브러리를 활용한 자동화된 카오스 테스트
- **복원력 패턴**: Circuit Breaker, Fallback 메커니즘 구현
- **종합적인 모니터링**: Actuator, Prometheus, Swagger 기반 관찰 가능성

## 🛠️ 기술 스택

- **언어**: Kotlin 1.9.25
- **프레임워크**: Spring Boot 3.5.6
- **빌드 도구**: Gradle 8.14.3
- **Java 버전**: JDK 17
- **Chaos Engineering**: chaos-monkey-spring-boot 3.1.0
- **복원력**: Resilience4j Circuit Breaker
- **모니터링**: Micrometer + Prometheus
- **문서화**: SpringDoc OpenAPI (Swagger)

## 🏛️ 아키텍처

### 도메인 모델
```
Product (상품)     Order (주문)     Inventory (재고)     Payment (결제)
├── 상품 정보       ├── 주문 처리     ├── 재고 관리        ├── 결제 처리
├── 재고 수량       ├── 상태 추적     ├── 예약/해제        ├── 다양한 결제 수단
└── 카테고리        └── 배송 정보     └── 가용성 확인      └── 트랜잭션 관리
```

### 서비스 레이어
- **OrderService**: 주문 생성부터 완료까지 전체 오케스트레이션
- **ProductService**: 상품 카탈로그 관리 및 검색
- **InventoryService**: 재고 확인, 예약, 해제 로직
- **PaymentService**: 결제 처리 및 Circuit Breaker 적용

### 외부 시스템 연동
- **InventoryClient**: 외부 재고 시스템 (Mock 구현)
- **PaymentClient**: 외부 결제 게이트웨이 (Mock 구현)

## 🚀 빠른 시작

### 1. 애플리케이션 실행
```bash
# 프로젝트 클론 후
./gradlew bootRun
```

### 2. API 문서 확인
브라우저에서 다음 URL로 접속:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API 문서**: http://localhost:8080/v3/api-docs

### 3. 헬스 체크
```bash
curl http://localhost:8080/actuator/health
```

## 🐵 Chaos Engineering 테스트 가이드

### 기본 Chaos Monkey 설정

#### 1. Chaos Monkey 활성화
```bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/enable
```

#### 2. 현재 설정 확인
```bash
curl http://localhost:8080/actuator/chaosmonkey
```

#### 3. 어택 설정 변경
```bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
  -H "Content-Type: application/json" \
  -d '{
    "level": 8,
    "latencyRangeStart": 2000,
    "latencyRangeEnd": 5000,
    "latencyActive": true,
    "exceptionsActive": true
  }'
```

### 고급 Chaos Monkey 설정

다양한 공격 유형과 강도를 설정할 수 있습니다:

#### 1. 지연 공격 설정
응답 시간을 인위적으로 늘려 성능 저하를 시뮬레이션:
```bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
  -H "Content-Type: application/json" \
  -d '{
    "latencyActive": true,
    "latencyRangeStart": 2000,
    "latencyRangeEnd": 5000,
    "level": 7
  }'
```

#### 2. 예외 공격 설정
무작위 예외를 발생시켜 오류 처리 로직을 테스트:
```bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
  -H "Content-Type: application/json" \
  -d '{
    "exceptionsActive": true,
    "level": 5
  }'
```

#### 3. 혼합 공격 설정
지연과 예외를 함께 적용하여 복합적인 장애 상황 시뮬레이션:
```bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
  -H "Content-Type: application/json" \
  -d '{
    "latencyActive": true,
    "latencyRangeStart": 1000,
    "latencyRangeEnd": 3000,
    "exceptionsActive": true,
    "level": 6
  }'
```

## 📡 API 엔드포인트

### 비즈니스 API

#### 상품 관리 (`/api/products`)
```bash
# 전체 상품 조회
curl http://localhost:8080/api/products

# 특정 상품 조회
curl http://localhost:8080/api/products/{productId}

# 카테고리별 상품 조회
curl http://localhost:8080/api/products/category/{category}

# 상품 검색
curl "http://localhost:8080/api/products/search?keyword=laptop"
```

#### 주문 관리 (`/api/orders`)
```bash
# 주문 생성 (Chaos 테스트 대상)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "items": [
      {
        "productId": "PROD-001",
        "productName": "노트북",
        "quantity": 1,
        "unitPrice": 1500000
      }
    ],
    "shippingAddress": "서울시 강남구",
    "paymentMethod": "CREDIT_CARD"
  }'

# 주문 조회
curl http://localhost:8080/api/orders/{orderId}

# 사용자 주문 목록
curl http://localhost:8080/api/orders/user/{userId}
```

### Chaos Engineering API (Actuator 기반)

#### Chaos Monkey 제어
```bash
# Chaos Monkey 활성화/비활성화
curl -X POST http://localhost:8080/actuator/chaosmonkey/enable
curl -X POST http://localhost:8080/actuator/chaosmonkey/disable

# 현재 상태 확인
curl http://localhost:8080/actuator/chaosmonkey/status

# Watcher 설정 확인
curl http://localhost:8080/actuator/chaosmonkey/watchers

# 공격 설정 확인
curl http://localhost:8080/actuator/chaosmonkey/assaults
```

### 모니터링 엔드포인트

```bash
# 헬스 체크
curl http://localhost:8080/actuator/health

# 메트릭스 (Prometheus 형식)
curl http://localhost:8080/actuator/prometheus

# Chaos Monkey 상태
curl http://localhost:8080/actuator/chaosmonkey/status
```

## 🧪 실전 테스트 시나리오

### 시나리오 1: 주문 생성 중 장애 발생

1. **정상 상태 확인**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "items": [{"productId": "PROD-001", "productName": "테스트 상품", "quantity": 1, "unitPrice": 10000}],
    "shippingAddress": "서울시 강남구",
    "paymentMethod": "CREDIT_CARD"
  }'
```

2. **Chaos Monkey 활성화**
```bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/enable
```

3. **동일한 주문 요청 반복 실행** (몇 번 중 일부는 실패하거나 지연됨)

4. **로그 및 메트릭 확인**
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/chaosmonkey/status
```

### 시나리오 2: 결제 시스템 Circuit Breaker 테스트

1. **높은 예외 발생율 설정**
```bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
  -H "Content-Type: application/json" \
  -d '{
    "exceptionsActive": true,
    "level": 8
  }'
```

2. **연속 주문 생성으로 Circuit Breaker 동작 유발**

3. **Circuit Breaker 상태 확인**
```bash
curl http://localhost:8080/actuator/health | jq '.components.circuitBreakers'
```

### 시나리오 3: 점진적 성능 저하 모니터링

1. **지연 공격 시작**
```bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
  -H "Content-Type: application/json" \
  -d '{
    "latencyActive": true,
    "latencyRangeStart": 2000,
    "latencyRangeEnd": 8000,
    "level": 7
  }'
```

2. **지속적인 API 호출로 응답 시간 측정**
```bash
# 반복 실행하여 응답 시간 변화 관찰
time curl http://localhost:8080/api/products
```

3. **메트릭 모니터링**
```bash
curl http://localhost:8080/actuator/prometheus | grep http_server_requests
```

## 📊 모니터링 및 관찰

### 모니터링 스택 구성

이 프로젝트는 Prometheus + Grafana 기반의 종합적인 모니터링 환경을 제공합니다.

#### 1. 모니터링 환경 시작

```bash
# 모니터링 스택 실행 (백그라운드)
docker-compose -f docker-compose.monitoring.yml up -d

# 실행 상태 확인
docker-compose -f docker-compose.monitoring.yml ps
```

#### 2. 접속 URL
- **Grafana 대시보드**: http://localhost:3000 (로그인 없이 바로 접속 가능)
- **Prometheus**: http://localhost:9090
- **AlertManager**: http://localhost:9093

> 💡 **Grafana 접속 팁**: 익명 접속이 활성화되어 있어 별도 로그인 없이 바로 대시보드를 확인할 수 있습니다. 필요시 admin/admin으로 로그인하여 설정을 변경할 수 있습니다.

#### 3. 애플리케이션 실행 후 모니터링 시작
```bash
# 1. Spring Boot 애플리케이션 실행
./gradlew bootRun

# 2. 메트릭 수집 확인
curl http://localhost:8080/actuator/prometheus

# 3. Grafana에서 "Chaos Engineering 모니터링" 대시보드 열기
```

### 실시간 카오스 모니터링 시나리오

#### 시나리오 1: 응답시간 모니터링
```bash
# 1. Chaos Monkey 지연 공격 활성화
curl -X POST http://localhost:8080/actuator/chaosmonkey/enable
curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
  -H "Content-Type: application/json" \
  -d '{
    "latencyActive": true,
    "latencyRangeStart": 2000,
    "latencyRangeEnd": 5000,
    "level": 8
  }'

# 2. 반복적인 API 호출로 지연 효과 확인
while true; do
  curl -w "응답시간: %{time_total}s\n" http://localhost:8080/api/products
  sleep 1
done

# 3. Grafana에서 "HTTP 요청 응답시간" 패널에서 실시간 증가 확인
```

#### 시나리오 2: 에러율 모니터링
```bash
# 1. 예외 공격 활성화
curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
  -H "Content-Type: application/json" \
  -d '{
    "exceptionsActive": true,
    "latencyActive": false,
    "level": 7
  }'

# 2. 주문 생성 API 반복 호출
for i in {1..20}; do
  curl -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d '{
      "userId": "test-user-'$i'",
      "items": [{"productId": "PROD-001", "productName": "테스트 상품", "quantity": 1, "unitPrice": 10000}],
      "shippingAddress": "서울시 강남구",
      "paymentMethod": "CREDIT_CARD"
    }'
  echo "주문 #$i 완료"
  sleep 0.5
done

# 3. Grafana에서 "HTTP 에러율" 패널에서 5xx 에러 증가 확인
```

#### 시나리오 3: Circuit Breaker 모니터링
```bash
# 1. 높은 강도의 예외 공격으로 Circuit Breaker 트리거
curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
  -H "Content-Type: application/json" \
  -d '{
    "exceptionsActive": true,
    "level": 10
  }'

# 2. Circuit Breaker 상태 확인
curl http://localhost:8080/actuator/health | jq '.components.circuitBreakers'

# 3. Resilience4j 메트릭 확인
curl http://localhost:8080/actuator/prometheus | grep resilience4j
```

### 커스텀 대시보드 메트릭

Grafana 대시보드에서 확인할 수 있는 주요 메트릭:

#### HTTP 성능 메트릭
```promql
# 평균 응답시간
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m]) * 1000

# 요청 처리량 (RPS)
rate(http_server_requests_seconds_count[5m])

# 에러율 (5xx)
rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) * 100
```

#### JVM 메트릭
```promql
# Heap 메모리 사용량
jvm_memory_used_bytes{area="heap"}

# GC 시간
rate(jvm_gc_pause_seconds_sum[5m])

# 스레드 수
jvm_threads_live_threads
```

#### Chaos Monkey 메트릭
```promql
# Chaos Monkey 상태 (활성화/비활성화)
chaos_monkey_enabled

# 공격 실행 횟수 (커스텀 메트릭이 있는 경우)
rate(chaos_monkey_assaults_total[5m])
```

### 주요 메트릭 해석

- **HTTP 요청**: 응답 시간, 성공/실패율
- **Circuit Breaker**: 상태 변화, 실패율
- **JVM**: 메모리, GC, 스레드 상태
- **Chaos Monkey**: 공격 횟수, 성공률

### 로그 확인
애플리케이션 로그에서 다음 패턴을 확인할 수 있습니다:
- `ChaosMonkey`: 카오스 공격 실행 로그
- `CircuitBreaker`: Circuit Breaker 상태 변경
- `PaymentService`: 결제 처리 및 Fallback 실행
- `OrderService`: 주문 처리 흐름 및 예외 처리

### 모니터링 환경 정리
```bash
# 모니터링 스택 종료
docker-compose -f docker-compose.monitoring.yml down

# 볼륨까지 함께 삭제 (데이터 초기화)
docker-compose -f docker-compose.monitoring.yml down -v
```

## 🔧 개발 및 테스트

### 빌드 및 테스트
```bash
# 전체 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 상세한 테스트 출력
./gradlew test --info

# 검증 작업 (테스트 + 정적 분석)
./gradlew check
```

### Docker 이미지 생성
```bash
./gradlew bootBuildImage
```

## 🎯 확장 가능한 테스트 아이디어

1. **커스텀 비즈니스 API**: 새로운 도메인 서비스를 추가하여 카오스 테스트 대상 확장
2. **외부 시스템 통합**: 실제 데이터베이스, 메시지 큐와 연동한 테스트
3. **로드 테스트 결합**: JMeter, K6와 결합한 부하 + 카오스 테스트
4. **관찰 가능성 강화**: Grafana 대시보드를 통한 실시간 모니터링
5. **커스텀 Watcher**: 특정 클래스나 메서드만 대상으로 하는 선택적 카오스 주입

## 📚 참고 자료

- [Chaos Engineering 원칙](https://principlesofchaos.org/)
- [chaos-monkey-spring-boot 문서](https://codecentric.github.io/chaos-monkey-spring-boot/)
- [Resilience4j 가이드](https://resilience4j.readme.io/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

이 애플리케이션을 통해 시스템의 복원력을 체계적으로 테스트하고, 장애 상황에서의 동작을 미리 검증해보세요! 🚀