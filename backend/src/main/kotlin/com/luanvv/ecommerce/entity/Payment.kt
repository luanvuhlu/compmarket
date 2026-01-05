package com.luanvv.ecommerce.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    var order: Order,

    @Column(nullable = false, name = "payment_method", length = 50)
    var paymentMethod: String,

    @Column(name = "transaction_id", length = 255)
    var transactionId: String? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Column(name = "payment_date")
    var paymentDate: LocalDateTime? = null,

    @Column(columnDefinition = "jsonb", name = "gateway_response")
    var gatewayResponse: String? = null
) : BaseEntity()

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}
