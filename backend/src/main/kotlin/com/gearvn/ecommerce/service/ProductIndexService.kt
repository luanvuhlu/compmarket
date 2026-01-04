package com.gearvn.ecommerce.service

import com.gearvn.ecommerce.elasticsearch.FacetFields
import com.gearvn.ecommerce.elasticsearch.ProductDocument
import com.gearvn.ecommerce.elasticsearch.ProductDocumentRepository
import com.gearvn.ecommerce.elasticsearch.SpecificationField
import com.gearvn.ecommerce.entity.Product
import com.gearvn.ecommerce.repository.ProductRepository
import com.gearvn.ecommerce.repository.ProductSpecificationRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service to synchronize products from PostgreSQL to Elasticsearch
 */
@Service
@ConditionalOnProperty(name = ["app.elasticsearch.enabled"], havingValue = "true", matchIfMissing = false)
class ProductIndexService(
    private val productRepository: ProductRepository,
    private val productSpecificationRepository: ProductSpecificationRepository,
    private val productDocumentRepository: ProductDocumentRepository,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(ProductIndexService::class.java)

    /**
     * Index a single product to Elasticsearch
     */
    @Transactional(readOnly = true)
    fun indexProduct(productId: Long) {
        val product = productRepository.findById(productId).orElse(null)
        if (product == null) {
            logger.warn("Product not found for indexing: $productId")
            return
        }
        
        val productDocument = convertToDocument(product)
        productDocumentRepository.save(productDocument)
        logger.info("Indexed product: ${product.name} (ID: $productId)")
    }

    /**
     * Index all products to Elasticsearch
     */
    @Transactional(readOnly = true)
    fun indexAllProducts() {
        logger.info("Starting to index all products...")
        val products = productRepository.findAll()
        
        val documents = products.map { convertToDocument(it) }
        productDocumentRepository.saveAll(documents)
        
        logger.info("Indexed ${documents.size} products to Elasticsearch")
    }

    /**
     * Delete product from Elasticsearch index
     */
    fun deleteFromIndex(productId: Long) {
        productDocumentRepository.deleteById(productId.toString())
        logger.info("Deleted product from index: $productId")
    }

    /**
     * Convert Product entity to ProductDocument for Elasticsearch
     */
    private fun convertToDocument(product: Product): ProductDocument {
        // Get specifications for this product
        val specifications = productSpecificationRepository.findByProductId(product.id!!)
        
        // Convert specifications to searchable format
        val specFields = specifications.map { spec ->
            SpecificationField(
                attributeName = spec.attribute.name,
                attributeDisplayName = spec.attribute.displayName,
                valueString = spec.valueString,
                valueNumeric = spec.valueNumeric,
                valueBoolean = spec.valueBoolean,
                unit = spec.attribute.unit
            )
        }
        
        // Build searchable text combining all relevant fields
        val searchableText = buildSearchableText(product, specifications)
        
        // Extract facet fields for easy filtering
        val facets = extractFacets(product, specifications)
        
        // Parse images from JSONB (assuming array of URLs)
        val images = parseImages(product.images)
        
        return ProductDocument(
            id = product.id.toString(),
            name = product.name,
            brand = product.brand,
            category = product.category.name,
            categoryId = product.category.id!!,
            price = product.price,
            discountPrice = product.discountPrice,
            description = product.description,
            sku = product.sku,
            stockQuantity = product.stockQuantity,
            inStock = product.stockQuantity > 0,
            model = product.model,
            images = images,
            specifications = specFields,
            searchableText = searchableText,
            tags = extractTags(product),
            facets = facets
        )
    }

    /**
     * Build searchable text combining all relevant fields
     */
    private fun buildSearchableText(
        product: Product,
        specifications: List<com.gearvn.ecommerce.entity.ProductSpecification>
    ): String {
        val parts = mutableListOf<String>()
        
        parts.add(product.name)
        product.brand?.let { parts.add(it) }
        product.model?.let { parts.add(it) }
        product.description?.let { parts.add(it) }
        parts.add(product.category.name)
        
        // Add specification values to searchable text
        specifications.forEach { spec ->
            spec.valueString?.let { parts.add(it) }
            spec.valueNumeric?.let { parts.add(it.toString()) }
        }
        
        return parts.joinToString(" ")
    }

    /**
     * Extract facet fields from specifications
     */
    private fun extractFacets(
        product: Product,
        specifications: List<com.gearvn.ecommerce.entity.ProductSpecification>
    ): FacetFields {
        val ramOptions = mutableListOf<String>()
        val processorFamily = mutableListOf<String>()
        val gpuFamily = mutableListOf<String>()
        val storageTypes = mutableListOf<String>()
        val colors = mutableListOf<String>()
        
        specifications.forEach { spec ->
            when (spec.attribute.name) {
                "ram_size" -> spec.valueNumeric?.let { 
                    ramOptions.add("${it.toInt()}GB")
                }
                "processor", "processor_brand" -> spec.valueString?.let { processorFamily.add(it) }
                "graphics_card" -> spec.valueString?.let { gpuFamily.add(it) }
                "storage_type" -> spec.valueString?.let { storageTypes.add(it) }
                "color" -> spec.valueString?.let { colors.add(it) }
            }
        }
        
        return FacetFields(
            brand = product.brand,
            ramOptions = ramOptions,
            processorFamily = processorFamily,
            gpuFamily = gpuFamily,
            storageTypes = storageTypes,
            colors = colors
        )
    }

    /**
     * Extract tags from product (can be enhanced with ML/NLP)
     */
    private fun extractTags(product: Product): List<String> {
        val tags = mutableListOf<String>()
        
        // Add category as tag
        tags.add(product.category.name.lowercase())
        
        // Extract tags from description/name
        val text = "${product.name} ${product.description}".lowercase()
        
        if (text.contains("gaming")) tags.add("gaming")
        if (text.contains("professional") || text.contains("business")) tags.add("business")
        if (text.contains("lightweight") || text.contains("portable")) tags.add("portable")
        if (text.contains("rgb")) tags.add("rgb")
        if (text.contains("high-performance")) tags.add("high-performance")
        
        return tags.distinct()
    }

    /**
     * Parse images from JSONB string using Jackson
     */
    private fun parseImages(imagesJson: String?): List<String> {
        if (imagesJson.isNullOrBlank()) return emptyList()
        
        return try {
            objectMapper.readValue<List<String>>(imagesJson)
        } catch (e: Exception) {
            logger.warn("Failed to parse images JSON: $imagesJson", e)
            emptyList()
        }
    }
}
