package com.gearvn.ecommerce.service

import com.gearvn.ecommerce.dto.*
import com.gearvn.ecommerce.entity.Product
import com.gearvn.ecommerce.repository.ProductSearchRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val productSearchRepository: ProductSearchRepository
) {

    /**
     * Perform advanced search with filters and facets
     */
    fun search(searchRequest: SearchRequest, pageable: Pageable): SearchResponse {
        // Get products matching the search criteria
        val productPage = productSearchRepository.searchWithFilters(searchRequest, pageable)
        
        // Get facets for filtering options
        val facets = SearchFacets(
            categories = productSearchRepository.getCategoryFacets(searchRequest),
            brands = productSearchRepository.getBrandFacets(searchRequest),
            priceRanges = productSearchRepository.getPriceRangeFacets(searchRequest)
        )
        
        // Convert products to response DTOs
        val productResponses = productPage.map { it.toResponse() }
        
        // Build page response
        val pageResponse = PageResponse(
            content = productResponses.content,
            page = productResponses.number,
            size = productResponses.size,
            totalElements = productResponses.totalElements,
            totalPages = productResponses.totalPages,
            last = productResponses.isLast
        )
        
        return SearchResponse(
            products = pageResponse,
            facets = facets
        )
    }

    private fun Product.toResponse() = ProductResponse(
        id = this.id!!,
        categoryId = this.category.id!!,
        categoryName = this.category.name,
        name = this.name,
        description = this.description,
        sku = this.sku,
        price = this.price,
        discountPrice = this.discountPrice,
        stockQuantity = this.stockQuantity,
        brand = this.brand,
        model = this.model,
        specifications = this.specifications,
        images = this.images,
        isActive = this.isActive
    )
}
