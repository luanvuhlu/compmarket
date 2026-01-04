package com.gearvn.ecommerce.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.data.domain.Page

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
data class PageResponse<T>(
    @JsonProperty("content")
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val size: Int = 0,
    val number: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val numberOfElements: Int = 0
) {
    companion object {
        fun <T> from(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content.toList(),
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                size = page.size,
                number = page.number,
                first = page.isFirst,
                last = page.isLast,
                numberOfElements = page.numberOfElements
            )
        }
    }
}

data class ErrorResponse(
    val timestamp: Long,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
