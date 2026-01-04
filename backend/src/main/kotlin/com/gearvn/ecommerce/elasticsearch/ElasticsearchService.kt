package com.gearvn.ecommerce.elasticsearch

import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation
import co.elastic.clients.elasticsearch._types.query_dsl.*
import com.gearvn.ecommerce.dto.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * Service for Elasticsearch-based product search
 */
@Service
class ElasticsearchService(
    private val elasticsearchOperations: ElasticsearchOperations
) {

    companion object {
        private const val INDEX_NAME = "products"
    }

    /**
     * Perform advanced search with filters and aggregations
     */
    fun search(searchRequest: SearchRequest, pageable: Pageable): SearchHits<ProductDocument> {
        val query = buildSearchQuery(searchRequest)
        val nativeQuery = NativeQueryBuilder()
            .withQuery(query)
            .withPageable(pageable)
            .withAggregation("brands", buildBrandAggregation())
            .withAggregation("categories", buildCategoryAggregation())
            .withAggregation("price_ranges", buildPriceRangeAggregation())
            .build()
        
        applySorting(nativeQuery, searchRequest.sortBy, searchRequest.sortOrder)
        
        return elasticsearchOperations.search(
            nativeQuery,
            ProductDocument::class.java,
            IndexCoordinates.of(INDEX_NAME)
        )
    }

    /**
     * Build the main search query combining filters and text search
     */
    private fun buildSearchQuery(searchRequest: SearchRequest): Query {
        val boolQuery = BoolQuery.Builder()

        // Always filter active products with stock
        boolQuery.filter(Query.Builder().term { t -> 
            t.field("inStock").value { v -> v.booleanValue(true) }
        }.build())

        // Add text search if query is provided
        if (!searchRequest.query.isNullOrBlank()) {
            boolQuery.must(buildTextSearchQuery(searchRequest.query))
        }

        // Add category filter
        if (!searchRequest.categoryIds.isNullOrEmpty()) {
            boolQuery.filter(Query.Builder().terms { t ->
                t.field("categoryId").terms { termsValue ->
                    termsValue.value(searchRequest.categoryIds.map { 
                        co.elastic.clients.elasticsearch._types.FieldValue.of(it) 
                    })
                }
            }.build())
        }

        // Add brand filter
        if (!searchRequest.brands.isNullOrEmpty()) {
            boolQuery.filter(Query.Builder().terms { t ->
                t.field("brand").terms { termsValue ->
                    termsValue.value(searchRequest.brands.map { 
                        co.elastic.clients.elasticsearch._types.FieldValue.of(it) 
                    })
                }
            }.build())
        }

        // Add price range filter
        if (searchRequest.minPrice != null || searchRequest.maxPrice != null) {
            boolQuery.filter(buildPriceRangeQuery(searchRequest.minPrice, searchRequest.maxPrice))
        }

        // Add stock filter
        if (searchRequest.inStock == true) {
            boolQuery.filter(Query.Builder().term { t ->
                t.field("inStock").value { v -> v.booleanValue(true) }
            }.build())
        }

        return Query.Builder().bool(boolQuery.build()).build()
    }

    /**
     * Build multi-field text search query
     */
    private fun buildTextSearchQuery(queryText: String): Query {
        return Query.Builder().multiMatch { m ->
            m.query(queryText)
                .fields(
                    "name^3",              // Boost name highest
                    "brand^2",             // Boost brand
                    "searchableText",      // Combined searchable field
                    "description",
                    "specifications.valueString"
                )
                .fuzziness("AUTO")        // Handle typos
                .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
        }.build()
    }

    /**
     * Build price range query
     */
    private fun buildPriceRangeQuery(minPrice: BigDecimal?, maxPrice: BigDecimal?): Query {
        return Query.Builder().range { r ->
            r.field("price")
            minPrice?.let { r.gte(co.elastic.clients.json.JsonData.of(it.toDouble())) }
            maxPrice?.let { r.lte(co.elastic.clients.json.JsonData.of(it.toDouble())) }
        }.build()
    }

    /**
     * Build brand aggregation
     */
    private fun buildBrandAggregation(): Aggregation {
        return Aggregation.Builder()
            .terms { t -> t.field("brand").size(50) }
            .build()
    }

    /**
     * Build category aggregation
     */
    private fun buildCategoryAggregation(): Aggregation {
        return Aggregation.Builder()
            .terms { t -> t.field("categoryId").size(50) }
            .build()
    }

    /**
     * Build price range aggregation
     */
    private fun buildPriceRangeAggregation(): Aggregation {
        return Aggregation.Builder()
            .range { r ->
                r.field("price")
                    .ranges { range -> range.to("100").key("under_100") }
                    .ranges { range -> range.from("100").to("500").key("100_500") }
                    .ranges { range -> range.from("500").to("1000").key("500_1000") }
                    .ranges { range -> range.from("1000").to("2000").key("1000_2000") }
                    .ranges { range -> range.from("2000").key("over_2000") }
            }
            .build()
    }

    /**
     * Apply sorting to the query
     */
    private fun applySorting(query: NativeQuery, sortBy: SortOption, sortOrder: com.gearvn.ecommerce.dto.SortOrder) {
        val esOrder = if (sortOrder == com.gearvn.ecommerce.dto.SortOrder.ASC) SortOrder.Asc else SortOrder.Desc
        
        when (sortBy) {
            SortOption.PRICE -> query.addSort(
                co.elastic.clients.elasticsearch._types.SortOptions.Builder()
                    .field { f -> f.field("price").order(esOrder) }
                    .build()
            )
            SortOption.NAME -> query.addSort(
                co.elastic.clients.elasticsearch._types.SortOptions.Builder()
                    .field { f -> f.field("name.keyword").order(esOrder) }
                    .build()
            )
            SortOption.NEWEST -> query.addSort(
                co.elastic.clients.elasticsearch._types.SortOptions.Builder()
                    .field { f -> f.field("createdAt").order(esOrder) }
                    .build()
            )
            SortOption.RELEVANCE -> {
                // Relevance is default, sorted by _score
                query.addSort(
                    co.elastic.clients.elasticsearch._types.SortOptions.Builder()
                        .score { s -> s.order(SortOrder.Desc) }
                        .build()
                )
            }
        }
    }

    /**
     * Get auto-complete suggestions
     */
    fun getAutoCompleteSuggestions(prefix: String, limit: Int = 10): List<String> {
        val query = Query.Builder()
            .matchPhrasePr prefix { m ->
                m.field("name").query(prefix)
            }
            .build()

        val nativeQuery = NativeQueryBuilder()
            .withQuery(query)
            .withPageable(PageRequest.of(0, limit))
            .build()

        val searchHits = elasticsearchOperations.search(
            nativeQuery,
            ProductDocument::class.java,
            IndexCoordinates.of(INDEX_NAME)
        )

        return searchHits.searchHits
            .map { it.content.name }
            .distinct()
    }

    /**
     * Get "More Like This" products
     */
    fun getMoreLikeThis(productId: String, limit: Int = 10): List<ProductDocument> {
        val query = Query.Builder()
            .moreLikeThis { mlt ->
                mlt.fields("name", "brand", "searchableText")
                    .like { like -> like.document { doc -> doc.index(INDEX_NAME).id(productId) } }
                    .minTermFreq(1)
                    .minDocFreq(1)
            }
            .build()

        val nativeQuery = NativeQueryBuilder()
            .withQuery(query)
            .withPageable(PageRequest.of(0, limit))
            .build()

        val searchHits = elasticsearchOperations.search(
            nativeQuery,
            ProductDocument::class.java,
            IndexCoordinates.of(INDEX_NAME)
        )

        return searchHits.searchHits.map { it.content }
    }
}
