package com.gearvn.ecommerce.service

import com.gearvn.ecommerce.dto.ProductCreateRequest
import com.gearvn.ecommerce.dto.ProductResponse
import com.gearvn.ecommerce.entity.Product
import com.gearvn.ecommerce.exception.ResourceNotFoundException
import com.gearvn.ecommerce.repository.CategoryRepository
import com.gearvn.ecommerce.repository.ProductRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {

    @Cacheable(value = ["products"], key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    fun getAllProducts(pageable: Pageable): Page<ProductResponse> {
        return productRepository.findByIsActiveTrue(pageable)
            .map { it.toResponse() }
    }

    @Cacheable(value = ["product"], key = "#id")
    fun getProductById(id: Long): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }
        return product.toResponse()
    }

    fun searchProducts(keyword: String, pageable: Pageable): Page<ProductResponse> {
        return productRepository.searchProducts(keyword, pageable)
            .map { it.toResponse() }
    }

    fun getProductsByCategory(categoryId: Long, pageable: Pageable): Page<ProductResponse> {
        if (!categoryRepository.existsById(categoryId)) {
            throw ResourceNotFoundException("Category not found with id: $categoryId")
        }
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable)
            .map { it.toResponse() }
    }

    @Transactional
    @CacheEvict(value = ["products", "product"], allEntries = true)
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

        return productRepository.save(product).toResponse()
    }

    @Transactional
    @CacheEvict(value = ["products", "product"], allEntries = true)
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

        return productRepository.save(product).toResponse()
    }

    @Transactional
    @CacheEvict(value = ["products", "product"], allEntries = true)
    fun deleteProduct(id: Long) {
        val product = productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }
        product.isActive = false
        productRepository.save(product)
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
}
