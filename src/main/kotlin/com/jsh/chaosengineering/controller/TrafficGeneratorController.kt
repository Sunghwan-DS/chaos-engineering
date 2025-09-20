package com.jsh.chaosengineering.controller

import com.jsh.chaosengineering.service.TrafficConfig
import com.jsh.chaosengineering.service.TrafficGeneratorService
import com.jsh.chaosengineering.service.TrafficStats
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/traffic")
@Tag(name = "Traffic Generator", description = "유동적 트래픽 생성기 - 자연스러운 사용자 패턴 시뮬레이션")
class TrafficGeneratorController(
    private val trafficGeneratorService: TrafficGeneratorService
) {

    @PostMapping("/start")
    @Operation(
        summary = "🚀 트래픽 생성 시작",
        description = "설정된 패턴으로 /api/products 엔드포인트들에 유동적인 트래픽을 생성합니다. " +
                "실제 사용자와 유사한 패턴으로 랜덤 변동, 버스트, 조용한 구간을 포함합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "트래픽 생성 시작 성공"),
            ApiResponse(responseCode = "409", description = "이미 실행 중인 트래픽 생성기가 있음"),
            ApiResponse(responseCode = "500", description = "트래픽 생성 시작 실패")
        ]
    )
    fun startTraffic(
        @RequestBody(required = false)
        @Parameter(description = "트래픽 생성 설정 (선택사항, 기본값 사용 가능)")
        config: TrafficConfig?
    ): ResponseEntity<Map<String, Any>> {
        val actualConfig = config ?: TrafficConfig()

        return if (trafficGeneratorService.startTraffic(actualConfig)) {
            ResponseEntity.ok(mapOf(
                "message" to "🚀 트래픽 생성이 시작되었습니다!",
                "status" to "started",
                "config" to actualConfig,
                "instructions" to listOf(
                    "실시간 통계는 GET /api/traffic/status로 확인하세요",
                    "Grafana 대시보드에서 메트릭을 모니터링하세요",
                    "Chaos Monkey를 함께 활성화하면 복원력 테스트가 가능합니다"
                )
            ))
        } else {
            ResponseEntity.status(409).body(mapOf(
                "error" to "이미 실행 중인 트래픽 생성기가 있습니다",
                "suggestion" to "먼저 POST /api/traffic/stop으로 중지하세요"
            ))
        }
    }

    @PostMapping("/stop")
    @Operation(
        summary = "🛑 트래픽 생성 중지",
        description = "현재 실행 중인 트래픽 생성을 중지합니다. 통계는 유지됩니다."
    )
    fun stopTraffic(): ResponseEntity<Map<String, String>> {
        return if (trafficGeneratorService.stopTraffic()) {
            ResponseEntity.ok(mapOf(
                "message" to "🛑 트래픽 생성이 중지되었습니다",
                "status" to "stopped"
            ))
        } else {
            ResponseEntity.ok(mapOf(
                "message" to "실행 중인 트래픽 생성기가 없습니다",
                "status" to "already_stopped"
            ))
        }
    }

    @GetMapping("/status")
    @Operation(
        summary = "📊 트래픽 생성 상태 조회",
        description = "현재 트래픽 생성기의 실행 상태와 실시간 통계를 조회합니다. " +
                "총 요청 수, 성공/실패율, 평균 응답시간, 현재 RPS 등을 확인할 수 있습니다."
    )
    fun getTrafficStatus(): ResponseEntity<TrafficStats> {
        return ResponseEntity.ok(trafficGeneratorService.getStats())
    }

    @PutMapping("/config")
    @Operation(
        summary = "⚙️ 트래픽 생성 설정 변경",
        description = "실행 중인 트래픽 생성기의 설정을 실시간으로 변경합니다. " +
                "호출 주기, 변동 범위, 버스트 확률 등을 조정할 수 있습니다."
    )
    fun updateTrafficConfig(
        @RequestBody
        @Parameter(description = "새로운 트래픽 생성 설정")
        config: TrafficConfig
    ): ResponseEntity<Map<String, Any>> {
        trafficGeneratorService.updateConfig(config)

        return ResponseEntity.ok(mapOf(
            "message" to "⚙️ 트래픽 생성 설정이 업데이트되었습니다",
            "config" to config
        ))
    }

    @PostMapping("/quick-start")
    @Operation(
        summary = "⚡ 빠른 시작",
        description = "미리 정의된 설정으로 트래픽 생성을 빠르게 시작합니다. " +
                "적당한 부하와 변동성을 가진 설정을 사용합니다."
    )
    fun quickStart(): ResponseEntity<Map<String, Any>> {
        val quickConfig = TrafficConfig(
            baseIntervalMs = 800,
            variationPercent = 60,
            burstProbability = 0.15,
            quietProbability = 0.08,
            burstCount = 3
        )

        return if (trafficGeneratorService.startTraffic(quickConfig)) {
            ResponseEntity.ok(mapOf(
                "message" to "⚡ 빠른 시작으로 트래픽 생성이 시작되었습니다!",
                "status" to "started",
                "config" to quickConfig,
                "pattern" to "적당한 부하 + 높은 변동성"
            ))
        } else {
            ResponseEntity.status(409).body(mapOf(
                "error" to "이미 실행 중인 트래픽 생성기가 있습니다"
            ))
        }
    }

    @PostMapping("/stress-test")
    @Operation(
        summary = "💥 스트레스 테스트",
        description = "높은 부하와 빈번한 버스트를 가진 스트레스 테스트용 트래픽을 생성합니다."
    )
    fun stressTest(): ResponseEntity<Map<String, Any>> {
        val stressConfig = TrafficConfig(
            baseIntervalMs = 300,
            variationPercent = 40,
            burstProbability = 0.25,
            quietProbability = 0.03,
            burstCount = 8
        )

        return if (trafficGeneratorService.startTraffic(stressConfig)) {
            ResponseEntity.ok(mapOf(
                "message" to "💥 스트레스 테스트가 시작되었습니다!",
                "status" to "started",
                "config" to stressConfig,
                "pattern" to "높은 부하 + 빈번한 버스트",
                "warning" to "시스템 리소스를 주의깊게 모니터링하세요"
            ))
        } else {
            ResponseEntity.status(409).body(mapOf(
                "error" to "이미 실행 중인 트래픽 생성기가 있습니다"
            ))
        }
    }
}