package com.jsh.chaosengineering.service

import com.jsh.chaosengineering.client.PaymentClient
import com.jsh.chaosengineering.domain.Payment
import com.jsh.chaosengineering.domain.PaymentRequest
import com.jsh.chaosengineering.domain.PaymentResult
import com.jsh.chaosengineering.domain.PaymentStatus
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class PaymentService(
    private val paymentClient: PaymentClient
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // In-memory 결제 데이터
    private val payments = mutableMapOf<String, Payment>()

    @CircuitBreaker(name = "payment-service", fallbackMethod = "processPaymentFallback")
    fun processPayment(request: PaymentRequest): PaymentResult {
        logger.info("Processing payment for order: ${request.orderId}")

        val paymentId = "PAY-${UUID.randomUUID()}"

        // 결제 정보 저장
        val payment = Payment(
            id = paymentId,
            orderId = request.orderId,
            amount = request.amount,
            method = request.method,
            status = PaymentStatus.PROCESSING,
            createdAt = LocalDateTime.now()
        )
        payments[paymentId] = payment

        // 외부 결제 시스템 호출
        val result = paymentClient.processPayment(request)

        // 결제 상태 업데이트
        payments[paymentId] = payment.copy(
            status = if (result.success) PaymentStatus.SUCCESS else PaymentStatus.FAILED,
            transactionId = result.transactionId,
            processedAt = result.processedAt
        )

        return result
    }

    fun processPaymentFallback(request: PaymentRequest, exception: Exception): PaymentResult {
        logger.error("Payment service circuit breaker activated for order: ${request.orderId}", exception)

        return PaymentResult(
            success = false,
            transactionId = null,
            message = "결제 시스템 일시 장애로 인해 처리할 수 없습니다. 잠시 후 다시 시도해주세요.",
            processedAt = LocalDateTime.now()
        )
    }

    fun refundPayment(orderId: String, amount: BigDecimal, reason: String): PaymentResult {
        logger.info("Processing refund for order: $orderId, amount: $amount, reason: $reason")

        val payment = payments.values.find { it.orderId == orderId && it.status == PaymentStatus.SUCCESS }
            ?: throw RuntimeException("결제 정보를 찾을 수 없습니다.")

        val result = paymentClient.refundPayment(payment.transactionId!!, amount)

        if (result.success) {
            // 환불 결제 정보 생성
            val refundPayment = Payment(
                id = "REFUND-${UUID.randomUUID()}",
                orderId = orderId,
                amount = amount.negate(), // 음수로 저장
                method = payment.method,
                status = PaymentStatus.REFUNDED,
                transactionId = result.transactionId,
                processedAt = result.processedAt,
                createdAt = LocalDateTime.now()
            )
            payments[refundPayment.id] = refundPayment
        }

        return result
    }

    fun getPayment(paymentId: String): Payment? {
        logger.info("Fetching payment: $paymentId")
        return payments[paymentId]
    }

    fun getPaymentsByOrder(orderId: String): List<Payment> {
        logger.info("Fetching payments for order: $orderId")
        return payments.values.filter { it.orderId == orderId }
    }

    fun checkPaymentStatus(transactionId: String): String {
        logger.info("Checking payment status for transaction: $transactionId")
        return paymentClient.checkPaymentStatus(transactionId)
    }

    fun getAllPayments(): List<Payment> {
        logger.info("Fetching all payments")
        return payments.values.toList()
    }
}