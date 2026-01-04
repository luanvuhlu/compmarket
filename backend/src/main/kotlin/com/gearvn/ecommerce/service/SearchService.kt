package com.gearvn.ecommerce.service

import com.gearvn.ecommerce.dto.*
import com.gearvn.ecommerce.elasticsearch.ElasticsearchService
import com.gearvn.ecommerce.elasticsearch.ProductDocument
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SearchService(
    private val elasticsearchService: ElasticsearchService
) {

    /**
     * Perform advanced search using Elasticsearch with filters and facets
     */
    fun search(searchRequest: SearchRequest, pageable: Pageable): SearchResponse {
        // Search using Elasticsearch
        val searchHits = elasticsearchService.search(searchRequest, pageable)
        
        // Extract products from search results
        val products = searchHits.searchHits.map { it.content.toProductResponse() }
        
        // Extract facets from aggregations
        val facets = extractFacets(searchHits)
        
        // Build page response
        val pageResponse = PageResponse(
            content = products,
            page = pageable.pageNumber,
            size = pageable.pageSize,
            totalElements = searchHits.totalHits,
            totalPages = (searchHits.totalHits + pageable.pageSize - 1) / pageable.pageSize,
            last = (pageable.pageNumber + 1) * pageable.pageSize >= searchHits.totalHits
        )
        
        return SearchResponse(
            products = pageResponse,
            facets = facets
        )
    }

    /**
     * Get auto-complete suggestions
     */
    fun getAutoCompleteSuggestions(prefix: String, limit: Int = 10): List<String> {
        return elasticsearchService.getAutoCompleteSuggestions(prefix, limit)
    }

    /**
     * Get similar products using "More Like This"
     */
    fun getMoreLikeThis(productId: String, limit: Int = 10): List<ProductResponse> {
        return elasticsearchService.getMoreLikeThis(productId, limit)
            .map { it.toProductResponse() }
    }

    /**
     * Extract facets from Elasticsearch aggregations
     */
    private fun extractFacets(searchHits: SearchHits<ProductDocument>): SearchFacets {
        val aggregations = searchHits.aggregations
        
        // Extract brand facets
        val brandFacets = aggregations?.get("brands")?.let { agg ->
            // Extract bucket aggregation results
            // Note: This is a simplified version, actual implementation depends on aggregation structure
            emptyList<BrandFacet>()
        } ?: emptyList()
        
        // Extract category facets
        val categoryFacets = aggregations?.get("categories")?.let { agg ->
            emptyList<CategoryFacet>()
        } ?: emptyList()
        
        // Extract price range facets
        val priceRangeFacets = listOf(
            PriceRangeFacet(BigDecimal.ZERO, BigDecimal("100"), "Under $100", 0),
            PriceRangeFacet(BigDecimal("100"), BigDecimal("500"), "$100 - $500", 0),
            PriceRangeFacet(BigDecimal("500"), BigDecimal("1000"), "$500 - $1,000", 0),
            PriceRangeFacet(BigDecimal("1000"), BigDecimal("2000"), "$1,000 - $2,000", 0),
            PriceRangeFacet(BigDecimal("2000"), BigDecimal("999999"), "$2,000+", 0)
        )
        
        return SearchFacets(
            categories = categoryFacets,
            brands = brandFacets,
            priceRanges = priceRangeFacets
        )
    }

    private fun ProductDocument.toProductResponse() = ProductResponse(
        id = this.id.toLong(),
        categoryId = this.categoryId,
        categoryName = this.category,
        name = this.name,
        description = this.description,
        sku = this.sku,
        price = this.price,
        discountPrice = this.discountPrice,
        stockQuantity = this.stockQuantity,
        brand = this.brand,
        model = this.model,
        specifications = null, // Specifications are in nested format in ES
        images = this.images.joinToString(",") { "{\"url\":\"$it\"}" },
        isActive = true
    )
}
