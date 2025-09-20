# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 작업할 때 참고할 수 있는 가이드를 제공합니다.

## 프로젝트 개요

이 프로젝트는 chaos engineering 실험을 위한 Spring Boot Kotlin 애플리케이션입니다. Netflix Chaos Engineering suite (Chaos Monkey, Latency Monkey, Doctor Monkey 등)를 구현하여 시스템의 복원력과 장애 시나리오를 테스트합니다.

## 기술 스택

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.4.5
- **Build Tool**: Gradle 8.10.2
- **Java Version**: JDK 17
- **Resilience**: Resilience4j (Circuit Breaker pattern)
- **Monitoring**: Micrometer with Prometheus
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **AOP**: Spring AOP for chaos injection

## 주요 개발 명령어

### 빌드 및 실행
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Clean build artifacts
./gradlew clean

# Create executable JAR
./gradlew bootJar

# Build Docker/OCI image
./gradlew bootBuildImage
```

### 테스트
```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run all verification tasks (tests + checks)
./gradlew check
```

## 아키텍처 개요

### 도메인 모델
이 애플리케이션은 전자상거래 시스템을 시뮬레이션하며 다음의 핵심 엔티티들로 구성됩니다:
- **Product**: 상품 카탈로그 관리
- **Order**: 주문 처리 (상품 목록, 상태 추적)
- **Inventory**: 재고 관리 및 예약
- **Payment**: 다양한 결제 방식 처리

### Service 레이어
- **OrderService**: 주문 생성, 재고 확인, 결제 처리를 조율
- **ProductService**: 상품 카탈로그 운영
- **InventoryService**: 재고 관리 및 가용성 확인
- **PaymentService**: 결제 처리 및 검증

### 외부 Client
- **InventoryClient**: 외부 재고 시스템 연동
- **PaymentClient**: 외부 결제 게이트웨이 연동

### Chaos Engineering 구성요소

이 애플리케이션은 표준화된 chaos engineering을 위해 **chaos-monkey-spring-boot** 라이브러리를 사용합니다:

#### Chaos Monkey for Spring Boot
- **Watcher 지원**: @Controller, @Service, @Repository, @Component 애노테이션이 붙은 클래스들을 자동으로 모니터링
- **공격 유형**:
  - **Latency**: 설정 가능한 지연 주입 (기본 1000-3000ms)
  - **Exception**: 무작위 runtime exception 발생
  - **AppKiller**: 애플리케이션 종료 가능 (기본적으로 비활성화)
  - **Memory**: 메모리 소모 공격 (기본적으로 비활성화)
- **런타임 제어**: 모든 설정이 REST API를 통해 런타임에 변경 가능
- **예약 공격**: 설정 가능한 cron 기반 공격 실행

#### Chaos Monkey Controller
애플리케이션에는 ChaosMonkeyController (`/api/chaos/*`)가 포함되어 다음 기능들을 제공합니다:
- **활성화/비활성화**: `/api/chaos/enable`, `/api/chaos/disable`
- **상태 모니터링**: `/api/chaos/status` - 현재 설정 및 상태 조회
- **설정 업데이트**: `/api/chaos/config` - 런타임 공격 파라미터 변경
- **빠른 테스트**: `/api/chaos/quick-test` - 미리 정의된 테스트 시나리오
- **Swagger 연동**: 모든 endpoint가 Swagger UI에 문서화됨

## 설정

### 애플리케이션 설정
`src/main/resources/application.yml`의 주요 설정:
- Chaos engineering 설정 (기본적으로 모두 비활성화)
- 외부 서비스를 위한 Circuit breaker 설정
- 모니터링을 위한 Actuator endpoint 설정
- Swagger/OpenAPI 문서화 설정

### 주요 설정 섹션

**CRITICAL**: chaos-monkey profile must be activated for proper auto-configuration:
```yaml
spring:
  profiles:
    active: chaos-monkey  # Required for chaos-monkey-spring-boot

chaos:
  monkey:
    enabled: false                 # Manual control via API
    watcher:
      controller: true             # @Controller/@RestController 감시
      service: true               # @Service 감시
      repository: true            # @Repository 감시
      component: true             # @Component 감시
    assaults:
      level: 10                   # 공격 강도 (1-10) - 최대
      deterministic: true         # 예측 가능한 공격 패턴
      latencyRangeStart: 500      # 최소 지연 시간 (ms)
      latencyRangeEnd: 1500       # 최대 지연 시간 (ms)
      latencyActive: false        # 지연 공격 비활성화
      exceptionsActive: true      # 예외 공격 활성화
      killApplicationActive: false # 애플리케이션 종료 공격 비활성화
      memoryActive: false         # 메모리 공격 비활성화
```

## 개발 패턴

### Chaos Monkey 런타임 제어
Chaos Monkey는 런타임에 REST API를 통해 제어할 수 있습니다:

```bash
# Chaos Monkey 활성화
curl -X POST http://localhost:8080/actuator/chaosmonkey/enable

# Chaos Monkey 비활성화
curl -X POST http://localhost:8080/actuator/chaosmonkey/disable

# 현재 설정 조회
curl http://localhost:8080/actuator/chaosmonkey

# 어택 설정 변경
curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
  -H "Content-Type: application/json" \
  -d '{"level":8, "latencyRangeStart":2000, "latencyRangeEnd":5000}'
```

### Circuit Breaker 패턴
외부 서비스 호출은 `application.yml`에서 설정된 `payment-service`와 `inventory-service`용 Resilience4j circuit breaker를 사용합니다.

### 모니터링 및 관찰성
- **Actuator endpoint**: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
- **Chaos Monkey endpoint**: `/actuator/chaosmonkey`, `/actuator/chaosmonkey/status`
- **Swagger UI**: `/swagger-ui.html`
- **API 문서**: `/v3/api-docs`

## 커뮤니케이션 규칙

### 언어 사용 정책
Claude Code가 이 프로젝트에서 작업할 때는 다음 규칙을 따릅니다:

- **기본 언어**: 한국어를 사용하여 개발 과정, 결과물, 에러 해결 과정을 설명
- **영어 유지**: 다음 요소들은 영어 그대로 유지
  - 개발 용어 (Spring Boot, Kotlin, REST API, Circuit Breaker 등)
  - 클래스명 (OrderService, ChaosMonkeyController 등)
  - 함수명 (enableChaosMonkey(), getChaosStatus() 등)
  - 파일명 (application.yml, build.gradle.kts 등)
  - 라이브러리명과 기술 스택명
  - API 엔드포인트 경로

### 커밋 메시지 작성 규칙
Git 커밋 메시지는 다음 형식을 따릅니다:

```
prefix: 한국어 설명 (개발 용어는 영어 유지)
```

#### 사용할 Prefix
- `feat:` - 새로운 기능 추가
- `fix:` - 버그 수정
- `chore:` - 빌드, 설정, 의존성 변경
- `docs:` - 문서 추가/수정
- `refactor:` - 코드 리팩토링 (기능 변경 없음)
- `test:` - 테스트 추가/수정
- `style:` - 코드 포맷팅, 세미콜론 등

#### Changes 기반 작성 원칙
커밋 메시지는 반드시 **실제 Git changes**만을 기반으로 작성해야 합니다:

**📋 작성 프로세스:**
1. `git status`로 변경된 파일 목록 확인
2. `git diff`로 실제 변경 내용 분석
3. 최종 변경사항만을 반영한 커밋 메시지 작성
4. 개발 과정의 시행착오는 커밋 메시지에서 제외

**✅ 올바른 접근:**
- 개발 과정에서 TempController.kt 생성 → 삭제 → ChaosController.kt만 최종 추가
- Git changes: ChaosController.kt 파일만 추가됨
- 커밋 메시지: `feat: ChaosController 추가`

**❌ 잘못된 접근:**
- 커밋 메시지: `feat: TempController 생성 및 ChaosController 추가`
- (실제로는 TempController는 changes에 없음)

**🔍 주의사항:**
- 개발 요청 기록이 아닌 최종 결과물만 기술
- 임시 파일 생성/삭제는 changes에 반영되지 않으므로 언급하지 않음
- 실제 코드 변경, 설정 변경, 파일 추가/삭제만 커밋 메시지에 포함

#### 커밋 메시지 예시
```bash
feat: OrderService에 재고 확인 로직 추가
fix: ChaosMonkeyController의 status endpoint 404 오류 수정
chore: Spring Boot 버전을 3.4.5로 업데이트
docs: README.md에 chaos monkey 테스트 가이드 추가
refactor: PaymentService의 validation 로직 개선
test: InventoryService unit test 추가
style: ChaosMonkeyController 코드 포맷팅 정리
```

### 커뮤니케이션 예시

**좋은 예시:**
- "OrderService의 createOrder() 메서드에서 재고 부족 시 예외가 발생하도록 수정했습니다."
- "chaos-monkey-spring-boot 라이브러리 초기화 문제를 해결하기 위해 application.yml에 spring.profiles.active: chaos-monkey를 추가했습니다."
- "Swagger UI에서 /api/chaos/enable endpoint 테스트가 가능합니다."

**피해야 할 예시:**
- "I've modified the createOrder method in OrderService to throw exception when inventory is insufficient."
- "주문서비스의 주문생성메서드에서 재고부족시 익셉션이 발생하도록 수정했습니다." (개발 용어를 무리하게 한국어로 번역)

## 프로젝트 구조

```
src/main/kotlin/com/jsh/chaosengineering/
├── ChaosEngineeringApplication.kt          # Main Spring Boot 애플리케이션
├── domain/                                 # 도메인 엔티티
│   ├── Product.kt, Order.kt, Inventory.kt, Payment.kt
├── service/                                # 비즈니스 로직 레이어 (Chaos 대상)
│   ├── OrderService.kt                     # 주문 처리 조율
│   ├── ProductService.kt, InventoryService.kt, PaymentService.kt
├── controller/                             # REST API endpoint
│   ├── OrderController.kt, ProductController.kt
│   └── ChaosMonkeyController.kt            # Chaos engineering API
├── client/                                 # 외부 서비스 client (circuit breaker 포함)
│   ├── InventoryClient.kt, PaymentClient.kt
└── config/                                 # 설정 클래스 (있는 경우)
```

### Chaos Engineering 사용법

1. **애플리케이션 실행**:
   ```bash
   ./gradlew bootRun
   ```

2. **Chaos Monkey 활성화** (Swagger UI 또는 API 직접 호출):
   ```bash
   # Swagger UI를 통한 제어 (권장)
   curl -X POST http://localhost:8080/api/chaos/enable

   # 또는 Actuator 직접 호출
   curl -X POST http://localhost:8080/actuator/chaosmonkey/enable
   ```

3. **서비스 호출하여 카오스 테스트**:
   - OrderService, PaymentService, InventoryService 메서드 호출 시 자동으로 카오스 주입
   - 설정된 확률에 따라 지연이나 예외 발생

4. **설정 조정** (runtime):
   ```bash
   # Swagger UI API 사용 (권장)
   curl -X PUT http://localhost:8080/api/chaos/config \
     -H "Content-Type: application/json" \
     -d '{"level":10, "latencyActive":false, "exceptionsActive":true}'

   # 또는 Actuator 직접 호출
   curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
     -H "Content-Type: application/json" \
     -d '{"level":10, "latencyActive":false, "exceptionsActive":true}'
   ```

5. **빠른 테스트 시작**:
   ```bash
   curl -X POST http://localhost:8080/api/chaos/quick-test
   ```

#### 모니터링 및 상태 확인
- **Chaos 상태**: `GET /api/chaos/status`
- **Health endpoints**: `/actuator/health`
- **Swagger UI**: `/swagger-ui.html` - 모든 chaos API 대화형 테스트
- **Actuator chaos**: `/actuator/chaosmonkey` - 현재 설정 조회
- **Prometheus metrics**: `/actuator/prometheus`