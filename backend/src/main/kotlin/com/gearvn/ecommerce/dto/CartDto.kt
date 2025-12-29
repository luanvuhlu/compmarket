package com.gearvn.ecommerce.dto

import java.math.BigDecimal

data class CartItemDto(
    val id: Long?,
    val productId: Long,
    val productName: String?,
    val productPrice: BigDecimal?,
    val quantity: Int
)

data class CartResponse(
    val id: Long,
    val items: List<CartItemDto>,
    val totalItems: Int,
    val totalAmount: BigDecimal
)

data class AddToCartRequest(
    val productId: Long,
    val quantity: Int = 1
)

data class UpdateCartItemRequest(
    val quantity: Int
)
