package com.gearvn.ecommerce.service

import com.gearvn.ecommerce.dto.AddToCartRequest
import com.gearvn.ecommerce.dto.CartItemDto
import com.gearvn.ecommerce.dto.CartResponse
import com.gearvn.ecommerce.entity.Cart
import com.gearvn.ecommerce.entity.CartItem
import com.gearvn.ecommerce.exception.InsufficientStockException
import com.gearvn.ecommerce.exception.ResourceNotFoundException
import com.gearvn.ecommerce.repository.CartItemRepository
import com.gearvn.ecommerce.repository.CartRepository
import com.gearvn.ecommerce.repository.ProductRepository
import com.gearvn.ecommerce.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {

    fun getCart(userId: Long): CartResponse {
        val cart = getOrCreateCart(userId)
        return cart.toResponse()
    }

    @Transactional
    fun addToCart(userId: Long, request: AddToCartRequest): CartResponse {
        val cart = getOrCreateCart(userId)
        val product = productRepository.findById(request.productId)
            .orElseThrow { ResourceNotFoundException("Product not found") }

        if (product.stockQuantity < request.quantity) {
            throw InsufficientStockException("Insufficient stock for product: ${product.name}")
        }

        val existingItem = cartItemRepository.findByCartIdAndProductId(cart.id!!, product.id!!)
        
        if (existingItem != null) {
            existingItem.quantity += request.quantity
            if (product.stockQuantity < existingItem.quantity) {
                throw InsufficientStockException("Insufficient stock for product: ${product.name}")
            }
            cartItemRepository.save(existingItem)
        } else {
            val cartItem = CartItem(
                cart = cart,
                product = product,
                quantity = request.quantity
            )
            cartItemRepository.save(cartItem)
        }

        return getCart(userId)
    }

    @Transactional
    fun updateCartItem(userId: Long, itemId: Long, quantity: Int): CartResponse {
        val cart = getOrCreateCart(userId)
        val cartItem = cartItemRepository.findById(itemId)
            .orElseThrow { ResourceNotFoundException("Cart item not found") }

        if (cartItem.cart.id != cart.id) {
            throw ResourceNotFoundException("Cart item not found in user's cart")
        }

        val product = cartItem.product
        if (product.stockQuantity < quantity) {
            throw InsufficientStockException("Insufficient stock for product: ${product.name}")
        }

        cartItem.quantity = quantity
        cartItemRepository.save(cartItem)

        return getCart(userId)
    }

    @Transactional
    fun removeFromCart(userId: Long, itemId: Long): CartResponse {
        val cart = getOrCreateCart(userId)
        val cartItem = cartItemRepository.findById(itemId)
            .orElseThrow { ResourceNotFoundException("Cart item not found") }

        if (cartItem.cart.id != cart.id) {
            throw ResourceNotFoundException("Cart item not found in user's cart")
        }

        cartItemRepository.delete(cartItem)
        return getCart(userId)
    }

    @Transactional
    fun clearCart(userId: Long) {
        val cart = getOrCreateCart(userId)
        cartItemRepository.deleteByCartId(cart.id!!)
    }

    private fun getOrCreateCart(userId: Long): Cart {
        return cartRepository.findByUserId(userId).orElseGet {
            val user = userRepository.findById(userId)
                .orElseThrow { ResourceNotFoundException("User not found") }
            val newCart = Cart(user = user)
            cartRepository.save(newCart)
        }
    }

    private fun Cart.toResponse(): CartResponse {
        val items = this.items.map { item ->
            CartItemDto(
                id = item.id,
                productId = item.product.id!!,
                productName = item.product.name,
                productPrice = item.product.discountPrice ?: item.product.price,
                quantity = item.quantity,
                imageUrls = item.product.images,
            )
        }

        val totalPrice = items.fold(BigDecimal.ZERO) { acc, item ->
            acc + (item.productPrice!! * item.quantity.toBigDecimal())
        }

        return CartResponse(
            id = this.id!!,
            items = items,
            totalItems = items.sumOf { it.quantity },
            totalPrice = totalPrice
        )
    }
}
