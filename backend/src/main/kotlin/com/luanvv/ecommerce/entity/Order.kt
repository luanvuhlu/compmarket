package com.luanvv.ecommerce.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false, unique = true, name = "order_number", length = 50)
    var orderNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(nullable = false, name = "total_amount", precision = 10, scale = 2)
    var totalAmount: BigDecimal,

    @Column(nullable = false, name = "tax_amount", precision = 10, scale = 2)
    var taxAmount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, name = "shipping_amount", precision = 10, scale = 2)
    var shippingAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "discount_amount", precision = 10, scale = 2)
    var discountAmount: BigDecimal? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    var shippingAddress: Address? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_id")
    var billingAddress: Address? = null,

    @Column(name = "shipped_at")
    var shippedAt: LocalDateTime? = null,

    @Column(name = "delivered_at")
    var deliveredAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<OrderItem> = mutableListOf()
) : BaseEntity()

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
