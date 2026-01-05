package com.luanvv.ecommerce.elasticsearch

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

/**
 * Elasticsearch repository for product search
 */
@Repository
@ConditionalOnProperty(name = ["app.elasticsearch.enabled"], havingValue = "true", matchIfMissing = false)
interface ProductDocumentRepository : ElasticsearchRepository<ProductDocument, String> {
    
    /**
     * Find products by brand
     */
    fun findByBrand(brand: String, pageable: Pageable): Page<ProductDocument>
    
    /**
     * Find products by category
     */
    fun findByCategoryId(categoryId: Long, pageable: Pageable): Page<ProductDocument>
    
    /**
     * Find products with stock
     */
    fun findByInStockTrue(pageable: Pageable): Page<ProductDocument>
    
    /**
     * Find products by price range
     */
    fun findByPriceBetween(minPrice: Double, maxPrice: Double, pageable: Pageable): Page<ProductDocument>
}
