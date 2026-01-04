package com.gearvn.ecommerce.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "product_specifications")
class ProductSpecification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spec_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    var attribute: AttributeDefinition,

    @Column(name = "value_string", length = 500)
    var valueString: String? = null,

    @Column(name = "value_numeric", precision = 15, scale = 4)
    var valueNumeric: BigDecimal? = null,

    @Column(name = "value_boolean")
    var valueBoolean: Boolean? = null
) : BaseEntity()
