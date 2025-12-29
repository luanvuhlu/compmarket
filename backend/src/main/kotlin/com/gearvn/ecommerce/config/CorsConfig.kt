package com.gearvn.ecommerce.config

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
        val config = CorsConfiguration()
        
        config.allowCredentials = appProperties.cors.allowCredentials
        config.allowedOrigins = appProperties.cors.allowedOrigins.split(",")
        config.allowedMethods = appProperties.cors.allowedMethods.split(",")
        config.allowedHeaders = appProperties.cors.allowedHeaders.split(",")
        config.maxAge = 3600L
        
        source.registerCorsConfiguration("/api/**", config)
        return CorsFilter(source)
    }
}
