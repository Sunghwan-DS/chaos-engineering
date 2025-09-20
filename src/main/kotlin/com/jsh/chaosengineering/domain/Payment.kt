package com.jsh.chaosengineering.domain

import java.math.BigDecimal
import java.time.LocalDateTime

data class Payment(
    val id: String,
    val orderId: String,
    val amount: BigDecimal,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val transactionId: String? = null,
    val processedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class PaymentRequest(
    val orderId: String,
    val amount: BigDecimal,
    val method: PaymentMethod,
    val cardNumber: String? = null,
    val cvv: String? = null
)

data class PaymentResult(
    val success: Boolean,
    val transactionId: String? = null,
    val message: String,
    val processedAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    BANK_TRANSFER,
    CASH_ON_DELIVERY
}

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUNDED
}