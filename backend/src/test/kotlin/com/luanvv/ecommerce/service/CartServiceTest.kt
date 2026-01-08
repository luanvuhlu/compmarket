package com.luanvv.ecommerce.service

import com.luanvv.ecommerce.dto.AddToCartRequest
import com.luanvv.ecommerce.entity.Cart
import com.luanvv.ecommerce.entity.CartItem
import com.luanvv.ecommerce.entity.Product
import com.luanvv.ecommerce.entity.User
import com.luanvv.ecommerce.exception.InsufficientStockException
import com.luanvv.ecommerce.exception.ResourceNotFoundException
import com.luanvv.ecommerce.repository.CartItemRepository
import com.luanvv.ecommerce.repository.CartRepository
import com.luanvv.ecommerce.repository.ProductRepository
import com.luanvv.ecommerce.repository.UserRepository
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockKExtension::class)
class CartServiceTest {

    @MockK lateinit var cartRepository: CartRepository
    @MockK lateinit var cartItemRepository: CartItemRepository
    @MockK lateinit var productRepository: ProductRepository
    @MockK lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var cartService: CartService

    @BeforeEach
    fun setup() = clearAllMocks()

    // helpers to build mocks without relying on constructors
    private fun mockProduct(
        id: Long = 2L,
        name: String = "prod",
        price: BigDecimal = BigDecimal("100.00"),
        discountPrice: BigDecimal? = null,
        stock: Int = 10,
        images: String? = null
    ): Product {
        val p = mockk<Product>(relaxed = true)
        every { p.id } returns id
        every { p.name } returns name
        every { p.price } returns price
        every { p.discountPrice } returns discountPrice
        every { p.stockQuantity } returns stock
        every { p.images } returns images
        return p
    }

    private fun mockUser(id: Long = 1L): User {
        val u = mockk<User>(relaxed = true)
        every { u.id } returns id
        return u
    }

    private fun mockCart(id: Long = 1L, items: List<CartItem> = emptyList()): Cart {
        val c = mockk<Cart>(relaxed = true)
        every { c.id } returns id
        every { c.items } returns items
        return c
    }

    private fun mockCartItem(
        id: Long = 5L,
        cart: Cart,
        product: Product,
        quantity: Int = 1
    ): CartItem {
        val ci = mockk<CartItem>(relaxed = true)
        every { ci.id } returns id
        every { ci.cart } returns cart
        every { ci.product } returns product
        every { ci.quantity } returns quantity
        every { ci.quantity = any() } just Runs
        return ci
    }

    @Test
    fun `getCart creates cart when none exists`() {
        val user = mockUser(1L)
        val newCart = mockCart(id = 10L, items = emptyList())

        every { cartRepository.findByUserId(1L) } returns Optional.empty()
        every { userRepository.findById(1L) } returns Optional.of(user)
        every { cartRepository.save(any()) } returns newCart

        val resp = cartService.getCart(1L)

        assertEquals(10L, resp.id)
        assertEquals(0, resp.totalItems)
    }

    @Test
    fun `addToCart creates new cart item when none exists`() {
        val cart = mockCart(id = 1L, items = emptyList())
        val product = mockProduct(id = 2L, price = BigDecimal("50.00"), stock = 10)
        val savedItem = mockCartItem(id = 7L, cart = cart, product = product, quantity = 2)
        val updatedCart = mockCart(id = 1L, items = listOf(savedItem))

        every { cartRepository.findByUserId(1L) } returnsMany listOf(
            Optional.of(cart),
            Optional.of(updatedCart),
        )
        every { productRepository.findById(2L) } returns Optional.of(product)
        every { cartItemRepository.findByCartIdAndProductId(1L, 2L) } returns null
        every { cartItemRepository.save(any()) } returns savedItem

        val req = AddToCartRequest(productId = 2L, quantity = 2)
        val resp = cartService.addToCart(1L, req)

        assertEquals(2, resp.totalItems)
        assertEquals(BigDecimal("100.00"), resp.totalPrice) // 50 * 2
        verify(exactly = 1) { cartItemRepository.save(any()) }
    }

    @Test
    fun `addToCart throws when insufficient stock`() {
        val cart = mockCart(id = 1L)
        val product = mockProduct(id = 3L, stock = 1)

        every { cartRepository.findByUserId(1L) } returns Optional.of(cart)
        every { productRepository.findById(3L) } returns Optional.of(product)

        val req = AddToCartRequest(productId = 3L, quantity = 5)
        assertThrows(InsufficientStockException::class.java) {
            cartService.addToCart(1L, req)
        }
    }

    @Test
    fun `updateCartItem throws when item not in user's cart`() {
        val cart = mockCart(id = 1L)
        val otherCart = mockCart(id = 2L)
        val product = mockProduct()
        val cartItem = mockCartItem(id = 9L, cart = otherCart, product = product, quantity = 1)

        every { cartRepository.findByUserId(1L) } returns Optional.of(cart)
        every { cartItemRepository.findById(9L) } returns Optional.of(cartItem)

        assertThrows(ResourceNotFoundException::class.java) {
            cartService.updateCartItem(1L, 9L, 2)
        }
    }

    @Test
    fun `clearCart deletes by cart id`() {
        val cart = mockCart(id = 42L)
        every { cartRepository.findByUserId(1L) } returns Optional.of(cart)
        every { cartItemRepository.deleteByCartId(42L) } just Runs

        cartService.clearCart(1L)

        verify { cartItemRepository.deleteByCartId(42L) }
    }
}
