package com.luanvv.ecommerce.repository

import com.luanvv.ecommerce.entity.AttributeOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AttributeOptionRepository : JpaRepository<AttributeOption, Long> {
    fun findByAttributeIdOrderBySortOrderAsc(attributeId: Long): List<AttributeOption>
}
