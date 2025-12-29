package com.gearvn.ecommerce.service

import com.gearvn.ecommerce.dto.AuthResponse
import com.gearvn.ecommerce.dto.LoginRequest
import com.gearvn.ecommerce.dto.RegisterRequest
import com.gearvn.ecommerce.entity.RoleType
import com.gearvn.ecommerce.entity.User
import com.gearvn.ecommerce.exception.DuplicateResourceException
import com.gearvn.ecommerce.repository.RoleRepository
import com.gearvn.ecommerce.repository.UserRepository
import com.gearvn.ecommerce.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateResourceException("Email already registered")
        }

        val customerRole = roleRepository.findByRoleName(RoleType.CUSTOMER)
            .orElseThrow { RuntimeException("Customer role not found") }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            firstName = request.firstName,
            lastName = request.lastName,
            phone = request.phone,
            roles = mutableSetOf(customerRole)
        )

        userRepository.save(user)

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val userDetails = authentication.principal as UserDetails
        val token = jwtTokenProvider.generateToken(userDetails)

        return AuthResponse(
            token = token,
            email = user.email,
            roles = user.roles.map { it.roleName.name }
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        SecurityContextHolder.getContext().authentication = authentication

        val userDetails = authentication.principal as UserDetails
        val token = jwtTokenProvider.generateToken(userDetails)

        val user = userRepository.findByEmail(request.email)
            .orElseThrow { RuntimeException("User not found") }

        return AuthResponse(
            token = token,
            email = user.email,
            roles = user.roles.map { it.roleName.name }
        )
    }
}
