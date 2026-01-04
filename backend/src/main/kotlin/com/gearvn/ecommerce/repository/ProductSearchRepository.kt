package com.gearvn.ecommerce.repository

import com.gearvn.ecommerce.dto.BrandFacet
import com.gearvn.ecommerce.dto.CategoryFacet
import com.gearvn.ecommerce.dto.PriceRangeFacet
import com.gearvn.ecommerce.dto.SearchRequest
import com.gearvn.ecommerce.entity.Product
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
