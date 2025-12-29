package com.gearvn.ecommerce.dto

import java.math.BigDecimal

data class ProductDto(
    val id: Long?,
    val categoryId: Long,
    val name: String,
    val description: String?,
    val sku: String,
    val price: BigDecimal,
    val discountPrice: BigDecimal?,
    val stockQuantity: Int,
    val brand: String?,
    val model: String?,
    val specifications: String?,
    val images: String?,
    val isActive: Boolean = true
)

data class ProductResponse(
    val id: Long,
    val categoryId: Long,
    val categoryName: String?,
    val name: String,
    val description: String?,
    val sku: String,
    val price: BigDecimal,
    val discountPrice: BigDecimal?,
    val stockQuantity: Int,
    val brand: String?,
    val model: String?,
    val specifications: String?,
    val images: String?,
    val isActive: Boolean
)

data class ProductCreateRequest(
    val categoryId: Long,
    val name: String,
    val description: String?,
    val sku: String,
    val price: BigDecimal,
    val discountPrice: BigDecimal?,
    val stockQuantity: Int,
    val brand: String?,
    val model: String?,
    val specifications: String?,
    val images: String?
)
