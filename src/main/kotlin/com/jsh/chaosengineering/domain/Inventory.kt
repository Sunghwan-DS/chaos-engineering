package com.jsh.chaosengineering.domain

import java.time.LocalDateTime

data class Inventory(
    val productId: String,
    val warehouseId: String,
    val availableQuantity: Int,
    val reservedQuantity: Int,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

data class InventoryCheckResult(
    val productId: String,
    val available: Boolean,
    val availableQuantity: Int,
    val message: String? = null
)