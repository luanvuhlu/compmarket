package com.gearvn.ecommerce.repository

import com.gearvn.ecommerce.entity.AttributeOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AttributeOptionRepository : JpaRepository<AttributeOption, Long> {
    fun findByAttributeIdOrderBySortOrderAsc(attributeId: Long): List<AttributeOption>
}
