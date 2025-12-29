package com.gearvn.ecommerce.dto

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)

data class ErrorResponse(
    val timestamp: Long,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
