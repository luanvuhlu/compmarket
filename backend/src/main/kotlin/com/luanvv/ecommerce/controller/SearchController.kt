package com.luanvv.ecommerce.controller

import com.luanvv.ecommerce.dto.ProductResponse
import com.luanvv.ecommerce.dto.SearchRequest
import com.luanvv.ecommerce.dto.SearchResponse
import com.luanvv.ecommerce.service.SqlSearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Advanced product search endpoints with SQL-based attribute filtering")
class SearchController(
    private val sqlSearchService: SqlSearchService
) {

    @PostMapping
    @Operation(
        summary = "Advanced product search",
        description = "Search products with filters, sorting, and facets using SQL. " +
                "Supports specification-based queries like RAM: 16GB, CPU: Intel Core i7"
    )
    fun search(
        @RequestBody searchRequest: SearchRequest,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<SearchResponse> {
        val pageable = PageRequest.of(page, size)
        val response = sqlSearchService.search(searchRequest, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    @Operation(
        summary = "Simple search with query parameters",
        description = "Search products using URL query parameters for easier testing. " +
                "Supports specification filtering: ?ram=16&cpu=Intel Core i7"
    )
    fun searchWithParams(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) categoryIds: List<Long>?,
        @RequestParam(required = false) brands: List<String>?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?,
        @RequestParam(required = false) inStock: Boolean?,
        @RequestParam(defaultValue = "RELEVANCE") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortOrder: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        // Specification filters as individual parameters
        @RequestParam(required = false) ram: String?, // e.g., ram=16
        @RequestParam(required = false) cpu: String?, // e.g., cpu=Intel Core i7
        @RequestParam(required = false) processor: String?, // Alternative name for CPU
        @RequestParam(required = false) storage: String?, // e.g., storage=512
        @RequestParam(required = false) gpu: String?, // e.g., gpu=RTX 3070
        @RequestParam(required = false) brand_cpu: String?, // e.g., brand_cpu=Intel
        @RequestParam(required = false) screen_size: String?, // e.g., screen_size=15.6
        @RequestParam(required = false) os: String?, // e.g., os=Windows 11
        @RequestParam(required = false) weight: String?, // e.g., weight=2.5
        // Generic specification parameter
        @RequestParam(required = false) specs: Map<String, String>?
    ): ResponseEntity<SearchResponse> {

        // Build specifications map from individual parameters and generic specs
        val specifications = mutableMapOf<String, String>()

        ram?.let { specifications["ram_size"] = it }
        cpu?.let { specifications["processor"] = it }
        processor?.let { specifications["processor"] = it }
        storage?.let { specifications["storage_capacity"] = it }
        gpu?.let { specifications["gpu"] = it }
        brand_cpu?.let { specifications["cpu_brand"] = it }
        screen_size?.let { specifications["screen_size"] = it }
        os?.let { specifications["operating_system"] = it }
        weight?.let { specifications["weight"] = it }

        // Add any additional specifications from the specs parameter
        specs?.let { specifications.putAll(it) }

        val searchRequest = SearchRequest(
            query = query,
            categoryIds = categoryIds,
            brands = brands,
            minPrice = minPrice,
            maxPrice = maxPrice,
            inStock = inStock,
            sortBy = com.luanvv.ecommerce.dto.SortOption.valueOf(sortBy),
            sortOrder = com.luanvv.ecommerce.dto.SortOrder.valueOf(sortOrder),
            specifications = if (specifications.isNotEmpty()) specifications else null
        )
        
        val pageable = PageRequest.of(page, size)
        val response = sqlSearchService.search(searchRequest, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/autocomplete")
    @Operation(
        summary = "Auto-complete suggestions",
        description = "Get auto-complete suggestions for product search including specification values"
    )
    fun autocomplete(
        @RequestParam prefix: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<String>> {
        val suggestions = sqlSearchService.getAutoCompleteSuggestions(prefix, limit)
        return ResponseEntity.ok(suggestions)
    }

    @GetMapping("/similar/{productId}")
    @Operation(
        summary = "Get similar products",
        description = "Find similar products based on category and specifications"
    )
    fun getSimilarProducts(
        @PathVariable productId: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<ProductResponse>> {
        val similarProducts = sqlSearchService.getMoreLikeThis(productId, limit)
        return ResponseEntity.ok(similarProducts)
    }

    @GetMapping("/by-spec")
    @Operation(
        summary = "Search by specific attribute",
        description = "Search products by specific attribute. Example: /api/search/by-spec?attr=ram_size&value=16"
    )
    fun searchBySpecification(
        @RequestParam attr: String,
        @RequestParam value: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<SearchResponse> {
        val searchRequest = SearchRequest(
            specifications = mapOf(attr to value)
        )

        val pageable = PageRequest.of(page, size)
        val response = sqlSearchService.search(searchRequest, pageable)
        return ResponseEntity.ok(response)
    }
}
