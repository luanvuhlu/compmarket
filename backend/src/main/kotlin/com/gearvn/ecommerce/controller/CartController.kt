package com.gearvn.ecommerce.controller

import com.gearvn.ecommerce.dto.AddToCartRequest
import com.gearvn.ecommerce.dto.ApiResponse
import com.gearvn.ecommerce.dto.CartResponse
import com.gearvn.ecommerce.dto.UpdateCartItemRequest
import com.gearvn.ecommerce.service.CartService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cart")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Shopping Cart", description = "Shopping cart management endpoints")
class CartController(
    private val cartService: CartService
) {

    @GetMapping
    @Operation(summary = "Get user cart", description = "Retrieve current user's shopping cart")
    fun getCart(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<ApiResponse<CartResponse>> {
        val userId = getUserId(userDetails)
        val cart = cartService.getCart(userId)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Cart retrieved successfully",
                data = cart
            )
        )
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a product to the shopping cart")
    fun addToCart(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: AddToCartRequest
    ): ResponseEntity<ApiResponse<CartResponse>> {
        val userId = getUserId(userDetails)
        val cart = cartService.addToCart(userId, request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Item added to cart",
                data = cart
            )
        )
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Update quantity of a cart item")
    fun updateCartItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable itemId: Long,
        @RequestBody request: UpdateCartItemRequest
    ): ResponseEntity<ApiResponse<CartResponse>> {
        val userId = getUserId(userDetails)
        val cart = cartService.updateCartItem(userId, itemId, request.quantity)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Cart item updated",
                data = cart
            )
        )
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Remove a product from the shopping cart")
    fun removeFromCart(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable itemId: Long
    ): ResponseEntity<ApiResponse<CartResponse>> {
        val userId = getUserId(userDetails)
        val cart = cartService.removeFromCart(userId, itemId)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Item removed from cart",
                data = cart
            )
        )
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from the shopping cart")
    fun clearCart(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<ApiResponse<Void>> {
        val userId = getUserId(userDetails)
        cartService.clearCart(userId)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Cart cleared successfully",
                data = null
            )
        )
    }

    // Helper method to extract user ID from UserDetails
    // In production, you'd fetch this from the database
    private fun getUserId(userDetails: UserDetails): Long {
        // For now, return a placeholder
        // In production, fetch user by email and get ID
        return 1L // Placeholder
    }
}
