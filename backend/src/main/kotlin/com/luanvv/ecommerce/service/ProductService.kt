package com.luanvv.ecommerce.service

import com.luanvv.ecommerce.dto.PageResponse
import com.luanvv.ecommerce.dto.ProductCreateRequest
import com.luanvv.ecommerce.dto.ProductDetailResponse
import com.luanvv.ecommerce.dto.ProductResponse
import com.luanvv.ecommerce.dto.ProductSpecificationDto
import com.luanvv.ecommerce.entity.Product
import com.luanvv.ecommerce.event.ProductCreatedEvent
import com.luanvv.ecommerce.event.ProductDeletedEvent
import com.luanvv.ecommerce.event.ProductUpdatedEvent
import com.luanvv.ecommerce.exception.ResourceNotFoundException
import com.luanvv.ecommerce.logger
import com.luanvv.ecommerce.repository.CategoryRepository
import com.luanvv.ecommerce.repository.ProductRepository
import com.luanvv.ecommerce.repository.ProductSpecificationRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val productSpecificationRepository: ProductSpecificationRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = logger()

    @Cacheable(value = ["products"], key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    fun getAllProducts(pageable: Pageable): PageResponse<ProductResponse> {
        val page = productRepository.findAll(pageable)
        val products = page.content.map { it.toResponse() }
        log.info("Fetched ${products.size} products for page ${pageable.pageNumber}")
        val productPage = PageImpl(products, pageable, page.totalElements)
        return PageResponse.from(productPage)
    }

    @Cacheable(value = ["product"], key = "#id")
    fun getProductById(id: Long): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }
        return product.toResponse()
    }

    /**
     * Get product details with structured specifications (EAV attributes)
     * Use this for product detail page to get full information
     */
    @Cacheable(value = ["product-detail"], key = "#id")
    fun getProductDetailById(id: Long): ProductDetailResponse {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }
        
        val specifications = productSpecificationRepository.findByProductId(id)
            .map { spec ->
                ProductSpecificationDto(
                    attributeId = spec.attribute.id!!,
                    attributeName = spec.attribute.name,
                    displayName = spec.attribute.displayName,
                    dataType = spec.attribute.dataType.name,
                    unit = spec.attribute.unit,
                    valueString = spec.valueString,
                    valueNumeric = spec.valueNumeric,
                    valueBoolean = spec.valueBoolean
                )
            }
        
        return product.toDetailResponse(specifications)
    }

    /**
     * Get product specifications separately (useful for lazy loading)
     */
    @Cacheable(value = ["product-specs"], key = "#productId")
    fun getProductSpecifications(productId: Long): List<ProductSpecificationDto> {
        // Verify product exists
        if (!productRepository.existsById(productId)) {
            throw ResourceNotFoundException("Product not found with id: $productId")
        }
        
        return productSpecificationRepository.findByProductId(productId)
            .map { spec ->
                ProductSpecificationDto(
                    attributeId = spec.attribute.id!!,
                    attributeName = spec.attribute.name,
                    displayName = spec.attribute.displayName,
                    dataType = spec.attribute.dataType.name,
                    unit = spec.attribute.unit,
                    valueString = spec.valueString,
                    valueNumeric = spec.valueNumeric,
                    valueBoolean = spec.valueBoolean
                )
            }
    }

    fun searchProducts(keyword: String, pageable: Pageable): PageResponse<ProductResponse> {
        val page = productRepository.searchProducts(keyword, pageable)
        val products = page.content.map { it.toResponse() }
        val productPage = PageImpl(products, pageable, page.totalElements)
        return PageResponse.from(productPage)
    }

    /**
     * Full-text search using PostgreSQL's search_vector
     * Provides better search relevance and performance for text queries
     */
    @Cacheable(value = ["product-search"], key = "#query + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    fun fullTextSearch(query: String, pageable: Pageable): PageResponse<ProductResponse> {
        // Prepare query for PostgreSQL tsquery format
        val searchQuery = prepareFullTextQuery(query)

        val page = productRepository.fullTextSearch(searchQuery, pageable)
        val products = page.content.map { it.toResponse() }
        val productPage = PageImpl(products, pageable, page.totalElements)
        return PageResponse.from(productPage)
    }

    /**
     * Prepare query string for PostgreSQL full-text search
     * Converts "gaming laptop intel" to "gaming:* & laptop:* & intel:*"
     */
    private fun prepareFullTextQuery(query: String): String {
        return query.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(" & ") { word ->
                // Clean word and add wildcard for partial matching
                val cleanWord = word.replace("[^a-zA-Z0-9]".toRegex(), "")
                if (cleanWord.isNotEmpty()) "${cleanWord}:*" else ""
            }
            .replace(" & ", " & ")
            .trim()
            .takeIf { it.isNotEmpty() } ?: "''::tsquery"
    }

    fun getProductsByCategory(categoryId: Long, pageable: Pageable): PageResponse<ProductResponse> {
        if (!categoryRepository.existsById(categoryId)) {
            throw ResourceNotFoundException("Category not found with id: $categoryId")
        }
        val page = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable)
        val products = page.content.map { it.toResponse() }
        val productPage = PageImpl(products, pageable, page.totalElements)
        return PageResponse.from(productPage)
    }

    @Transactional
    @CacheEvict(value = ["products", "product", "product-detail", "product-specs"], allEntries = true)
    fun createProduct(request: ProductCreateRequest): ProductResponse {
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found") }

        val product = Product(
            category = category,
            name = request.name,
            description = request.description,
            sku = request.sku,
            price = request.price,
            discountPrice = request.discountPrice,
            stockQuantity = request.stockQuantity,
            brand = request.brand,
            model = request.model,
            specifications = request.specifications,
            images = request.images
        )

        val savedProduct = productRepository.save(product)
        
        // Publish event for Elasticsearch indexing
        eventPublisher.publishEvent(ProductCreatedEvent(savedProduct))
        
        return savedProduct.toResponse()
    }

    @Transactional
    @CacheEvict(value = ["products", "product", "product-detail", "product-specs"], allEntries = true)
    fun updateProduct(id: Long, request: ProductCreateRequest): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }

        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found") }

        product.apply {
            this.category = category
            name = request.name
            description = request.description
            sku = request.sku
            price = request.price
            discountPrice = request.discountPrice
            stockQuantity = request.stockQuantity
            brand = request.brand
            model = request.model
            specifications = request.specifications
            images = request.images
        }

        val updatedProduct = productRepository.save(product)
        
        // Publish event for Elasticsearch reindexing
        eventPublisher.publishEvent(ProductUpdatedEvent(updatedProduct))
        
        return updatedProduct.toResponse()
    }

    @Transactional
    @CacheEvict(value = ["products", "product", "product-detail", "product-specs"], allEntries = true)
    fun deleteProduct(id: Long) {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }
        product.isActive = false
        productRepository.save(product)
        
        // Publish event for Elasticsearch deletion
        eventPublisher.publishEvent(ProductDeletedEvent(id))
    }

    private fun Product.toResponse() = ProductResponse(
        id = this.id!!,
        categoryId = this.category.id!!,
        categoryName = this.category.name,
        name = this.name,
        description = this.description,
        sku = this.sku,
        price = this.price,
        discountPrice = this.discountPrice,
        stockQuantity = this.stockQuantity,
        brand = this.brand,
        model = this.model,
        specifications = this.specifications,
        images = this.images,
        isActive = this.isActive
    )

    private fun Product.toDetailResponse(specifications: List<ProductSpecificationDto>) = ProductDetailResponse(
        id = this.id!!,
        categoryId = this.category.id!!,
        categoryName = this.category.name,
        name = this.name,
        description = this.description,
        sku = this.sku,
        price = this.price,
        discountPrice = this.discountPrice,
        stockQuantity = this.stockQuantity,
        brand = this.brand,
        model = this.model,
        images = this.images,
        isActive = this.isActive,
        specifications = specifications
    )
}
