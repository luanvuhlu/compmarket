package com.gearvn.ecommerce.controller

import com.gearvn.ecommerce.dto.ApiResponse
import com.gearvn.ecommerce.dto.AuthResponse
import com.gearvn.ecommerce.dto.LoginRequest
import com.gearvn.ecommerce.dto.RegisterRequest
import com.gearvn.ecommerce.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                message = "User registered successfully",
                data = response
            )
        )
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.login(request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Login successful",
                data = response
            )
        )
    }
}
