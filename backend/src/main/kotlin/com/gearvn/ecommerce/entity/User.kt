package com.gearvn.ecommerce.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 255)
    var email: String,

    @Column(nullable = false, name = "password_hash", length = 255)
    var passwordHash: String,

    @Column(nullable = false, name = "first_name", length = 100)
    var firstName: String,

    @Column(nullable = false, name = "last_name", length = 100)
    var lastName: String,

    @Column(length = 20)
    var phone: String? = null,

    @Column(nullable = false, name = "is_active")
    var isActive: Boolean = true,

    @Column(nullable = false, name = "email_verified")
    var emailVerified: Boolean = false,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<Role> = mutableSetOf()
) : BaseEntity()
