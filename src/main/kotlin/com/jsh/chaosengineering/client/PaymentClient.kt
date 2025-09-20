package com.jsh.chaosengineering.client

import com.jsh.chaosengineering.domain.PaymentRequest
import com.jsh.chaosengineering.domain.PaymentResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

@Component
class PaymentClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun processPayment(request: PaymentRequest): PaymentResult {
        logger.info("Processing payment for order: ${request.orderId}, amount: ${request.amount}, method: ${request.method}")

        // 처리 시간 시뮬레이션 (100ms ~ 500ms)
        Thread.sleep(Random.nextLong(100, 500))

        // Mock 응답 - 85% 확률로 결제 성공
        val success = Random.nextDouble() > 0.15

        return if (success) {
            val transactionId = "TXN-${UUID.randomUUID()}"
            logger.info("Payment successful for order ${request.orderId}, transaction: $transactionId")

            PaymentResult(
                success = true,
                transactionId = transactionId,
                message = "결제가 성공적으로 처리되었습니다.",
                processedAt = LocalDateTime.now()
            )
        } else {
            val errorMessages = listOf(
                "카드 잔액 부족",
                "카드 유효기간 만료",
                "결제 한도 초과",
                "은행 시스템 오류",
                "네트워크 타임아웃"
            )
            val errorMessage = errorMessages.random()

            logger.error("Payment failed for order ${request.orderId}: $errorMessage")

            PaymentResult(
                success = false,
                transactionId = null,
                message = errorMessage,
                processedAt = LocalDateTime.now()
            )
        }
    }

    fun refundPayment(transactionId: String, amount: java.math.BigDecimal): PaymentResult {
        logger.info("Processing refund for transaction: $transactionId, amount: $amount")

        // Mock 응답 - 95% 확률로 환불 성공
        val success = Random.nextDouble() > 0.05

        return PaymentResult(
            success = success,
            transactionId = if (success) "REFUND-${UUID.randomUUID()}" else null,
            message = if (success) "환불이 성공적으로 처리되었습니다." else "환불 처리 실패",
            processedAt = LocalDateTime.now()
        )
    }

    fun checkPaymentStatus(transactionId: String): String {
        logger.info("Checking payment status for transaction: $transactionId")

        val statuses = listOf("SUCCESS", "PENDING", "FAILED", "PROCESSING")
        return statuses.random()
    }
}