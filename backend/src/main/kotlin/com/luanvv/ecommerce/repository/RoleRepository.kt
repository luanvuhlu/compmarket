package com.luanvv.ecommerce.repository

import com.luanvv.ecommerce.entity.Role
import com.luanvv.ecommerce.entity.RoleType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    fun findByRoleName(roleName: RoleType): Optional<Role>
}
