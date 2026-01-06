package com.luanvv.ecommerce.service

import com.luanvv.ecommerce.dto.*
import com.luanvv.ecommerce.repository.ProductSearchRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SqlSearchService(
    private val productSearchRepository: ProductSearchRepository
) {

    /**
     * Perform advanced search using SQL with filters and facets
     * This replaces the Elasticsearch-based search with SQL-based search
     * that supports attribute/specification filtering like RAM: 16GB, CPU: Intel Core i7
     */
    fun search(searchRequest: SearchRequest, pageable: Pageable): SearchResponse {
        // Search using SQL with specification filters
        val searchPage = productSearchRepository.searchWithFilters(searchRequest, pageable)

        // Extract products from search results
        val products = searchPage.content.map { it.toResponse() }

        // Extract facets for filtering UI
        val facets = SearchFacets(
            categories = productSearchRepository.getCategoryFacets(searchRequest),
            brands = productSearchRepository.getBrandFacets(searchRequest),
            priceRanges = productSearchRepository.getPriceRangeFacets(searchRequest),
            specifications = productSearchRepository.getSpecificationFacets(searchRequest)
        )

        // Build page response
        val pageResponse = PageResponse(
            content = products,
            number = searchPage.number,
            size = searchPage.size,
            totalElements = searchPage.totalElements,
            totalPages = searchPage.totalPages,
            first = searchPage.isFirst,
            last = searchPage.isLast
        )

        return SearchResponse(
            products = pageResponse,
            facets = facets
        )
    }

    /**
     * Get auto-complete suggestions based on product names and specifications
     */
    fun getAutoCompleteSuggestions(prefix: String, limit: Int = 10): List<String> {
        // Simple implementation - can be enhanced to include specification values
        val allProducts = productSearchRepository.searchWithFilters(
            SearchRequest(query = prefix),
            org.springframework.data.domain.PageRequest.of(0, limit * 2)
        )

        val suggestions = mutableSetOf<String>()

        allProducts.content.forEach { product ->
            // Add product name suggestions
            if (product.name.contains(prefix, ignoreCase = true)) {
                suggestions.add(product.name)
            }

            // Add brand suggestions
            if (product.brand?.contains(prefix, ignoreCase = true) == true) {
                suggestions.add(product.brand!!)
            }
        }

        return suggestions.take(limit)
    }

    /**
     * Get similar products based on category and specifications
     */
    fun getMoreLikeThis(productId: String, limit: Int = 10): List<ProductResponse> {
        // Find the product first
        val productIdLong = productId.toLongOrNull() ?: return emptyList()

        // For now, return products from same category
        // This can be enhanced to match specifications
        val searchRequest = SearchRequest(
            categoryIds = listOf(productIdLong), // This would need the actual product's category
            inStock = true
        )

        val similarProducts = productSearchRepository.searchWithFilters(
            searchRequest,
            org.springframework.data.domain.PageRequest.of(0, limit)
        )

        return similarProducts.content.map { it.toResponse() }
    }

    private fun com.luanvv.ecommerce.entity.Product.toResponse() = ProductResponse(
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

