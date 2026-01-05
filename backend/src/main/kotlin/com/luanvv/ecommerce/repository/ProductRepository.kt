package com.luanvv.ecommerce.repository

import com.luanvv.ecommerce.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findBySku(sku: String): Optional<Product>
    
    fun findByIsActiveTrue(pageable: Pageable): Page<Product>
    
    fun findByCategoryIdAndIsActiveTrue(categoryId: Long, pageable: Pageable): Page<Product>
    
    @Query("""
        SELECT p FROM Product p 
        WHERE p.isActive = true 
        AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    fun searchProducts(@Param("keyword") keyword: String, pageable: Pageable): Page<Product>
    
    @Query(value = """
        SELECT * FROM products 
        WHERE is_active = true 
        AND search_vector @@ to_tsquery('english', :query)
        ORDER BY ts_rank(search_vector, to_tsquery('english', :query)) DESC
    """, nativeQuery = true)
    fun fullTextSearch(@Param("query") query: String, pageable: Pageable): Page<Product>
}
