package com.luanvv.ecommerce.repository

import com.luanvv.ecommerce.entity.ProductSpecification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductSpecificationRepository : JpaRepository<ProductSpecification, Long> {
    fun findByProductId(productId: Long): List<ProductSpecification>
    
    fun findByProductIdAndAttributeId(productId: Long, attributeId: Long): ProductSpecification?
    
    @Query("SELECT ps FROM ProductSpecification ps WHERE ps.product.id = :productId")
    fun findByProductIdWithAttributes(productId: Long): List<ProductSpecification>
    
    fun deleteByProductId(productId: Long)
}
