package com.gearvn.ecommerce.repository

import com.gearvn.ecommerce.entity.Order
import com.gearvn.ecommerce.entity.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByOrderNumber(orderNumber: String): Optional<Order>
    
    fun findByUserId(userId: Long, pageable: Pageable): Page<Order>
    
    fun findByUserIdAndStatus(userId: Long, status: OrderStatus, pageable: Pageable): Page<Order>
}
