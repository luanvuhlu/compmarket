package com.luanvv.ecommerce.repository

import com.luanvv.ecommerce.dto.BrandFacet
import com.luanvv.ecommerce.dto.CategoryFacet
import com.luanvv.ecommerce.dto.PriceRangeFacet
import com.luanvv.ecommerce.dto.SearchRequest
import com.luanvv.ecommerce.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Custom repository for advanced product search operations
 */
interface ProductSearchRepository {
    /**
     * Search products with advanced filtering and sorting
     */
    fun searchWithFilters(searchRequest: SearchRequest, pageable: Pageable): Page<Product>
    
    /**
     * Get category facets for current search
     */
    fun getCategoryFacets(searchRequest: SearchRequest): List<CategoryFacet>
    
    /**
     * Get brand facets for current search
     */
    fun getBrandFacets(searchRequest: SearchRequest): List<BrandFacet>
    
    /**
     * Get price range facets for current search
     */
    fun getPriceRangeFacets(searchRequest: SearchRequest): List<PriceRangeFacet>
}
