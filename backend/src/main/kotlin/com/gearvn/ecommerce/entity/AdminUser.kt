package com.gearvn.ecommerce.entity

import jakarta.persistence.*

@Entity
@Table(name = "admin_users")
class AdminUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: User,

    @Column(columnDefinition = "jsonb")
    var permissions: String? = null,

    @Column(name = "last_login")
    var lastLogin: java.time.LocalDateTime? = null
) : BaseEntity()
