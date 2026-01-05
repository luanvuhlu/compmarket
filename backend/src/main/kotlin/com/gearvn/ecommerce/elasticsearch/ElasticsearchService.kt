package com.gearvn.ecommerce.elasticsearch

import com.gearvn.ecommerce.dto.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * Service for Elasticsearch-based product search
 * Simplified stub implementation - will be completed when Elasticsearch is properly configured
 */
@Service
@ConditionalOnProperty(name = ["app.elasticsearch.enabled"], havingValue = "true", matchIfMissing = false)
class ElasticsearchService(
    private val productDocumentRepository: ProductDocumentRepository
) {

    /**
     * Perform advanced search with filters
     */
    fun search(searchRequest: SearchRequest, pageable: Pageable): Page<ProductDocument> {
        // Use repository methods for basic search
        return when {
            !searchRequest.brands.isNullOrEmpty() -> {
                productDocumentRepository.findByBrand(searchRequest.brands.first(), pageable)
            }
            !searchRequest.categoryIds.isNullOrEmpty() -> {
                productDocumentRepository.findByCategoryId(searchRequest.categoryIds.first(), pageable)
            }
            else -> {
                productDocumentRepository.findByInStockTrue(pageable)
            }
        }
    }

    /**
     * Get auto-complete suggestions
     */
    fun getAutoCompleteSuggestions(prefix: String, limit: Int = 10): List<String> {
        // Simple implementation using repository
        val pageable = org.springframework.data.domain.PageRequest.of(0, limit)
        val results = productDocumentRepository.findByInStockTrue(pageable)
        
        return results.content
            .filter { it.name.contains(prefix, ignoreCase = true) }
            .map { it.name }
            .distinct()
            .take(limit)
    }

    /**
     * Get similar products
     */
    fun getMoreLikeThis(productId: String, limit: Int = 10): List<ProductDocument> {
        // Simplified implementation: find products in same category
        val product = productDocumentRepository.findById(productId).orElse(null) ?: return emptyList()
        
        val pageable = org.springframework.data.domain.PageRequest.of(0, limit)
        return productDocumentRepository.findByCategoryId(product.categoryId, pageable).content
            .filter { it.id != productId }
    }
}
