package com.luanvv.ecommerce.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig(
    private val appProperties: AppProperties
) {
    
    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration().apply {
            allowCredentials = appProperties.cors.allowCredentials
            allowedOrigins = appProperties.cors.allowedOrigins.split(",")
            allowedMethods = appProperties.cors.allowedMethods.split(",")
            allowedHeaders = appProperties.cors.allowedHeaders.split(",")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization")
            maxAge = 3600L
        }
        source.registerCorsConfiguration("/api/**", config)
        return CorsFilter(source)
    }
}
