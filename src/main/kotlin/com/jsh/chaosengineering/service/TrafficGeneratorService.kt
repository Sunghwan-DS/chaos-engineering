package com.jsh.chaosengineering.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

data class TrafficConfig(
    val baseIntervalMs: Long = 1000,
    val variationPercent: Int = 50,
    val burstProbability: Double = 0.1,
    val quietProbability: Double = 0.05,
    val burstCount: Int = 5,
    val quietDurationMs: Long = 5000,
    val targetEndpoints: List<String> = listOf(
        "/api/products"
    )
)

data class TrafficStats(
    val isRunning: Boolean = false,
    val totalRequests: Long = 0,
    val successRequests: Long = 0,
    val failedRequests: Long = 0,
    val averageResponseTimeMs: Double = 0.0,
    val currentRpsEstimate: Double = 0.0,
    val startTime: LocalDateTime? = null,
    val lastRequestTime: LocalDateTime? = null
)

@Service
class TrafficGeneratorService {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate()

    private val executor = Executors.newSingleThreadExecutor()
    private val isRunning = AtomicBoolean(false)
    private var currentConfig = TrafficConfig()

    // 통계
    private val totalRequests = AtomicLong(0)
    private val successRequests = AtomicLong(0)
    private val failedRequests = AtomicLong(0)
    private val totalResponseTime = AtomicLong(0)
    private var startTime: LocalDateTime? = null
    private var lastRequestTime: LocalDateTime? = null

    // RPS 계산용
    private val requestTimestamps = ConcurrentLinkedQueue<Long>()

    fun startTraffic(config: TrafficConfig): Boolean {
        if (isRunning.compareAndSet(false, true)) {
            currentConfig = config
            resetStats()
            startTime = LocalDateTime.now()

            executor.submit {
                generateTraffic()
            }

            logger.info("Traffic generator started with config: $config")
            return true
        }
        return false
    }

    fun stopTraffic(): Boolean {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Traffic generator stopped")
            return true
        }
        return false
    }

    fun getStats(): TrafficStats {
        val currentTime = System.currentTimeMillis()

        // 최근 1분간의 요청으로 RPS 계산
        val oneMinuteAgo = currentTime - 60000
        requestTimestamps.removeIf { it < oneMinuteAgo }
        val currentRps = requestTimestamps.size / 60.0

        return TrafficStats(
            isRunning = isRunning.get(),
            totalRequests = totalRequests.get(),
            successRequests = successRequests.get(),
            failedRequests = failedRequests.get(),
            averageResponseTimeMs = if (totalRequests.get() > 0)
                totalResponseTime.get().toDouble() / totalRequests.get()
            else 0.0,
            currentRpsEstimate = currentRps,
            startTime = startTime,
            lastRequestTime = lastRequestTime
        )
    }

    fun updateConfig(config: TrafficConfig) {
        currentConfig = config
        logger.info("Traffic generator config updated: $config")
    }

    private fun generateTraffic() {
        logger.info("Starting traffic generation loop")

        while (isRunning.get()) {
            try {
                val interval = calculateNextInterval()

                if (shouldEnterQuietPeriod()) {
                    logger.debug("Entering quiet period for ${currentConfig.quietDurationMs}ms")
                    Thread.sleep(currentConfig.quietDurationMs)
                    continue
                }

                if (shouldBurst()) {
                    logger.debug("Starting burst of ${currentConfig.burstCount} requests")
                    repeat(currentConfig.burstCount) {
                        if (isRunning.get()) {
                            makeRequest()
                            Thread.sleep(50) // 버스트 시 짧은 간격
                        }
                    }
                } else {
                    makeRequest()
                }

                Thread.sleep(interval)

            } catch (e: InterruptedException) {
                logger.info("Traffic generation interrupted")
                break
            } catch (e: Exception) {
                logger.error("Error in traffic generation: ${e.message}", e)
                Thread.sleep(1000) // 에러 시 1초 대기
            }
        }

        logger.info("Traffic generation loop ended")
    }

    private fun calculateNextInterval(): Long {
        val variation = (currentConfig.baseIntervalMs * currentConfig.variationPercent / 100.0).toLong()
        val randomVariation = Random.nextLong(-variation, variation + 1)
        return (currentConfig.baseIntervalMs + randomVariation).coerceAtLeast(100)
    }

    private fun shouldEnterQuietPeriod(): Boolean {
        return Random.nextDouble() < currentConfig.quietProbability
    }

    private fun shouldBurst(): Boolean {
        return Random.nextDouble() < currentConfig.burstProbability
    }

    private fun makeRequest() {
        val endpoint = currentConfig.targetEndpoints.random()
        val startTime = System.currentTimeMillis()
        val url = "http://localhost:8080$endpoint"

        try {
            val response = restTemplate.getForEntity(url, String::class.java)
            val responseTime = System.currentTimeMillis() - startTime

            if (response.statusCode.is2xxSuccessful) {
                recordSuccess(responseTime)
                logger.debug("Request to $endpoint successful (${responseTime}ms)")
            } else {
                recordFailure(responseTime)
                logger.debug("Request to $endpoint failed with status: ${response.statusCode}")
            }

        } catch (e: Exception) {
            val responseTime = System.currentTimeMillis() - startTime
            recordFailure(responseTime)
            logger.debug("Request to $endpoint failed: ${e.message}")
        }
    }

    private fun recordSuccess(responseTimeMs: Long) {
        totalRequests.incrementAndGet()
        successRequests.incrementAndGet()
        totalResponseTime.addAndGet(responseTimeMs)
        lastRequestTime = LocalDateTime.now()
        requestTimestamps.offer(System.currentTimeMillis())
    }

    private fun recordFailure(responseTimeMs: Long) {
        totalRequests.incrementAndGet()
        failedRequests.incrementAndGet()
        totalResponseTime.addAndGet(responseTimeMs)
        lastRequestTime = LocalDateTime.now()
        requestTimestamps.offer(System.currentTimeMillis())
    }

    private fun resetStats() {
        totalRequests.set(0)
        successRequests.set(0)
        failedRequests.set(0)
        totalResponseTime.set(0)
        lastRequestTime = null
        requestTimestamps.clear()
    }
}
