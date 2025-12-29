package com.gearvn.ecommerce.dto

import com.gearvn.ecommerce.entity.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderDto(
    val id: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val taxAmount: BigDecimal,
    val shippingAmount: BigDecimal,
    val discountAmount: BigDecimal?,
    val items: List<OrderItemDto>,
    val shippingAddress: AddressDto?,
    val billingAddress: AddressDto?,
    val createdAt: LocalDateTime
)

data class OrderItemDto(
    val id: Long?,
    val productId: Long,
    val productName: String?,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)

data class CreateOrderRequest(
    val shippingAddressId: Long,
    val billingAddressId: Long,
    val paymentMethod: String
)

data class OrderResponse(
    val orderId: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val paymentUrl: String?
)
