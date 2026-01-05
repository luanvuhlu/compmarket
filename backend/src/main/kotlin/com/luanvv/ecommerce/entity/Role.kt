package com.luanvv.ecommerce.entity

import jakarta.persistence.*

@Entity
@Table(name = "roles")
class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, name = "role_name", length = 50)
    var roleName: RoleType
)

enum class RoleType {
    CUSTOMER,
    ADMIN,
    SUPER_ADMIN
}
