package com.luanvv.ecommerce.repository

import com.luanvv.ecommerce.entity.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findByCartIdAndProductId(cartId: Long, productId: Long): CartItem?
    fun deleteByCartId(cartId: Long)
}
