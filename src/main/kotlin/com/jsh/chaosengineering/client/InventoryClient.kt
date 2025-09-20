package com.jsh.chaosengineering.client

import com.jsh.chaosengineering.domain.InventoryCheckResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class InventoryClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun checkAvailability(productId: String, quantity: Int): InventoryCheckResult {
        logger.info("Checking inventory for product: $productId, quantity: $quantity")

        // Mock 응답 - 실제로는 외부 인벤토리 시스템 호출
        // 90% 확률로 재고 있음, 10% 확률로 재고 부족
        val isAvailable = Random.nextDouble() > 0.1
        val availableQuantity = if (isAvailable) Random.nextInt(100, 1000) else Random.nextInt(0, quantity)

        return InventoryCheckResult(
            productId = productId,
            available = isAvailable && availableQuantity >= quantity,
            availableQuantity = availableQuantity,
            message = if (isAvailable && availableQuantity >= quantity) {
                "재고 확인 완료"
            } else {
                "재고 부족 (요청: $quantity, 가용: $availableQuantity)"
            }
        )
    }

    fun reserveInventory(productId: String, quantity: Int): Boolean {
        logger.info("Reserving inventory for product: $productId, quantity: $quantity")

        // Mock 응답 - 95% 확률로 예약 성공
        val success = Random.nextDouble() > 0.05

        if (success) {
            logger.info("Successfully reserved $quantity units of product $productId")
        } else {
            logger.error("Failed to reserve inventory for product $productId")
        }

        return success
    }

    fun releaseInventory(productId: String, quantity: Int): Boolean {
        logger.info("Releasing inventory for product: $productId, quantity: $quantity")

        // Mock 응답 - 항상 성공
        return true
    }
}