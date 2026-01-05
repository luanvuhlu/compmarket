package com.gearvn.ecommerce.entity

import jakarta.persistence.*

@Entity
@Table(name = "attribute_options")
class AttributeOption(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    var attribute: AttributeDefinition,

    @Column(nullable = false, name = "option_value", length = 255)
    var optionValue: String,

    @Column(nullable = false, name = "display_label", length = 255)
    var displayLabel: String,

    @Column(nullable = false, name = "sort_order")
    var sortOrder: Int = 0
) : BaseEntity()
