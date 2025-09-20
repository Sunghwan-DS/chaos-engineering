package com.jsh.chaosengineering.service

import com.jsh.chaosengineering.client.InventoryClient
import com.jsh.chaosengineering.domain.InventoryCheckResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class InventoryService(
    private val inventoryClient: InventoryClient
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun checkInventory(productId: String, quantity: Int): InventoryCheckResult {
        logger.info("Checking inventory for product: $productId, quantity: $quantity")

        return try {
            inventoryClient.checkAvailability(productId, quantity)
        } catch (e: Exception) {
            logger.error("Error checking inventory for product $productId", e)
            InventoryCheckResult(
                productId = productId,
                available = false,
                availableQuantity = 0,
                message = "재고 확인 중 오류 발생: ${e.message}"
            )
        }
    }

    fun reserveInventory(productId: String, quantity: Int): Boolean {
        logger.info("Reserving inventory for product: $productId, quantity: $quantity")

        return try {
            inventoryClient.reserveInventory(productId, quantity)
        } catch (e: Exception) {
            logger.error("Error reserving inventory for product $productId", e)
            false
        }
    }

    fun releaseInventory(productId: String, quantity: Int): Boolean {
        logger.info("Releasing inventory for product: $productId, quantity: $quantity")

        return try {
            inventoryClient.releaseInventory(productId, quantity)
        } catch (e: Exception) {
            logger.error("Error releasing inventory for product $productId", e)
            false
        }
    }

    fun bulkCheckInventory(productQuantities: Map<String, Int>): Map<String, InventoryCheckResult> {
        logger.info("Bulk checking inventory for ${productQuantities.size} products")

        return productQuantities.map { (productId, quantity) ->
            productId to checkInventory(productId, quantity)
        }.toMap()
    }

    fun getInventoryStatus(productId: String): InventoryCheckResult {
        logger.info("Getting inventory status for product: $productId")

        return checkInventory(productId, 0)
    }
}