package com.luanvv.ecommerce.event

import com.luanvv.ecommerce.service.ProductIndexService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * Event listener to automatically sync product changes to Elasticsearch
 */
@Component
@ConditionalOnBean(ProductIndexService::class)
class ProductEventListener(
    private val productIndexService: ProductIndexService
) {
    private val logger = LoggerFactory.getLogger(ProductEventListener::class.java)

    /**
     * Listen for product created events and index to Elasticsearch
     */
    @Async
    @EventListener
    fun onProductCreated(event: ProductCreatedEvent) {
        try {
            logger.info("Product created event received for product ID: ${event.product.id}")
            event.product.id?.let { productIndexService.indexProduct(it) }
        } catch (e: Exception) {
            logger.error("Failed to index product on creation: ${event.product.id}", e)
        }
    }

    /**
     * Listen for product updated events and reindex to Elasticsearch
     */
    @Async
    @EventListener
    fun onProductUpdated(event: ProductUpdatedEvent) {
        try {
            logger.info("Product updated event received for product ID: ${event.product.id}")
            event.product.id?.let { productIndexService.indexProduct(it) }
        } catch (e: Exception) {
            logger.error("Failed to reindex product on update: ${event.product.id}", e)
        }
    }

    /**
     * Listen for product deleted events and remove from Elasticsearch
     */
    @Async
    @EventListener
    fun onProductDeleted(event: ProductDeletedEvent) {
        try {
            logger.info("Product deleted event received for product ID: ${event.productId}")
            productIndexService.deleteFromIndex(event.productId)
        } catch (e: Exception) {
            logger.error("Failed to delete product from index: ${event.productId}", e)
        }
    }
}
