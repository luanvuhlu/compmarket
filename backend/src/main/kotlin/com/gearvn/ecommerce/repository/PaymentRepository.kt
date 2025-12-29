package com.gearvn.ecommerce.repository

import com.gearvn.ecommerce.entity.Payment
import com.gearvn.ecommerce.entity.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: Long): Optional<Payment>
    
    fun findByTransactionId(transactionId: String): Optional<Payment>
    
    fun findByStatus(status: PaymentStatus): List<Payment>
}
