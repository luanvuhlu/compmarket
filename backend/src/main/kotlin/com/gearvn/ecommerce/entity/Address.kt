package com.gearvn.ecommerce.entity

import jakarta.persistence.*

@Entity
@Table(name = "addresses")
class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "address_type", length = 20)
    var addressType: AddressType,

    @Column(nullable = false, length = 255)
    var street: String,

    @Column(nullable = false, length = 100)
    var city: String,

    @Column(nullable = false, length = 100)
    var state: String,

    @Column(nullable = false, name = "postal_code", length = 20)
    var postalCode: String,

    @Column(nullable = false, length = 100)
    var country: String,

    @Column(nullable = false, name = "is_default")
    var isDefault: Boolean = false
) : BaseEntity()

enum class AddressType {
    SHIPPING,
    BILLING
}
