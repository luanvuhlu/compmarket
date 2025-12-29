package com.gearvn.ecommerce

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
class EcommerceApplication

fun main(args: Array<String>) {
    runApplication<EcommerceApplication>(*args)
}
