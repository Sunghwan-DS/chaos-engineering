# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot Kotlin application for chaos engineering experimentation. The project implements the Netflix Chaos Engineering suite (Chaos Monkey, Latency Monkey, Doctor Monkey, etc.) to test system resilience and failure scenarios.

## Tech Stack

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.6
- **Build Tool**: Gradle 8.14.3
- **Java Version**: JDK 17
- **Resilience**: Resilience4j (Circuit Breaker pattern)
- **Monitoring**: Micrometer with Prometheus
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **AOP**: Spring AOP for chaos injection

## Common Development Commands

### Build and Run
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

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run all verification tasks (tests + checks)
./gradlew check
```

## Architecture Overview

### Domain Model
The application simulates an e-commerce system with core entities:
- **Product**: Product catalog management
- **Order**: Order processing with items, status tracking
- **Inventory**: Stock management and reservation
- **Payment**: Payment processing with different methods

### Service Layer
- **OrderService**: Orchestrates order creation, inventory checks, payment processing
- **ProductService**: Product catalog operations
- **InventoryService**: Stock management and availability checks
- **PaymentService**: Payment processing and validation

### External Clients
- **InventoryClient**: External inventory system integration
- **PaymentClient**: External payment gateway integration

### Chaos Engineering Components

The application uses the **chaos-monkey-spring-boot** library for standardized chaos engineering:

#### Chaos Monkey for Spring Boot
- **Watcher Support**: Automatically monitors @Controller, @Service, @Repository, @Component annotated classes
- **Assault Types**:
  - **Latency**: Injects configurable delays (1000-3000ms by default)
  - **Exception**: Throws random runtime exceptions
  - **AppKiller**: Can terminate the application (disabled by default)
  - **Memory**: Memory consumption attacks (disabled by default)
- **Runtime Control**: All settings can be changed at runtime via REST API
- **Scheduled Attacks**: Configurable cron-based attack execution

#### Custom Chaos Test Controller
The application includes a comprehensive ChaosTestController (`/api/chaos/*`) that provides:
- Scenario-based testing (Black Friday, Disaster Recovery, Gradual Degradation)
- Manual chaos control endpoints for all chaos types
- Integration with the standard chaos-monkey-spring-boot library
- Swagger documentation for all chaos engineering APIs

## Configuration

### Application Configuration
Main configuration in `src/main/resources/application.yml`:
- Chaos engineering settings (all disabled by default)
- Circuit breaker configurations for external services
- Actuator endpoints for monitoring
- Swagger/OpenAPI documentation settings

### Key Configuration Sections
```yaml
chaos:
  monkey:
    enabled: false              # Runtime에 변경 가능
    watcher:
      controller: true          # @Controller/@RestController 감시
      service: true            # @Service 감시
      repository: true         # @Repository 감시
      component: true          # @Component 감시
    assaults:
      level: 5                 # 공격 강도 (1-10)
      latencyRangeStart: 1000  # 최소 지연 시간 (ms)
      latencyRangeEnd: 3000    # 최대 지연 시간 (ms)
      latencyActive: true      # 지연 공격 활성화
      exceptionsActive: true   # 예외 공격 활성화
```

## Development Patterns

### Chaos Monkey Runtime Control
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

### Circuit Breaker Pattern
External service calls use Resilience4j circuit breakers configured in `application.yml` for `payment-service` and `inventory-service`.

### Monitoring and Observability
- **Actuator endpoints**: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
- **Chaos Monkey endpoints**: `/actuator/chaosmonkey`, `/actuator/chaosmonkey/status`
- **Swagger UI**: `/swagger-ui.html`
- **API docs**: `/v3/api-docs`

## Project Structure

```
src/main/kotlin/com/jsh/chaosengineering/
├── ChaosEngineeringApplication.kt          # Main Spring Boot application
├── domain/                                 # Domain entities
│   ├── Product.kt, Order.kt, Inventory.kt, Payment.kt
├── service/                                # Business logic layer (Chaos targets)
│   ├── OrderService.kt                     # Order processing orchestration
│   ├── ProductService.kt, InventoryService.kt, PaymentService.kt
├── controller/                             # REST API endpoints
│   ├── OrderController.kt, ProductController.kt
│   └── ChaosTestController.kt              # Chaos engineering API
├── client/                                 # External service clients (with circuit breakers)
│   ├── InventoryClient.kt, PaymentClient.kt
└── config/                                 # Configuration classes
    └── SwaggerConfig.kt                    # API documentation setup
```

### Chaos Engineering Usage

#### Basic Chaos Monkey Control
1. **Start Application**:
   ```bash
   ./gradlew bootRun
   ```

2. **Enable Chaos Monkey** (via Actuator):
   ```bash
   curl -X POST http://localhost:8080/actuator/chaosmonkey/enable
   ```

3. **Configure Assaults**:
   ```bash
   curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
     -H "Content-Type: application/json" \
     -d '{"level":8, "latencyRangeStart":2000, "latencyRangeEnd":5000}'
   ```

#### Advanced Chaos Testing (via Custom API)
The ChaosTestController provides comprehensive chaos testing scenarios:

- **Enable all chaos tools**: `POST /api/chaos/enable-all`
- **Black Friday scenario**: `POST /api/chaos/scenario/black-friday`
- **Disaster recovery**: `POST /api/chaos/scenario/disaster-recovery`
- **Gradual degradation**: `POST /api/chaos/scenario/gradual-degradation`

#### Monitoring
- **Chaos status**: `/api/chaos/status`
- **Health endpoints**: `/actuator/health`
- **Swagger UI**: `/swagger-ui.html` for interactive API testing
- **Prometheus metrics**: `/actuator/prometheus`