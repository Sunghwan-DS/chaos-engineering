package com.jsh.chaosengineering.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

data class ChaosAssaultConfig(
    val level: Int = 5,
    val latencyRangeStart: Int = 1000,
    val latencyRangeEnd: Int = 3000,
    val latencyActive: Boolean = true,
    val exceptionsActive: Boolean = true
)

data class ChaosStatusResponse(
    val enabled: Boolean,
    val config: Map<String, Any>? = null
)

@RestController
@RequestMapping("/api/chaos")
@Tag(name = "Chaos Engineering", description = "Chaos Monkey 제어 API - Swagger UI에서 쉽게 카오스 테스트 실행")
class ChaosMonkeyController {

    private val restTemplate = RestTemplate()
    private val actuatorBaseUrl = "http://localhost:8080/actuator/chaosmonkey"

    @PostMapping("/enable")
    @Operation(
        summary = "🐵 Chaos Monkey 활성화",
        description = "시스템에 무작위 장애를 주입하는 Chaos Monkey를 활성화합니다. " +
                "활성화 후 @Service, @Controller, @Component 애노테이션이 붙은 클래스들의 메서드 호출 시 " +
                "설정된 확률에 따라 지연이나 예외가 발생합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Chaos Monkey 활성화 성공"),
            ApiResponse(responseCode = "500", description = "활성화 실패")
        ]
    )
    fun enableChaosMonkey(): ResponseEntity<Map<String, String>> {
        return try {
            restTemplate.postForEntity("$actuatorBaseUrl/enable", null, String::class.java)
            ResponseEntity.ok(mapOf("message" to "🐵 Chaos Monkey가 활성화되었습니다!", "status" to "enabled"))
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .body(mapOf("error" to "Chaos Monkey 활성화 실패: ${e.message}"))
        }
    }

    @PostMapping("/disable")
    @Operation(
        summary = "🛑 Chaos Monkey 비활성화",
        description = "현재 실행 중인 Chaos Monkey를 비활성화합니다. 비활성화 후에는 정상적인 서비스 동작이 보장됩니다."
    )
    fun disableChaosMonkey(): ResponseEntity<Map<String, String>> {
        return try {
            restTemplate.postForEntity("$actuatorBaseUrl/disable", null, String::class.java)
            ResponseEntity.ok(mapOf("message" to "🛑 Chaos Monkey가 비활성화되었습니다.", "status" to "disabled"))
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .body(mapOf("error" to "Chaos Monkey 비활성화 실패: ${e.message}"))
        }
    }

    @GetMapping("/status")
    @Operation(
        summary = "📊 Chaos Monkey 상태 조회",
        description = "현재 Chaos Monkey의 활성화 상태와 설정 정보를 조회합니다."
    )
    fun getChaosStatus(): ResponseEntity<Any> {
        return try {
            // chaos-monkey-spring-boot의 실제 엔드포인트 사용
            val configResponse = restTemplate.getForEntity("$actuatorBaseUrl", Map::class.java)
            val watchersResponse = restTemplate.getForEntity("$actuatorBaseUrl/watchers", Map::class.java)

            ResponseEntity.ok(mapOf(
                "enabled" to ((configResponse.body?.get("chaosMonkeyProperties") as? Map<*, *>)?.get("enabled") ?: false),
                "configuration" to configResponse.body,
                "watchers" to watchersResponse.body
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .body(mapOf("error" to "상태 조회 실패: ${e.message}"))
        }
    }

    @PutMapping("/config")
    @Operation(
        summary = "⚙️ Chaos Monkey 설정 변경",
        description = "Chaos Monkey의 공격 설정을 실시간으로 변경합니다. " +
                "level(강도), latencyRange(지연시간), 활성화할 공격 유형을 설정할 수 있습니다."
    )
    fun updateChaosConfig(
        @RequestBody
        @Parameter(description = "카오스 공격 설정")
        config: ChaosAssaultConfig
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val requestBody = mapOf(
                "level" to config.level,
                "latencyRangeStart" to config.latencyRangeStart,
                "latencyRangeEnd" to config.latencyRangeEnd,
                "latencyActive" to config.latencyActive,
                "exceptionsActive" to config.exceptionsActive
            )

            restTemplate.postForEntity("$actuatorBaseUrl/assaults", requestBody, String::class.java)

            ResponseEntity.ok(mapOf(
                "message" to "⚙️ Chaos Monkey 설정이 업데이트되었습니다.",
                "config" to requestBody
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .body(mapOf("error" to "설정 변경 실패: ${e.message}"))
        }
    }

    @PostMapping("/quick-test")
    @Operation(
        summary = "🚀 빠른 테스트 시작",
        description = "Chaos Monkey를 활성화하고 중간 강도의 설정으로 빠른 테스트를 시작합니다. " +
                "테스트 완료 후 비즈니스 API(주문 생성, 상품 조회 등)를 호출해보세요."
    )
    fun startQuickTest(): ResponseEntity<Map<String, Any>> {
        return try {
            // 1. 활성화
            restTemplate.postForEntity("$actuatorBaseUrl/enable", null, String::class.java)

            // 2. 중간 강도 설정
            val quickConfig = mapOf(
                "level" to 6,
                "latencyRangeStart" to 1500,
                "latencyRangeEnd" to 4000,
                "latencyActive" to true,
                "exceptionsActive" to true
            )
            restTemplate.postForEntity("$actuatorBaseUrl/assaults", quickConfig, String::class.java)

            ResponseEntity.ok(mapOf(
                "message" to "🚀 빠른 테스트가 시작되었습니다!",
                "instructions" to listOf(
                    "이제 주문 생성 API나 상품 조회 API를 여러 번 호출해보세요.",
                    "일부 요청에서 지연이나 오류가 발생할 것입니다.",
                    "테스트 완료 후 '/api/chaos/disable'로 비활성화하세요."
                ),
                "config" to quickConfig
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .body(mapOf("error" to "빠른 테스트 시작 실패: ${e.message}"))
        }
    }
}