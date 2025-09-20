package com.jsh.chaosengineering.domain

import java.math.BigDecimal
import java.time.LocalDateTime

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val stockQuantity: Int,
    val category: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)