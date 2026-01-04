package com.gearvn.ecommerce.controller

import com.gearvn.ecommerce.dto.ProductResponse
import com.gearvn.ecommerce.dto.SearchRequest
import com.gearvn.ecommerce.dto.SearchResponse
import com.gearvn.ecommerce.service.SearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Advanced product search endpoints with Elasticsearch")
@ConditionalOnBean(SearchService::class)
class SearchController(
    private val searchService: SearchService
) {

    @PostMapping
    @Operation(
        summary = "Advanced product search",
        description = "Search products with filters, sorting, and facets using Elasticsearch. " +
                "Supports natural language queries like 'Asus i7 16GB RAM'"
    )
    fun search(
        @RequestBody searchRequest: SearchRequest,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<SearchResponse> {
        val pageable = PageRequest.of(page, size)
        val response = searchService.search(searchRequest, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    @Operation(
        summary = "Simple search with query parameters",
        description = "Search products using URL query parameters for easier testing"
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
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<SearchResponse> {
        val searchRequest = SearchRequest(
            query = query,
            categoryIds = categoryIds,
            brands = brands,
            minPrice = minPrice,
            maxPrice = maxPrice,
            inStock = inStock,
            sortBy = com.gearvn.ecommerce.dto.SortOption.valueOf(sortBy),
            sortOrder = com.gearvn.ecommerce.dto.SortOrder.valueOf(sortOrder)
        )
        
        val pageable = PageRequest.of(page, size)
        val response = searchService.search(searchRequest, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/autocomplete")
    @Operation(
        summary = "Auto-complete suggestions",
        description = "Get auto-complete suggestions for product search"
    )
    fun autocomplete(
        @RequestParam prefix: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<String>> {
        val suggestions = searchService.getAutoCompleteSuggestions(prefix, limit)
        return ResponseEntity.ok(suggestions)
    }

    @GetMapping("/similar/{productId}")
    @Operation(
        summary = "Get similar products",
        description = "Find similar products using Elasticsearch 'More Like This' feature"
    )
    fun getSimilarProducts(
        @PathVariable productId: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<ProductResponse>> {
        val similarProducts = searchService.getMoreLikeThis(productId, limit)
        return ResponseEntity.ok(similarProducts)
    }
}
