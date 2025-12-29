package com.gearvn.ecommerce.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
)

data class AuthResponse(
    val token: String,
    val type: String = "Bearer",
    val email: String,
    val roles: List<String>
)
