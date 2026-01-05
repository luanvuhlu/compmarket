package com.luanvv.ecommerce.dto

import java.math.BigDecimal

/**
 * DTO for product specification with attribute details
 */
data class ProductSpecificationDto(
    val attributeId: Long,
    val attributeName: String,
    val displayName: String,
    val dataType: String,
    val unit: String?,
    val valueString: String?,
    val valueNumeric: BigDecimal?,
    val valueBoolean: Boolean?
)

/**
 * Enhanced product response with structured specifications
 */
data class ProductDetailResponse(
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
    val images: String?,
    val isActive: Boolean,
    val specifications: List<ProductSpecificationDto> = emptyList()
)
