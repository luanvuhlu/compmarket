package com.luanvv.ecommerce.repository

import com.luanvv.ecommerce.entity.AttributeDefinition
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AttributeDefinitionRepository : JpaRepository<AttributeDefinition, Long> {
    fun findByName(name: String): Optional<AttributeDefinition>
    fun findByIsFilterableTrue(): List<AttributeDefinition>
    fun findByIsSearchableTrue(): List<AttributeDefinition>
    fun findAllByOrderBySortOrderAsc(): List<AttributeDefinition>
}
