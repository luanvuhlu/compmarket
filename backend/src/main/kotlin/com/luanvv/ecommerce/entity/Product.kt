package com.luanvv.ecommerce.entity

import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.math.BigDecimal

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @Column(nullable = false, length = 255)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false, unique = true, length = 100)
    var sku: String,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,

    @Column(name = "discount_price", precision = 10, scale = 2)
    var discountPrice: BigDecimal? = null,

    @Column(nullable = false, name = "stock_quantity")
    var stockQuantity: Int = 0,

    @Column(length = 100)
    var brand: String? = null,

    @Column(length = 100)
    var model: String? = null,

    @Column(columnDefinition = "jsonb")
    @Deprecated("Use a dedicated Attribute entity instead")
    var specifications: String? = null,

    @Column(columnDefinition = "jsonb")
    var images: String? = null,

    @Column(nullable = false, name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "search_vector", columnDefinition = "tsvector")
    var searchVector: String? = null
) : BaseEntity()
