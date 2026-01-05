package com.luanvv.ecommerce.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync

/**
 * Configuration for async processing
 * Enables @Async annotation for asynchronous event handling
 */
@Configuration
@EnableAsync
class AsyncConfig
