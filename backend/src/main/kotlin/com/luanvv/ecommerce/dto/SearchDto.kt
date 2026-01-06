package com.luanvv.ecommerce.dto

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
    val sortOrder: SortOrder = SortOrder.DESC,
    val specifications: Map<String, String>? = null // e.g., "ram_size" -> "16", "processor" -> "Intel Core i7"
)

/**
 * Specification filter for attribute-based search
 */
data class SpecificationFilter(
    val attributeName: String,
    val value: String,
    val operator: SpecificationOperator = SpecificationOperator.EQUALS
)

/**
 * Operators for specification filtering
 */
enum class SpecificationOperator {
    EQUALS,         // Exact match: RAM = 16GB
    CONTAINS,       // Contains text: CPU contains "Intel"
    GREATER_THAN,   // Numeric: Storage > 512GB
    LESS_THAN,      // Numeric: Price < 1000
    RANGE           // Between values: RAM between 8GB and 32GB
}

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
    val priceRanges: List<PriceRangeFacet> = emptyList(),
    val specifications: List<SpecificationFacet> = emptyList()
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

/**
 * Specification facet for attribute-based filtering
 */
data class SpecificationFacet(
    val attributeName: String,
    val attributeDisplayName: String,
    val values: List<SpecificationValue>
)

/**
 * Specification value with count for faceted search
 */
data class SpecificationValue(
    val value: String,
    val count: Long
)

