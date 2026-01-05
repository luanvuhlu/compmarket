package com.luanvv.ecommerce.repository

import com.luanvv.ecommerce.entity.AdminUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AdminUserRepository : JpaRepository<AdminUser, Long> {
    fun findByUserId(userId: Long): Optional<AdminUser>
}
