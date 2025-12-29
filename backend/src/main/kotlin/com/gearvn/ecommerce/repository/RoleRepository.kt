package com.gearvn.ecommerce.repository

import com.gearvn.ecommerce.entity.Role
import com.gearvn.ecommerce.entity.RoleType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    fun findByRoleName(roleName: RoleType): Optional<Role>
}
