package com.jsh.chaosengineering.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Chaos Engineering API")
                    .description(
                        """
                        # Chaos Engineering 실험 플랫폼 API

                        Spring Boot + Kotlin 기반의 Chaos Engineering 실험 애플리케이션입니다.
                        E-commerce 도메인 모델을 통해 실제 운영 환경과 유사한 장애 시나리오를 테스트할 수 있습니다.

                        ## 🏗️ 주요 기능

                        ### 🐵 Chaos Monkey (chaos-monkey-spring-boot)
                        - **지연 주입**: 1000-8000ms 범위의 응답 지연 시뮬레이션
                        - **예외 발생**: 무작위 RuntimeException 주입으로 오류 처리 테스트
                        - **선택적 감시**: @Service, @Controller, @Component 클래스 대상
                        - **실시간 제어**: 애플리케이션 재시작 없이 설정 변경

                        ### 💳 비즈니스 도메인
                        - **상품 관리**: 카탈로그, 검색, 재고 관리
                        - **주문 처리**: 전체 주문 플로우 (재고 확인 → 결제 → 완료)
                        - **결제 시스템**: Circuit Breaker 패턴 적용
                        - **재고 관리**: 예약/해제 로직으로 동시성 테스트

                        ## 🚀 빠른 시작 가이드

                        ### 1단계: Chaos Monkey 활성화
                        - `POST /api/chaos/enable` 또는 `POST /api/chaos/quick-test` 사용

                        ### 2단계: 비즈니스 API 테스트
                        - 주문 생성, 상품 조회 등을 반복 호출
                        - 일부 요청에서 지연이나 오류 발생 확인

                        ### 3단계: 모니터링
                        - `GET /api/chaos/status`로 현재 상태 확인
                        - `/actuator/health`에서 Circuit Breaker 상태 관찰

                        ### 4단계: 설정 조정
                        - `PUT /api/chaos/config`로 공격 강도/유형 변경
                        - level(1-10), 지연시간, 활성화할 공격 선택

                        ## 📊 관찰 가능성
                        - **Swagger UI**: 모든 API 대화형 테스트
                        - **Actuator**: 헬스체크, 메트릭, Chaos Monkey 상태
                        - **Prometheus**: 성능 메트릭 수집
                        """.trimIndent()
                    )
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Chaos Engineering Team")
                            .email("chaos@company.com")
                            .url("https://github.com/chaos-engineering")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Development Server"),
                    Server()
                        .url("https://chaos-engineering.example.com")
                        .description("Production Server")
                )
            )
    }
}