package com.gearvn.ecommerce.entity

import jakarta.persistence.*
import org.hibernate.annotations.Type

@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    var id: Long? = null,

    @Column(nullable = false, length = 255)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    var parentCategory: Category? = null,

    @Column(nullable = false, unique = true, length = 255)
    var slug: String,

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null
) : BaseEntity()
