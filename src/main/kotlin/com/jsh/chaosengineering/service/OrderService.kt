package com.jsh.chaosengineering.service

import com.jsh.chaosengineering.domain.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class OrderService(
    private val inventoryService: InventoryService,
    private val paymentService: PaymentService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // In-memory 주문 데이터
    private val orders = mutableMapOf<String, Order>()

    fun createOrder(
        userId: String,
        items: List<OrderItem>,
        shippingAddress: String,
        paymentMethod: PaymentMethod
    ): Order {
        logger.info("Creating order for user: $userId with ${items.size} items")

        val orderId = "ORD-${UUID.randomUUID()}"

        // 1. 재고 확인
        items.forEach { item ->
            val inventoryCheck = inventoryService.checkInventory(item.productId, item.quantity)
            if (!inventoryCheck.available) {
                logger.error("Insufficient inventory for product ${item.productId}")
                throw RuntimeException("재고 부족: ${item.productName}")
            }
        }

        // 2. 재고 예약
        items.forEach { item ->
            val reserved = inventoryService.reserveInventory(item.productId, item.quantity)
            if (!reserved) {
                logger.error("Failed to reserve inventory for product ${item.productId}")
                throw RuntimeException("재고 예약 실패: ${item.productName}")
            }
        }

        // 3. 총 금액 계산
        val totalAmount = items.sumOf { it.unitPrice.multiply(BigDecimal(it.quantity)) }

        // 4. 주문 생성
        val order = Order(
            id = orderId,
            userId = userId,
            items = items,
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
            shippingAddress = shippingAddress,
            createdAt = LocalDateTime.now()
        )

        orders[order.id] = order

        // 5. 결제 처리
        try {
            val paymentRequest = PaymentRequest(
                orderId = orderId,
                amount = totalAmount,
                method = paymentMethod
            )

            val paymentResult = paymentService.processPayment(paymentRequest)

            if (paymentResult.success) {
                updateOrderStatus(orderId, OrderStatus.PAID)
                logger.info("Order $orderId successfully created and paid")
            } else {
                updateOrderStatus(orderId, OrderStatus.FAILED)
                // 재고 롤백
                items.forEach { item ->
                    inventoryService.releaseInventory(item.productId, item.quantity)
                }
                throw RuntimeException("결제 실패: ${paymentResult.message}")
            }
        } catch (e: Exception) {
            logger.error("Payment processing failed for order $orderId", e)
            updateOrderStatus(orderId, OrderStatus.FAILED)
            // 재고 롤백
            items.forEach { item ->
                inventoryService.releaseInventory(item.productId, item.quantity)
            }
            throw e
        }

        return orders[orderId]!!
    }

    fun getOrder(orderId: String): Order? {
        logger.info("Fetching order: $orderId")
        return orders[orderId]
    }

    fun getUserOrders(userId: String): List<Order> {
        logger.info("Fetching orders for user: $userId")
        return orders.values.filter { it.userId == userId }
    }

    fun getAllOrders(): List<Order> {
        logger.info("Fetching all orders")
        return orders.values.toList()
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus): Boolean {
        logger.info("Updating order $orderId status to $status")
        val order = orders[orderId] ?: return false

        orders[orderId] = order.copy(status = status)
        return true
    }

    fun cancelOrder(orderId: String): Boolean {
        logger.info("Cancelling order: $orderId")
        val order = orders[orderId] ?: return false

        if (order.status in listOf(OrderStatus.SHIPPED, OrderStatus.DELIVERED)) {
            logger.warn("Cannot cancel order $orderId in status ${order.status}")
            return false
        }

        // 재고 반환
        order.items.forEach { item ->
            inventoryService.releaseInventory(item.productId, item.quantity)
        }

        updateOrderStatus(orderId, OrderStatus.CANCELLED)
        return true
    }
}