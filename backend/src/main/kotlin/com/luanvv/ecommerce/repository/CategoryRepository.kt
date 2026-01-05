package com.luanvv.ecommerce.repository

import com.luanvv.ecommerce.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findBySlug(slug: String): Optional<Category>
    fun findByParentCategoryIsNull(): List<Category>
    fun findByParentCategoryId(parentId: Long): List<Category>
}
