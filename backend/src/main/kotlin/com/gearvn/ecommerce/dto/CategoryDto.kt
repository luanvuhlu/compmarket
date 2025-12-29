package com.gearvn.ecommerce.dto

data class CategoryDto(
    val id: Long?,
    val name: String,
    val description: String?,
    val parentCategoryId: Long?,
    val slug: String,
    val imageUrl: String?
)

data class CategoryResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val parentCategoryId: Long?,
    val slug: String,
    val imageUrl: String?,
    val subcategories: List<CategoryResponse>? = null
)
