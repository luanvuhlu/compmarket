package com.luanvv.ecommerce.service

import com.luanvv.ecommerce.dto.*
import com.luanvv.ecommerce.entity.*
import com.luanvv.ecommerce.event.ProductCreatedEvent
import com.luanvv.ecommerce.event.ProductDeletedEvent
import com.luanvv.ecommerce.event.ProductUpdatedEvent
import com.luanvv.ecommerce.exception.ResourceNotFoundException
import com.luanvv.ecommerce.repository.CategoryRepository
import com.luanvv.ecommerce.repository.ProductRepository
import com.luanvv.ecommerce.repository.ProductSpecificationRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.util.*
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @MockK
    private lateinit var productRepository: ProductRepository

    @MockK
    private lateinit var categoryRepository: CategoryRepository

    @MockK
    private lateinit var productSpecificationRepository: ProductSpecificationRepository

    @MockK
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var productService: ProductService

    private lateinit var testCategory: Category
    private lateinit var testProduct: Product
    private lateinit var testAttribute: AttributeDefinition
    private lateinit var testProductSpec: ProductSpecification

    private fun makeProduct(
        id: Long = 1L,
        category: Category = testCategory,
        name: String = "Gaming Laptop X1",
        description: String = "High-performance gaming laptop",
        sku: String = "LAP-001",
        price: BigDecimal = BigDecimal("1500.00"),
        discountPrice: BigDecimal? = BigDecimal("1350.00"),
        stockQuantity: Int = 10,
        brand: String = "TechBrand",
        model: String = "X1-2024",
        specifications: String = """{"cpu": "Intel i7", "ram": "16GB"}""",
        images: String = "image1.jpg",
        isActive: Boolean = true
    ): Product = Product(
        id = id,
        category = category,
        name = name,
        description = description,
        sku = sku,
        price = price,
        discountPrice = discountPrice,
        stockQuantity = stockQuantity,
        brand = brand,
        model = model,
        specifications = specifications,
        images = images,
        isActive = isActive
    )

    @BeforeEach
    fun setUp() {
        testCategory = Category(
            id = 1L,
            name = "Laptops",
            description = "Gaming Laptops",
            parentCategory = null,
            slug = "laptop",
            imageUrl = null,
        )

        testProduct = makeProduct()

        testAttribute = AttributeDefinition(
            id = 1L,
            name = "cpu",
            displayName = "Processor",
            dataType = AttributeDataType.STRING,
            unit = null
        )

        testProductSpec = ProductSpecification(
            id = 1L,
            product = testProduct,
            attribute = testAttribute,
            valueString = "Intel i7-12700H"
        )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("getAllProducts")
    inner class GetAllProductsTests {

        @Test
        fun `should return paginated products successfully`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val products = listOf(testProduct)
            val page = PageImpl(products, pageable, 1)

            every { productRepository.findAll(pageable) } returns page

            // Act
            val result = productService.getAllProducts(pageable)

            // Assert
            assertThat(result).isNotNull
            assertThat(result.content).hasSize(1)
            assertThat(result.content[0].id).isEqualTo(1L)
            assertThat(result.content[0].name).isEqualTo("Gaming Laptop X1")
            assertThat(result.totalElements).isEqualTo(1)
            assertThat(result.totalPages).isEqualTo(1)

            verify(exactly = 1) { productRepository.findAll(pageable) }
        }

        @Test
        fun `should return empty page when no products exist`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val page = PageImpl<Product>(emptyList(), pageable, 0)

            every { productRepository.findAll(pageable) } returns page

            // Act
            val result = productService.getAllProducts(pageable)

            // Assert
            assertThat(result.content).isEmpty()
            assertThat(result.totalElements).isEqualTo(0)
            assertThat(result.totalPages).isEqualTo(0)
        }

        @Test
        fun `should handle large page numbers correctly`() {
            // Arrange
            val pageable = PageRequest.of(100, 10)
            val page = PageImpl<Product>(emptyList(), pageable, 50)

            every { productRepository.findAll(pageable) } returns page

            // Act
            val result = productService.getAllProducts(pageable)

            // Assert
            assertThat(result.content).isEmpty()
            assertThat(result.totalElements).isEqualTo(50)
        }
    }

    @Nested
    @DisplayName("getProductById")
    inner class GetProductByIdTests {

        @Test
        fun `should return product when id exists`() {
            // Arrange
            every { productRepository.findById(1L) } returns Optional.of(testProduct)

            // Act
            val result = productService.getProductById(1L)

            // Assert
            assertThat(result).isNotNull
            assertThat(result.id).isEqualTo(1L)
            assertThat(result.name).isEqualTo("Gaming Laptop X1")
            assertThat(result.sku).isEqualTo("LAP-001")
            assertThat(result.price).isEqualByComparingTo(BigDecimal("1500.00"))

            verify(exactly = 1) { productRepository.findById(1L) }
        }

        @Test
        fun `should throw ResourceNotFoundException when product does not exist`() {
            // Arrange
            every { productRepository.findById(999L) } returns Optional.empty()

            // Act & Assert
            assertThatThrownBy { productService.getProductById(999L) }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Product not found with id: 999")

            verify(exactly = 1) { productRepository.findById(999L) }
        }

        @ParameterizedTest
        @CsvSource("0", "-1", "-999")
        fun `should handle invalid product ids`(productId: Long) {
            // Arrange
            every { productRepository.findById(productId) } returns Optional.empty()

            // Act & Assert
            assertThatThrownBy { productService.getProductById(productId) }
                .isInstanceOf(ResourceNotFoundException::class.java)
        }
    }

    @Nested
    @DisplayName("getProductDetailById")
    inner class GetProductDetailByIdTests {

        @Test
        fun `should return product detail with specifications`() {
            // Arrange
            val specs = listOf(testProductSpec)
            every { productRepository.findById(1L) } returns Optional.of(testProduct)
            every { productSpecificationRepository.findByProductId(1L) } returns specs

            // Act
            val result = productService.getProductDetailById(1L)

            // Assert
            assertThat(result).isNotNull
            assertThat(result.id).isEqualTo(1L)
            assertThat(result.name).isEqualTo("Gaming Laptop X1")
            assertThat(result.specifications).hasSize(1)
            assertThat(result.specifications[0].attributeName).isEqualTo("cpu")
            assertThat(result.specifications[0].displayName).isEqualTo("Processor")
            assertThat(result.specifications[0].valueString).isEqualTo("Intel i7-12700H")

            verify(exactly = 1) { productRepository.findById(1L) }
            verify(exactly = 1) { productSpecificationRepository.findByProductId(1L) }
        }

        @Test
        fun `should return product detail with empty specifications list`() {
            // Arrange
            every { productRepository.findById(1L) } returns Optional.of(testProduct)
            every { productSpecificationRepository.findByProductId(1L) } returns emptyList()

            // Act
            val result = productService.getProductDetailById(1L)

            // Assert
            assertThat(result.specifications).isEmpty()
        }

        @Test
        fun `should throw exception when product not found for detail`() {
            // Arrange
            every { productRepository.findById(999L) } returns Optional.empty()

            // Act & Assert
            assertThatThrownBy { productService.getProductDetailById(999L) }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Product not found with id: 999")
        }
    }

    @Nested
    @DisplayName("getProductSpecifications")
    inner class GetProductSpecificationsTests {

        @Test
        fun `should return specifications for existing product`() {
            // Arrange
            val specs = listOf(testProductSpec)
            every { productRepository.existsById(1L) } returns true
            every { productSpecificationRepository.findByProductId(1L) } returns specs

            // Act
            val result = productService.getProductSpecifications(1L)

            // Assert
            assertThat(result).hasSize(1)
            assertThat(result[0].attributeName).isEqualTo("cpu")
            assertThat(result[0].valueString).isEqualTo("Intel i7-12700H")
        }

        @Test
        fun `should return empty list when product has no specifications`() {
            // Arrange
            every { productRepository.existsById(1L) } returns true
            every { productSpecificationRepository.findByProductId(1L) } returns emptyList()

            // Act
            val result = productService.getProductSpecifications(1L)

            // Assert
            assertThat(result).isEmpty()
        }

        @Test
        fun `should throw exception when product does not exist`() {
            // Arrange
            every { productRepository.existsById(999L) } returns false

            // Act & Assert
            assertThatThrownBy { productService.getProductSpecifications(999L) }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Product not found with id: 999")

            verify(exactly = 0) { productSpecificationRepository.findByProductId(any()) }
        }
    }

    @Nested
    @DisplayName("searchProducts")
    inner class SearchProductsTests {

        @Test
        fun `should return products matching keyword`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val products = listOf(testProduct)
            val page = PageImpl(products, pageable, 1)

            every { productRepository.searchProducts("gaming", pageable) } returns page

            // Act
            val result = productService.searchProducts("gaming", pageable)

            // Assert
            assertThat(result.content).hasSize(1)
            assertThat(result.content[0].name).contains("Gaming")
        }

        @Test
        fun `should return empty result for non-matching keyword`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val page = PageImpl<Product>(emptyList(), pageable, 0)

            every { productRepository.searchProducts("nonexistent", pageable) } returns page

            // Act
            val result = productService.searchProducts("nonexistent", pageable)

            // Assert
            assertThat(result.content).isEmpty()
        }

        @ParameterizedTest
        @CsvSource(
            "'',",
            "' ',",
            "'   ',"
        )
        fun `should handle empty or whitespace keywords`(keyword: String) {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val page = PageImpl<Product>(emptyList(), pageable, 0)

            every { productRepository.searchProducts(keyword, pageable) } returns page

            // Act
            val result = productService.searchProducts(keyword, pageable)

            // Assert
            assertThat(result.content).isEmpty()
        }
    }

    @Nested
    @DisplayName("fullTextSearch")
    inner class FullTextSearchTests {

        @Test
        fun `should perform full-text search successfully`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val products = listOf(testProduct)
            val page = PageImpl(products, pageable, 1)

            every { productRepository.fullTextSearch(any(), pageable) } returns page

            // Act
            val result = productService.fullTextSearch("gaming laptop intel", pageable)

            // Assert
            assertThat(result.content).hasSize(1)
            verify(exactly = 1) { productRepository.fullTextSearch(match { it.contains("gaming:*") }, pageable) }
        }

        @Test
        fun `should prepare full-text query correctly for single case`() {
            val pageable = PageRequest.of(0, 10)
            val page = PageImpl<Product>(emptyList(), pageable, 0)
            val capturedQuery = slot<String>()

            every { productRepository.fullTextSearch(capture(capturedQuery), pageable) } returns page

            // Act
            productService.fullTextSearch("gaming laptop intel", pageable)

            // Assert
            assertThat(capturedQuery.captured).contains("gaming:*")
        }

        @Test
        fun `should handle empty query string`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val page = PageImpl<Product>(emptyList(), pageable, 0)

            every { productRepository.fullTextSearch(any(), pageable) } returns page

            // Act
            val result = productService.fullTextSearch("", pageable)

            // Assert
            assertThat(result.content).isEmpty()
        }

        @Test
        fun `should handle query with special characters`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val page = PageImpl<Product>(emptyList(), pageable, 0)

            every { productRepository.fullTextSearch(any(), pageable) } returns page

            // Act
            val result = productService.fullTextSearch("laptop@#$%gaming", pageable)

            // Assert
            verify(exactly = 1) { productRepository.fullTextSearch(match { it.contains("laptopgaming:*") }, pageable) }
        }
    }

    @Nested
    @DisplayName("getProductsByCategory")
    inner class GetProductsByCategoryTests {

        @Test
        fun `should return products for valid category`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val products = listOf(testProduct)
            val page = PageImpl(products, pageable, 1)

            every { categoryRepository.existsById(1L) } returns true
            every { productRepository.findByCategoryIdAndIsActiveTrue(1L, pageable) } returns page

            // Act
            val result = productService.getProductsByCategory(1L, pageable)

            // Assert
            assertThat(result.content).hasSize(1)
            assertThat(result.content[0].categoryId).isEqualTo(1L)
        }

        @Test
        fun `should throw exception when category does not exist`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            every { categoryRepository.existsById(999L) } returns false

            // Act & Assert
            assertThatThrownBy { productService.getProductsByCategory(999L, pageable) }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Category not found with id: 999")

            verify(exactly = 0) { productRepository.findByCategoryIdAndIsActiveTrue(any(), any()) }
        }

        @Test
        fun `should return empty page when category has no active products`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val page = PageImpl<Product>(emptyList(), pageable, 0)

            every { categoryRepository.existsById(1L) } returns true
            every { productRepository.findByCategoryIdAndIsActiveTrue(1L, pageable) } returns page

            // Act
            val result = productService.getProductsByCategory(1L, pageable)

            // Assert
            assertThat(result.content).isEmpty()
        }
    }

    @Nested
    @DisplayName("createProduct")
    inner class CreateProductTests {

        private lateinit var createRequest: ProductCreateRequest

        @BeforeEach
        fun setupCreateRequest() {
            createRequest = ProductCreateRequest(
                categoryId = 1L,
                name = "New Gaming Laptop",
                description = "Latest gaming laptop",
                sku = "LAP-002",
                price = BigDecimal("2000.00"),
                discountPrice = BigDecimal("1800.00"),
                stockQuantity = 5,
                brand = "TechBrand",
                model = "X2-2024",
                specifications = """{"cpu": "Intel i9"}""",
                images = "image1.jpg"
            )
        }

        @Test
        fun `should create product successfully and publish event`() {
            // Arrange
            val savedProduct = makeProduct(id = 2L)
            val eventSlot = slot<ProductCreatedEvent>()

            every { categoryRepository.findById(1L) } returns Optional.of(testCategory)
            every { productRepository.save(any<Product>()) } returns savedProduct
            every { eventPublisher.publishEvent(capture(eventSlot)) } just Runs

            // Act
            val result = productService.createProduct(createRequest)

            // Assert
            assertThat(result).isNotNull
            assertThat(result.id).isEqualTo(2L)
            assertThat(result.name).isEqualTo("Gaming Laptop X1")

            verify(exactly = 1) { categoryRepository.findById(1L) }
            verify(exactly = 1) { productRepository.save(any<Product>()) }
            verify(exactly = 1) { eventPublisher.publishEvent(any<ProductCreatedEvent>()) }

            assertThat(eventSlot.captured.product).isEqualTo(savedProduct)
        }

        @Test
        fun `should throw exception when category not found during creation`() {
            // Arrange
            every { categoryRepository.findById(1L) } returns Optional.empty()

            // Act & Assert
            assertThatThrownBy { productService.createProduct(createRequest) }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Category not found")

            verify(exactly = 0) { productRepository.save(any<Product>()) }
            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }

        @Test
        fun `should handle null discount price`() {
            // Arrange
            val requestWithNullDiscount = createRequest.copy(discountPrice = null)
            val savedProduct = makeProduct(discountPrice = null)

            every { categoryRepository.findById(1L) } returns Optional.of(testCategory)
            every { productRepository.save(any<Product>()) } returns savedProduct
            every { eventPublisher.publishEvent(any<ProductCreatedEvent>()) } just Runs

            // Act
            val result = productService.createProduct(requestWithNullDiscount)

            // Assert
            assertThat(result.discountPrice).isNull()
        }

        @Test
        fun `should create product with zero stock quantity`() {
            // Arrange
            val requestWithZeroStock = createRequest.copy(stockQuantity = 0)
            val savedProduct = makeProduct(stockQuantity = 0)

            every { categoryRepository.findById(1L) } returns Optional.of(testCategory)
            every { productRepository.save(any<Product>()) } returns savedProduct
            every { eventPublisher.publishEvent(any<ProductCreatedEvent>()) } just Runs

            // Act
            val result = productService.createProduct(requestWithZeroStock)

            // Assert
            assertThat(result.stockQuantity).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("updateProduct")
    inner class UpdateProductTests {

        private lateinit var updateRequest: ProductCreateRequest

        @BeforeEach
        fun setupUpdateRequest() {
            updateRequest = ProductCreateRequest(
                categoryId = 1L,
                name = "Updated Gaming Laptop",
                description = "Updated description",
                sku = "LAP-001-UPD",
                price = BigDecimal("1600.00"),
                discountPrice = BigDecimal("1400.00"),
                stockQuantity = 15,
                brand = "TechBrand",
                model = "X1-2024-Updated",
                specifications = """{"cpu": "Intel i9"}""",
                images = "updated_image.jpg"
            )
        }

        @Test
        fun `should update product successfully and publish event`() {
            // Arrange
            val updatedProduct = testProduct.apply {
                name = updateRequest.name
                price = updateRequest.price
            }
            val eventSlot = slot<ProductUpdatedEvent>()

            every { productRepository.findById(1L) } returns Optional.of(testProduct)
            every { categoryRepository.findById(1L) } returns Optional.of(testCategory)
            every { productRepository.save(any<Product>()) } returns updatedProduct
            every { eventPublisher.publishEvent(capture(eventSlot)) } just Runs

            // Act
            val result = productService.updateProduct(1L, updateRequest)

            // Assert
            assertThat(result).isNotNull
            verify(exactly = 1) { productRepository.findById(1L) }
            verify(exactly = 1) { productRepository.save(any<Product>()) }
            verify(exactly = 1) { eventPublisher.publishEvent(any<ProductUpdatedEvent>()) }

            assertThat(eventSlot.captured.product.id).isEqualTo(1L)
        }

        @Test
        fun `should throw exception when product not found during update`() {
            // Arrange
            every { productRepository.findById(999L) } returns Optional.empty()

            // Act & Assert
            assertThatThrownBy { productService.updateProduct(999L, updateRequest) }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Product not found with id: 999")

            verify(exactly = 0) { productRepository.save(any<Product>()) }
        }

        @Test
        fun `should throw exception when category not found during update`() {
            // Arrange
            every { productRepository.findById(1L) } returns Optional.of(testProduct)
            every { categoryRepository.findById(999L) } returns Optional.empty()

            val requestWithInvalidCategory = updateRequest.copy(categoryId = 999L)

            // Act & Assert
            assertThatThrownBy { productService.updateProduct(1L, requestWithInvalidCategory) }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Category not found")

            verify(exactly = 0) { productRepository.save(any<Product>()) }
        }

        @Test
        fun `should update all product fields correctly`() {
            // Arrange
            val productSlot = slot<Product>()

            every { productRepository.findById(1L) } returns Optional.of(testProduct)
            every { categoryRepository.findById(1L) } returns Optional.of(testCategory)
            every { productRepository.save(capture(productSlot)) } answers { productSlot.captured }
            every { eventPublisher.publishEvent(any<ProductUpdatedEvent>()) } just Runs

            // Act
            productService.updateProduct(1L, updateRequest)

            // Assert
            val capturedProduct = productSlot.captured
            assertThat(capturedProduct.name).isEqualTo(updateRequest.name)
            assertThat(capturedProduct.description).isEqualTo(updateRequest.description)
            assertThat(capturedProduct.sku).isEqualTo(updateRequest.sku)
            assertThat(capturedProduct.price).isEqualByComparingTo(updateRequest.price)
            assertThat(capturedProduct.stockQuantity).isEqualTo(updateRequest.stockQuantity)
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    inner class DeleteProductTests {

        @Test
        fun `should soft delete product and publish event`() {
            // Arrange
            val eventSlot = slot<ProductDeletedEvent>()

            every { productRepository.findById(1L) } returns Optional.of(testProduct)
            every { productRepository.save(any<Product>()) } answers { firstArg() }
            every { eventPublisher.publishEvent(capture(eventSlot)) } just Runs

            // Act
            productService.deleteProduct(1L)

            // Assert
            assertThat(testProduct.isActive).isFalse()

            verify(exactly = 1) { productRepository.findById(1L) }
            verify(exactly = 1) { productRepository.save(match { !it.isActive }) }
            verify(exactly = 1) { eventPublisher.publishEvent(any<ProductDeletedEvent>()) }

            assertThat(eventSlot.captured.productId).isEqualTo(1L)
        }

        @Test
        fun `should throw exception when deleting non-existent product`() {
            // Arrange
            every { productRepository.findById(999L) } returns Optional.empty()

            // Act & Assert
            assertThatThrownBy { productService.deleteProduct(999L) }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Product not found with id: 999")

            verify(exactly = 0) { productRepository.save(any<Product>()) }
            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }

        @Test
        fun `should not fail when deleting already inactive product`() {
            // Arrange
            val inactiveProduct = makeProduct(isActive = false)
            every { productRepository.findById(1L) } returns Optional.of(inactiveProduct)
            every { productRepository.save(any<Product>()) } answers { firstArg() }
            every { eventPublisher.publishEvent(any<ProductDeletedEvent>()) } just Runs

            // Act
            productService.deleteProduct(1L)

            // Assert
            verify(exactly = 1) { productRepository.save(match { !it.isActive }) }
        }
    }

    @Nested
    @DisplayName("Event Publishing")
    inner class EventPublishingTests {

        @Test
        fun `should publish ProductCreatedEvent with correct data`() {
            // Arrange
            val request = ProductCreateRequest(
                categoryId = 1L,
                name = "Test Product",
                description = "Test",
                sku = "TEST-001",
                price = BigDecimal("100.00"),
                discountPrice = null,
                stockQuantity = 10,
                brand = "TestBrand",
                model = "T1",
                specifications = "{}",
                images = ""
            )
            val savedProduct = testProduct
            val eventSlot = slot<ProductCreatedEvent>()

            every { categoryRepository.findById(1L) } returns Optional.of(testCategory)
            every { productRepository.save(any<Product>()) } returns savedProduct
            every { eventPublisher.publishEvent(capture(eventSlot)) } just Runs

            // Act
            productService.createProduct(request)

            // Assert
            assertThat(eventSlot.captured).isNotNull
            assertThat(eventSlot.captured.product).isEqualTo(savedProduct)
        }

        @Test
        fun `should publish ProductUpdatedEvent with correct data`() {
            // Arrange
            val request = ProductCreateRequest(
                categoryId = 1L,
                name = "Updated",
                description = "Updated",
                sku = "UPD-001",
                price = BigDecimal("200.00"),
                discountPrice = null,
                stockQuantity = 5,
                brand = "Brand",
                model = "M1",
                specifications = "{}",
                images = ""
            )
            val eventSlot = slot<ProductUpdatedEvent>()

            every { productRepository.findById(1L) } returns Optional.of(testProduct)
            every { categoryRepository.findById(1L) } returns Optional.of(testCategory)
            every { productRepository.save(any<Product>()) } answers { firstArg() }
            every { eventPublisher.publishEvent(capture(eventSlot)) } just Runs

            // Act
            productService.updateProduct(1L, request)

            // Assert
            assertThat(eventSlot.captured).isNotNull
            assertThat(eventSlot.captured.product.id).isEqualTo(1L)
        }

        @Test
        fun `should publish ProductDeletedEvent with correct id`() {
            // Arrange
            val eventSlot = slot<ProductDeletedEvent>()

            every { productRepository.findById(1L) } returns Optional.of(testProduct)
            every { productRepository.save(any<Product>()) } answers { firstArg() }
            every { eventPublisher.publishEvent(capture(eventSlot)) } just Runs

            // Act
            productService.deleteProduct(1L)

            // Assert
            assertThat(eventSlot.captured.productId).isEqualTo(1L)
        }
    }
}
