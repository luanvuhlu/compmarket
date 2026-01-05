package com.luanvv.ecommerce.service

import com.luanvv.ecommerce.dto.*
import com.luanvv.ecommerce.elasticsearch.ElasticsearchService
import com.luanvv.ecommerce.elasticsearch.ProductDocument
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
@ConditionalOnBean(ElasticsearchService::class)
class SearchService(
    private val elasticsearchService: ElasticsearchService
) {

    /**
     * Perform advanced search using Elasticsearch with filters and facets
     */
    fun search(searchRequest: SearchRequest, pageable: Pageable): SearchResponse {
        // Search using Elasticsearch
        val searchPage = elasticsearchService.search(searchRequest, pageable)
        
        // Extract products from search results
        val products = searchPage.content.map { it.toProductResponse() }
        
        // Extract facets (simplified - would need proper implementation)
        val facets = SearchFacets(
            categories = emptyList(),
            brands = emptyList(),
            priceRanges = emptyList()
        )
        
        // Build page response
        val pageResponse = PageResponse(
            content = products,
            number = searchPage.number,
            size = searchPage.size,
            totalElements = searchPage.totalElements,
            totalPages = searchPage.totalPages,
            last = searchPage.isLast
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
        images = serializeImages(this.images),
        isActive = true
    )
    
    private fun serializeImages(images: List<String>): String {
        return if (images.isEmpty()) {
            "[]"
        } else {
            try {
                com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
                    .writeValueAsString(images.map { mapOf("url" to it) })
            } catch (e: Exception) {
                "[]"
            }
        }
    }
}
