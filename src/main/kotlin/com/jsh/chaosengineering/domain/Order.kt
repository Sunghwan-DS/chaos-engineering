package com.jsh.chaosengineering.domain

import java.math.BigDecimal
import java.time.LocalDateTime

data class Order(
    val id: String,
    val userId: String,
    val items: List<OrderItem>,
    val totalAmount: BigDecimal,
    val status: OrderStatus,
    val shippingAddress: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_PROCESSING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED
}