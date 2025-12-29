package com.gearvn.ecommerce.controller

import com.gearvn.ecommerce.dto.ApiResponse
import com.gearvn.ecommerce.dto.PageResponse
import com.gearvn.ecommerce.dto.ProductCreateRequest
import com.gearvn.ecommerce.dto.ProductResponse
import com.gearvn.ecommerce.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management endpoints")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve paginated list of active products")
    fun getAllProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<ProductResponse>> {
        val pageable = PageRequest.of(page, size)
        val productPage = productService.getAllProducts(pageable)
        
        return ResponseEntity.ok(
            PageResponse(
                content = productPage.content,
                page = productPage.number,
                size = productPage.size,
                totalElements = productPage.totalElements,
                totalPages = productPage.totalPages,
                last = productPage.isLast
            )
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve product details by ID")
    fun getProductById(@PathVariable id: Long): ResponseEntity<ApiResponse<ProductResponse>> {
        val product = productService.getProductById(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Product retrieved successfully",
                data = product
            )
        )
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by keyword")
    fun searchProducts(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<ProductResponse>> {
        val pageable = PageRequest.of(page, size)
        val productPage = productService.searchProducts(keyword, pageable)
        
        return ResponseEntity.ok(
            PageResponse(
                content = productPage.content,
                page = productPage.number,
                size = productPage.size,
                totalElements = productPage.totalElements,
                totalPages = productPage.totalPages,
                last = productPage.isLast
            )
        )
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieve products by category ID")
    fun getProductsByCategory(
        @PathVariable categoryId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<ProductResponse>> {
        val pageable = PageRequest.of(page, size)
        val productPage = productService.getProductsByCategory(categoryId, pageable)
        
        return ResponseEntity.ok(
            PageResponse(
                content = productPage.content,
                page = productPage.number,
                size = productPage.size,
                totalElements = productPage.totalElements,
                totalPages = productPage.totalPages,
                last = productPage.isLast
            )
        )
    }
}
