package com.gearvn.ecommerce.dto

import java.math.BigDecimal

/**
 * Request DTO for enhanced product search with filters
 */
data class SearchRequest(
    val query: String? = null,
    val categoryIds: List<Long>? = null,
    val brands: List<String>? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val inStock: Boolean? = null,
    val sortBy: SortOption = SortOption.RELEVANCE,
    val sortOrder: SortOrder = SortOrder.DESC
)

/**
 * Sort options for search results
 */
enum class SortOption {
    RELEVANCE,
    PRICE,
    NAME,
    NEWEST
}

/**
 * Sort order
 */
enum class SortOrder {
    ASC,
    DESC
}

/**
 * Response DTO for search results with facets
 */
data class SearchResponse(
    val products: PageResponse<ProductResponse>,
    val facets: SearchFacets
)

/**
 * Facets for filtering options
 */
data class SearchFacets(
    val categories: List<CategoryFacet> = emptyList(),
    val brands: List<BrandFacet> = emptyList(),
    val priceRanges: List<PriceRangeFacet> = emptyList()
)

/**
 * Category facet with count
 */
data class CategoryFacet(
    val categoryId: Long,
    val categoryName: String,
    val count: Long
)

/**
 * Brand facet with count
 */
data class BrandFacet(
    val brand: String,
    val count: Long
)

/**
 * Price range facet with count
 */
data class PriceRangeFacet(
    val min: BigDecimal,
    val max: BigDecimal,
    val label: String,
    val count: Long
)
