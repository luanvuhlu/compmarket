package com.luanvv.ecommerce.controller

import com.luanvv.ecommerce.dto.ApiResponse
import com.luanvv.ecommerce.dto.ProductCreateRequest
import com.luanvv.ecommerce.dto.ProductResponse
import com.luanvv.ecommerce.service.ProductIndexService
import com.luanvv.ecommerce.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/products")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Admin - Products", description = "Admin product management endpoints")
class AdminController(
    private val productService: ProductService
) {
    @Autowired(required = false)
    private val productIndexService: ProductIndexService? = null

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product (Admin only)")
    fun createProduct(@RequestBody request: ProductCreateRequest): ResponseEntity<ApiResponse<ProductResponse>> {
        val product = productService.createProduct(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                message = "Product created successfully",
                data = product
            )
        )
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product (Admin only)")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody request: ProductCreateRequest
    ): ResponseEntity<ApiResponse<ProductResponse>> {
        val product = productService.updateProduct(id, request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Product updated successfully",
                data = product
            )
        )
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Soft delete a product (Admin only)")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<ApiResponse<Void>> {
        productService.deleteProduct(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Product deleted successfully",
                data = null
            )
        )
    }

    @PostMapping("/reindex")
    @Operation(
        summary = "Reindex all products to Elasticsearch",
        description = "Triggers bulk reindexing of all products to Elasticsearch. Only available when Elasticsearch is enabled."
    )
    fun reindexAllProducts(): ResponseEntity<ApiResponse<Map<String, Any>>> {
        if (productIndexService == null) {
            return ResponseEntity.ok(
                ApiResponse(
                    success = false,
                    message = "Elasticsearch is not enabled",
                    data = mapOf("status" to "disabled")
                )
            )
        }

        productIndexService.indexAllProducts()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Bulk reindexing started successfully",
                data = mapOf("status" to "reindexing")
            )
        )
    }

    @PostMapping("/{id}/reindex")
    @Operation(
        summary = "Reindex a single product to Elasticsearch",
        description = "Triggers reindexing of a specific product to Elasticsearch. Only available when Elasticsearch is enabled."
    )
    fun reindexProduct(@PathVariable id: Long): ResponseEntity<ApiResponse<Map<String, Any>>> {
        if (productIndexService == null) {
            return ResponseEntity.ok(
                ApiResponse(
                    success = false,
                    message = "Elasticsearch is not enabled",
                    data = mapOf("status" to "disabled")
                )
            )
        }

        productIndexService.indexProduct(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Product reindexed successfully",
                data = mapOf("productId" to id, "status" to "indexed")
            )
        )
    }
}
