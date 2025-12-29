package com.gearvn.ecommerce.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app")
data class AppProperties(
    var jwt: JwtProperties = JwtProperties(),
    var file: FileProperties = FileProperties(),
    var cors: CorsProperties = CorsProperties()
)

data class JwtProperties(
    var secret: String = "",
    var expiration: Long = 86400000 // 24 hours
)

data class FileProperties(
    var uploadDir: String = "./uploads"
)

data class CorsProperties(
    var allowedOrigins: String = "http://localhost:3000",
    var allowedMethods: String = "GET,POST,PUT,DELETE,OPTIONS",
    var allowedHeaders: String = "*",
    var allowCredentials: Boolean = true
)
