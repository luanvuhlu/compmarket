package com.luanvv.ecommerce.controller

import com.luanvv.ecommerce.dto.ApiResponse
import com.luanvv.ecommerce.dto.PageResponse
import com.luanvv.ecommerce.dto.ProductCreateRequest
import com.luanvv.ecommerce.dto.ProductDetailResponse
import com.luanvv.ecommerce.dto.ProductResponse
import com.luanvv.ecommerce.dto.ProductSpecificationDto
import com.luanvv.ecommerce.service.ProductService
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
    @Operation(
        summary = "Get all products", 
        description = "Retrieve paginated list of active products. Returns lightweight product info without EAV specifications for better performance."
    )
    fun getAllProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<ProductResponse>> {
        val pageable = PageRequest.of(page, size)
        val productPage = productService.getAllProducts(pageable)
        return ResponseEntity.ok(productPage)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get product by ID", 
        description = "Retrieve basic product details by ID. For full details including structured specifications, use /api/products/{id}/detail"
    )
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

    @GetMapping("/{id}/detail")
    @Operation(
        summary = "Get product detail with specifications",
        description = "Retrieve complete product details including structured EAV specifications. Use this for product detail pages."
    )
    fun getProductDetailById(@PathVariable id: Long): ResponseEntity<ApiResponse<ProductDetailResponse>> {
        val productDetail = productService.getProductDetailById(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Product detail retrieved successfully",
                data = productDetail
            )
        )
    }

    @GetMapping("/{id}/specifications")
    @Operation(
        summary = "Get product specifications separately",
        description = "Retrieve only the EAV specifications for a product. Useful for lazy loading or when you need specifications independently."
    )
    fun getProductSpecifications(@PathVariable id: Long): ResponseEntity<ApiResponse<List<ProductSpecificationDto>>> {
        val specifications = productService.getProductSpecifications(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Product specifications retrieved successfully",
                data = specifications
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
        
        return ResponseEntity.ok(productPage)
    }

    @GetMapping("/search/fulltext")
    @Operation(
        summary = "Full-text search products",
        description = "Advanced full-text search using PostgreSQL's search_vector. " +
                     "Provides better relevance ranking and performance for complex text queries. " +
                     "Supports phrase matching, stemming, and ranking by relevance."
    )
    fun fullTextSearchProducts(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<ProductResponse>> {
        val pageable = PageRequest.of(page, size)
        val productPage = productService.fullTextSearch(query, pageable)

        return ResponseEntity.ok(productPage)
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
        
        return ResponseEntity.ok(productPage)
    }
}
