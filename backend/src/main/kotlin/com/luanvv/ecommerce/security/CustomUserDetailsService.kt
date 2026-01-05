package com.luanvv.ecommerce.security

import com.luanvv.ecommerce.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User not found with email: $email") }
        
        val authorities = user.roles.map { role ->
            SimpleGrantedAuthority("ROLE_${role.roleName.name}")
        }
        
        return org.springframework.security.core.userdetails.User(
            user.email,
            user.passwordHash,
            user.isActive,
            true,
            true,
            true,
            authorities
        )
    }
}
