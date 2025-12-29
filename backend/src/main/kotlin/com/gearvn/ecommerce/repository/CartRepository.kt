package com.gearvn.ecommerce.repository

import com.gearvn.ecommerce.entity.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CartRepository : JpaRepository<Cart, Long> {
    fun findByUserId(userId: Long): Optional<Cart>
}
