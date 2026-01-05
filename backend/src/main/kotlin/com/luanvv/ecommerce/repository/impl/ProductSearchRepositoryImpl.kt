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
        val queryBuilder = StringBuilder("""
            SELECT p FROM Product p
            WHERE p.isActive = true
        """)
        
        val params = mutableMapOf<String, Any>()
        
        // Add full-text search if query is provided
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
        
        // Add sorting
        queryBuilder.append(getSortClause(searchRequest.sortBy, searchRequest.sortOrder))
        
        // Count query
        val countQuery = entityManager.createQuery(
            queryBuilder.toString().replace("SELECT p", "SELECT COUNT(p)"),
            Long::class.java
        )
        params.forEach { (key, value) -> countQuery.setParameter(key, value) }
        val total = countQuery.singleResult
        
        // Data query with pagination
        val query = entityManager.createQuery(queryBuilder.toString(), Product::class.java)
        params.forEach { (key, value) -> query.setParameter(key, value) }
        query.firstResult = pageable.offset.toInt()
        query.maxResults = pageable.pageSize
        
        val results = query.resultList
        
        return PageImpl(results, pageable, total)
    }

    override fun getCategoryFacets(searchRequest: SearchRequest): List<CategoryFacet> {
        val queryBuilder = StringBuilder("""
            SELECT c.id, c.name, COUNT(p.id)
            FROM Product p
            JOIN p.category c
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
        
        queryBuilder.append(" GROUP BY c.id, c.name ORDER BY COUNT(p.id) DESC")
        
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
