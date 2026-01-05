package com.luanvv.ecommerce.dto

import com.luanvv.ecommerce.entity.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentDto(
    val id: Long,
    val orderId: Long,
    val paymentMethod: String,
    val transactionId: String?,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val paymentDate: LocalDateTime?
)

data class PaymentWebhookRequest(
    val transactionId: String,
    val orderId: Long,
    val status: String,
    val amount: BigDecimal,
    val gatewayResponse: String?
)
