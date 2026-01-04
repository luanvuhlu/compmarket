package com.gearvn.ecommerce.entity

import jakarta.persistence.*

@Entity
@Table(name = "attribute_definitions")
class AttributeDefinition(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attribute_id")
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 100)
    var name: String,

    @Column(nullable = false, name = "display_name", length = 255)
    var displayName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "data_type", length = 20)
    var dataType: AttributeDataType,

    @Column(length = 50)
    var unit: String? = null,

    @Column(nullable = false, name = "is_filterable")
    var isFilterable: Boolean = true,

    @Column(nullable = false, name = "is_searchable")
    var isSearchable: Boolean = false,

    @Column(nullable = false, name = "sort_order")
    var sortOrder: Int = 0,

    @OneToMany(mappedBy = "attribute", cascade = [CascadeType.ALL], orphanRemoval = true)
    var options: MutableList<AttributeOption> = mutableListOf()
) : BaseEntity()

enum class AttributeDataType {
    STRING,
    NUMERIC,
    BOOLEAN,
    ENUM
}
