package com.jsh.chaosengineering.controller

import com.jsh.chaosengineering.domain.Product
import com.jsh.chaosengineering.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Management", description = "상품 관리 API - 카오스 테스트 대상")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    @Operation(summary = "모든 상품 조회", description = "전체 상품 목록을 조회합니다.")
    fun getAllProducts(): ResponseEntity<List<Product>> {
        return ResponseEntity.ok(productService.getAllProducts())
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: String): ResponseEntity<Product> {
        val product = productService.getProductById(id)
        return if (product != null) {
            ResponseEntity.ok(product)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/category/{category}")
    fun getProductsByCategory(@PathVariable category: String): ResponseEntity<List<Product>> {
        return ResponseEntity.ok(productService.getProductsByCategory(category))
    }

    @GetMapping("/search")
    fun searchProducts(@RequestParam keyword: String): ResponseEntity<List<Product>> {
        return ResponseEntity.ok(productService.searchProducts(keyword))
    }

    @PostMapping
    fun createProduct(@RequestBody product: Product): ResponseEntity<Product> {
        val createdProduct = productService.createProduct(product)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct)
    }

    @PutMapping("/{id}/stock")
    fun updateStock(
        @PathVariable id: String,
        @RequestParam quantity: Int
    ): ResponseEntity<String> {
        val updated = productService.updateStock(id, quantity)
        return if (updated) {
            ResponseEntity.ok("재고가 업데이트되었습니다.")
        } else {
            ResponseEntity.notFound().build()
        }
    }
}