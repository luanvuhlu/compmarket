package com.luanvv.ecommerce.event

import com.luanvv.ecommerce.entity.Product

/**
 * Event published when a product is created
 */
data class ProductCreatedEvent(
    val product: Product
)

/**
 * Event published when a product is updated
 */
data class ProductUpdatedEvent(
    val product: Product
)

/**
 * Event published when a product is deleted
 */
data class ProductDeletedEvent(
    val productId: Long
)
