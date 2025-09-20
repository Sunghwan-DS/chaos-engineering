package com.jsh.chaosengineering.service

import com.jsh.chaosengineering.domain.Product
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class ProductService {

    private val logger = LoggerFactory.getLogger(javaClass)

    // In-memory 상품 데이터
    private val products = mutableMapOf<String, Product>().apply {
        // 샘플 상품 데이터 초기화
        val sampleProducts = listOf(
            Product(
                id = "PROD-001",
                name = "노트북 Pro Max",
                description = "고성능 프로세서와 대용량 메모리를 갖춘 프리미엄 노트북",
                price = BigDecimal("2500000"),
                stockQuantity = 50,
                category = "전자제품"
            ),
            Product(
                id = "PROD-002",
                name = "무선 이어폰 Ultra",
                description = "노이즈 캔슬링 기능이 탑재된 프리미엄 무선 이어폰",
                price = BigDecimal("350000"),
                stockQuantity = 200,
                category = "전자제품"
            ),
            Product(
                id = "PROD-003",
                name = "스마트워치 5",
                description = "건강 관리와 피트니스 트래킹이 가능한 스마트워치",
                price = BigDecimal("450000"),
                stockQuantity = 150,
                category = "웨어러블"
            ),
            Product(
                id = "PROD-004",
                name = "4K 웹캠",
                description = "화상회의를 위한 고화질 4K 웹캠",
                price = BigDecimal("180000"),
                stockQuantity = 100,
                category = "전자제품"
            ),
            Product(
                id = "PROD-005",
                name = "게이밍 키보드",
                description = "RGB 백라이트가 탑재된 기계식 게이밍 키보드",
                price = BigDecimal("150000"),
                stockQuantity = 80,
                category = "게이밍"
            )
        )

        sampleProducts.forEach { product ->
            this[product.id] = product
        }
    }

    fun getAllProducts(): List<Product> {
        logger.info("Fetching all products")
        return products.values.toList()
    }

    fun getProductById(id: String): Product? {
        logger.info("Fetching product with id: $id")
        return products[id]
    }

    fun getProductsByCategory(category: String): List<Product> {
        logger.info("Fetching products in category: $category")
        return products.values.filter { it.category == category }
    }

    fun searchProducts(keyword: String): List<Product> {
        logger.info("Searching products with keyword: $keyword")
        val lowerKeyword = keyword.lowercase()
        return products.values.filter {
            it.name.lowercase().contains(lowerKeyword) ||
            it.description.lowercase().contains(lowerKeyword) ||
            it.category.lowercase().contains(lowerKeyword)
        }
    }

    fun createProduct(product: Product): Product {
        logger.info("Creating new product: ${product.name}")
        val newProduct = product.copy(
            id = "PROD-${UUID.randomUUID()}",
            createdAt = LocalDateTime.now()
        )
        products[newProduct.id] = newProduct
        return newProduct
    }

    fun updateStock(productId: String, quantity: Int): Boolean {
        logger.info("Updating stock for product $productId: $quantity")
        val product = products[productId] ?: return false

        products[productId] = product.copy(stockQuantity = product.stockQuantity + quantity)
        return true
    }
}