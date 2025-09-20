package com.jsh.chaosengineering.controller

import com.jsh.chaosengineering.domain.*
import com.jsh.chaosengineering.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class CreateOrderRequest(
    val userId: String,
    val items: List<OrderItem>,
    val shippingAddress: String,
    val paymentMethod: PaymentMethod
)

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "주문 관리 API - 카오스 테스트 대상")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    @Operation(summary = "주문 생성", description = "새 주문을 생성합니다. 카오스 테스트를 통해 장애 상황에서의 주문 처리를 확인할 수 있습니다.")
    fun createOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<Any> {
        return try {
            val order = orderService.createOrder(
                userId = request.userId,
                items = request.items,
                shippingAddress = request.shippingAddress,
                paymentMethod = request.paymentMethod
            )
            ResponseEntity.status(HttpStatus.CREATED).body(order)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("error" to e.message)
            )
        }
    }

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: String): ResponseEntity<Order> {
        val order = orderService.getOrder(orderId)
        return if (order != null) {
            ResponseEntity.ok(order)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    fun getAllOrders(): ResponseEntity<List<Order>> {
        return ResponseEntity.ok(orderService.getAllOrders())
    }

    @GetMapping("/user/{userId}")
    fun getUserOrders(@PathVariable userId: String): ResponseEntity<List<Order>> {
        return ResponseEntity.ok(orderService.getUserOrders(userId))
    }

    @PutMapping("/{orderId}/status")
    fun updateOrderStatus(
        @PathVariable orderId: String,
        @RequestParam status: OrderStatus
    ): ResponseEntity<String> {
        val updated = orderService.updateOrderStatus(orderId, status)
        return if (updated) {
            ResponseEntity.ok("주문 상태가 업데이트되었습니다.")
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{orderId}/cancel")
    fun cancelOrder(@PathVariable orderId: String): ResponseEntity<Any> {
        return try {
            val cancelled = orderService.cancelOrder(orderId)
            if (cancelled) {
                ResponseEntity.ok(mapOf("message" to "주문이 취소되었습니다."))
            } else {
                ResponseEntity.badRequest().body(
                    mapOf("error" to "주문을 취소할 수 없습니다.")
                )
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf("error" to e.message)
            )
        }
    }
}