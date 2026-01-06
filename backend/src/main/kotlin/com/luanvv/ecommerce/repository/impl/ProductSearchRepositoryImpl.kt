package com.luanvv.ecommerce.repository.impl

import com.luanvv.ecommerce.dto.*
import com.luanvv.ecommerce.entity.Product
import com.luanvv.ecommerce.repository.ProductSearchRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class ProductSearchRepositoryImpl : ProductSearchRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun searchWithFilters(searchRequest: SearchRequest, pageable: Pageable): Page<Product> {
        val params = mutableMapOf<String, Any>()

        // Build the base WHERE clause that will be shared by both queries
        val whereClause = buildWhereClause(searchRequest, params)

        // Build count query
        val countQueryString = """
            SELECT COUNT(DISTINCT p.id)
            FROM Product p
            LEFT JOIN ProductSpecification ps ON ps.product.id = p.id
            LEFT JOIN AttributeDefinition ad ON ad.id = ps.attribute.id
            $whereClause
        """.trimIndent()

        val countQuery = entityManager.createQuery(countQueryString, Long::class.java)
        params.forEach { (key, value) -> countQuery.setParameter(key, value) }
        val total = countQuery.singleResult

        // Build data query with sorting
        val dataQueryString = """
            SELECT DISTINCT p 
            FROM Product p
            LEFT JOIN ProductSpecification ps ON ps.product.id = p.id
            LEFT JOIN AttributeDefinition ad ON ad.id = ps.attribute.id
            $whereClause
            ${getSortClause(searchRequest.sortBy, searchRequest.sortOrder)}
        """.trimIndent()

        val query = entityManager.createQuery(dataQueryString, Product::class.java)
        params.forEach { (key, value) -> query.setParameter(key, value) }
        query.firstResult = pageable.offset.toInt()
        query.maxResults = pageable.pageSize

        val results = query.resultList

        return PageImpl(results, pageable, total)
    }

    private fun buildWhereClause(searchRequest: SearchRequest, params: MutableMap<String, Any>): String {
        val whereBuilder = StringBuilder("WHERE p.isActive = true")

        // Add full-text search if query is provided
        if (!searchRequest.query.isNullOrBlank()) {
            whereBuilder.append("""
                AND (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))
                )
            """)
            params["query"] = searchRequest.query
        }
        
        // Add specification filters (e.g., RAM: 16GB, CPU: Intel Core i7)
        if (!searchRequest.specifications.isNullOrEmpty()) {
            searchRequest.specifications.forEach { (attributeName, value) ->
                val paramKey = "spec_${attributeName.replace(" ", "_").replace("-", "_")}"
                whereBuilder.append("""
                    AND EXISTS (
                        SELECT 1 FROM ProductSpecification ps2
                        JOIN AttributeDefinition ad2 ON ad2.id = ps2.attribute.id
                        WHERE ps2.product.id = p.id
                        AND LOWER(ad2.name) = LOWER(:${paramKey}_attr)
                        AND (
                            LOWER(ps2.valueString) LIKE LOWER(CONCAT('%', :${paramKey}_val, '%'))
                            OR CAST(ps2.valueNumeric AS STRING) LIKE CONCAT('%', :${paramKey}_val, '%')
                        )
                    )
                """)
                params["${paramKey}_attr"] = attributeName
                params["${paramKey}_val"] = value
            }
        }

        // Add category filter
        if (!searchRequest.categoryIds.isNullOrEmpty()) {
            whereBuilder.append(" AND p.category.id IN :categoryIds")
            params["categoryIds"] = searchRequest.categoryIds
        }
        
        // Add brand filter
        if (!searchRequest.brands.isNullOrEmpty()) {
            whereBuilder.append(" AND p.brand IN :brands")
            params["brands"] = searchRequest.brands
        }
        
        // Add price range filter
        if (searchRequest.minPrice != null) {
            whereBuilder.append(" AND p.price >= :minPrice")
            params["minPrice"] = searchRequest.minPrice
        }
        if (searchRequest.maxPrice != null) {
            whereBuilder.append(" AND p.price <= :maxPrice")
            params["maxPrice"] = searchRequest.maxPrice
        }
        
        // Add stock filter
        if (searchRequest.inStock == true) {
            whereBuilder.append(" AND p.stockQuantity > 0")
        }
        
        return whereBuilder.toString()
    }

    override fun getCategoryFacets(searchRequest: SearchRequest): List<CategoryFacet> {
        val queryBuilder = StringBuilder("""
            SELECT c.id, c.name, COUNT(DISTINCT p.id)
            FROM Product p
            JOIN p.category c
            LEFT JOIN ProductSpecification ps ON ps.product.id = p.id
            LEFT JOIN AttributeDefinition ad ON ad.id = ps.attribute.id
            WHERE p.isActive = true
        """)
        
        val params = mutableMapOf<String, Any>()
        
        // Add search query filter
        if (!searchRequest.query.isNullOrBlank()) {
            queryBuilder.append("""
                AND (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))
                )
            """)
            params["query"] = searchRequest.query
        }
        
        // Add specification filters
        if (!searchRequest.specifications.isNullOrEmpty()) {
            searchRequest.specifications.forEach { (attributeName, value) ->
                val paramKey = "spec_${attributeName.replace(" ", "_")}"
                queryBuilder.append("""
                    AND EXISTS (
                        SELECT 1 FROM ProductSpecification ps2
                        JOIN AttributeDefinition ad2 ON ad2.id = ps2.attribute.id
                        WHERE ps2.product.id = p.id
                        AND LOWER(ad2.name) = LOWER(:${paramKey}_attr)
                        AND (
                            LOWER(ps2.valueString) LIKE LOWER(CONCAT('%', :${paramKey}_val, '%'))
                            OR CAST(ps2.valueNumeric AS STRING) LIKE CONCAT('%', :${paramKey}_val, '%')
                        )
                    )
                """)
                params["${paramKey}_attr"] = attributeName
                params["${paramKey}_val"] = value
            }
        }

        // Add brand filter
        if (!searchRequest.brands.isNullOrEmpty()) {
            queryBuilder.append(" AND p.brand IN :brands")
            params["brands"] = searchRequest.brands
        }
        
        // Add price range filter
        if (searchRequest.minPrice != null) {
            queryBuilder.append(" AND p.price >= :minPrice")
            params["minPrice"] = searchRequest.minPrice
        }
        if (searchRequest.maxPrice != null) {
            queryBuilder.append(" AND p.price <= :maxPrice")
            params["maxPrice"] = searchRequest.maxPrice
        }
        
        // Add stock filter
        if (searchRequest.inStock == true) {
            queryBuilder.append(" AND p.stockQuantity > 0")
        }
        
        queryBuilder.append(" GROUP BY c.id, c.name ORDER BY COUNT(DISTINCT p.id) DESC")

        val query = entityManager.createQuery(queryBuilder.toString())
        params.forEach { (key, value) -> query.setParameter(key, value) }
        
        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<Array<Any>>
        
        return results.map { row ->
            CategoryFacet(
                categoryId = row[0] as Long,
                categoryName = row[1] as String,
                count = row[2] as Long
            )
        }
    }

    override fun getBrandFacets(searchRequest: SearchRequest): List<BrandFacet> {
        val queryBuilder = StringBuilder("""
            SELECT p.brand, COUNT(p.id)
            FROM Product p
            WHERE p.isActive = true
            AND p.brand IS NOT NULL
        """)
        
        val params = mutableMapOf<String, Any>()
        
        // Add search query filter
        if (!searchRequest.query.isNullOrBlank()) {
            queryBuilder.append("""
                AND (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))
                )
            """)
            params["query"] = searchRequest.query
        }
        
        // Add category filter
        if (!searchRequest.categoryIds.isNullOrEmpty()) {
            queryBuilder.append(" AND p.category.id IN :categoryIds")
            params["categoryIds"] = searchRequest.categoryIds
        }
        
        // Add price range filter
        if (searchRequest.minPrice != null) {
            queryBuilder.append(" AND p.price >= :minPrice")
            params["minPrice"] = searchRequest.minPrice
        }
        if (searchRequest.maxPrice != null) {
            queryBuilder.append(" AND p.price <= :maxPrice")
            params["maxPrice"] = searchRequest.maxPrice
        }
        
        // Add stock filter
        if (searchRequest.inStock == true) {
            queryBuilder.append(" AND p.stockQuantity > 0")
        }
        
        queryBuilder.append(" GROUP BY p.brand ORDER BY COUNT(p.id) DESC")
        
        val query = entityManager.createQuery(queryBuilder.toString())
        params.forEach { (key, value) -> query.setParameter(key, value) }
        
        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<Array<Any>>
        
        return results.map { row ->
            BrandFacet(
                brand = row[0] as String,
                count = row[1] as Long
            )
        }
    }

    override fun getPriceRangeFacets(searchRequest: SearchRequest): List<PriceRangeFacet> {
        // Define price ranges
        val priceRanges = listOf(
            PriceRange(BigDecimal.ZERO, BigDecimal("100"), "Under $100"),
            PriceRange(BigDecimal("100"), BigDecimal("500"), "$100 - $500"),
            PriceRange(BigDecimal("500"), BigDecimal("1000"), "$500 - $1,000"),
            PriceRange(BigDecimal("1000"), BigDecimal("2000"), "$1,000 - $2,000"),
            PriceRange(BigDecimal("2000"), BigDecimal("999999"), "$2,000+")
        )
        
        return priceRanges.map { range ->
            val count = countProductsInPriceRange(searchRequest, range.min, range.max)
            PriceRangeFacet(
                min = range.min,
                max = range.max,
                label = range.label,
                count = count
            )
        }.filter { it.count > 0 }
    }
    
    private fun countProductsInPriceRange(
        searchRequest: SearchRequest,
        minPrice: BigDecimal,
        maxPrice: BigDecimal
    ): Long {
        val queryBuilder = StringBuilder("""
            SELECT COUNT(p.id)
            FROM Product p
            WHERE p.isActive = true
            AND p.price >= :rangeMin
            AND p.price < :rangeMax
        """)
        
        val params = mutableMapOf<String, Any>(
            "rangeMin" to minPrice,
            "rangeMax" to maxPrice
        )
        
        // Add search query filter
        if (!searchRequest.query.isNullOrBlank()) {
            queryBuilder.append("""
                AND (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))
                )
            """)
            params["query"] = searchRequest.query
        }
        
        // Add category filter
        if (!searchRequest.categoryIds.isNullOrEmpty()) {
            queryBuilder.append(" AND p.category.id IN :categoryIds")
            params["categoryIds"] = searchRequest.categoryIds
        }
        
        // Add brand filter
        if (!searchRequest.brands.isNullOrEmpty()) {
            queryBuilder.append(" AND p.brand IN :brands")
            params["brands"] = searchRequest.brands
        }
        
        // Add stock filter
        if (searchRequest.inStock == true) {
            queryBuilder.append(" AND p.stockQuantity > 0")
        }
        
        val query = entityManager.createQuery(queryBuilder.toString(), Long::class.java)
        params.forEach { (key, value) -> query.setParameter(key, value) }
        
        return query.singleResult
    }
    
    override fun getSpecificationFacets(searchRequest: SearchRequest): List<SpecificationFacet> {
        // Get top filterable attributes with their values
        val queryBuilder = StringBuilder("""
            SELECT ad.name, ad.displayName, 
                   COALESCE(ps.valueString, CAST(ps.valueNumeric AS STRING)), 
                   COUNT(DISTINCT p.id)
            FROM Product p
            JOIN ProductSpecification ps ON ps.product.id = p.id
            JOIN AttributeDefinition ad ON ad.id = ps.attribute.id
            WHERE p.isActive = true 
            AND ad.isFilterable = true
            AND (ps.valueString IS NOT NULL OR ps.valueNumeric IS NOT NULL)
        """)

        val params = mutableMapOf<String, Any>()

        // Add search query filter (same filters as main search)
        if (!searchRequest.query.isNullOrBlank()) {
            queryBuilder.append("""
                AND (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))
                )
            """)
            params["query"] = searchRequest.query
        }

        // Add category filter
        if (!searchRequest.categoryIds.isNullOrEmpty()) {
            queryBuilder.append(" AND p.category.id IN :categoryIds")
            params["categoryIds"] = searchRequest.categoryIds
        }

        // Add brand filter
        if (!searchRequest.brands.isNullOrEmpty()) {
            queryBuilder.append(" AND p.brand IN :brands")
            params["brands"] = searchRequest.brands
        }

        // Add price range filter
        if (searchRequest.minPrice != null) {
            queryBuilder.append(" AND p.price >= :minPrice")
            params["minPrice"] = searchRequest.minPrice
        }
        if (searchRequest.maxPrice != null) {
            queryBuilder.append(" AND p.price <= :maxPrice")
            params["maxPrice"] = searchRequest.maxPrice
        }

        // Add stock filter
        if (searchRequest.inStock == true) {
            queryBuilder.append(" AND p.stockQuantity > 0")
        }

        queryBuilder.append("""
            GROUP BY ad.name, ad.displayName, COALESCE(ps.valueString, CAST(ps.valueNumeric AS STRING))
            HAVING COUNT(DISTINCT p.id) > 0
            ORDER BY ad.name, COUNT(DISTINCT p.id) DESC
        """)

        val query = entityManager.createQuery(queryBuilder.toString())
        params.forEach { (key, value) -> query.setParameter(key, value) }

        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<Array<Any>>

        // Group results by attribute name
        val groupedResults = results.groupBy { row ->
            Pair(row[0] as String, row[1] as String) // (attributeName, displayName)
        }

        return groupedResults.map { (attributeInfo, values) ->
            SpecificationFacet(
                attributeName = attributeInfo.first,
                attributeDisplayName = attributeInfo.second,
                values = values.map { row ->
                    SpecificationValue(
                        value = row[2] as String,
                        count = row[3] as Long
                    )
                }.take(10) // Limit to top 10 values per attribute
            )
        }
    }

    private fun getSortClause(sortBy: SortOption, sortOrder: SortOrder): String {
        val order = if (sortOrder == SortOrder.ASC) "ASC" else "DESC"
        
        return when (sortBy) {
            SortOption.PRICE -> " ORDER BY p.price $order"
            SortOption.NAME -> " ORDER BY p.name $order"
            SortOption.NEWEST -> " ORDER BY p.createdAt $order"
            SortOption.RELEVANCE -> " ORDER BY p.name $order" // For simple LIKE search, name is a good proxy
        }
    }
    
    private data class PriceRange(
        val min: BigDecimal,
        val max: BigDecimal,
        val label: String
    )
}
