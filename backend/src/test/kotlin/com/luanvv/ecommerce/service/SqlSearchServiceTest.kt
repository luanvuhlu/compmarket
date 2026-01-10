package com.luanvv.ecommerce.service

import com.luanvv.ecommerce.dto.BrandFacet
import com.luanvv.ecommerce.dto.CategoryFacet
import com.luanvv.ecommerce.dto.PriceRangeFacet
import com.luanvv.ecommerce.dto.SearchRequest
import com.luanvv.ecommerce.dto.SpecificationFacet
import com.luanvv.ecommerce.dto.SpecificationValue
import com.luanvv.ecommerce.entity.Category
import com.luanvv.ecommerce.entity.Product
import com.luanvv.ecommerce.repository.ProductSearchRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
@DisplayName("SqlSearchService Tests")
class SqlSearchServiceTest {

    @MockK
    private lateinit var productSearchRepository: ProductSearchRepository

    @InjectMockKs
    private lateinit var sqlSearchService: SqlSearchService

    // Factory methods (must be placed before tests)
    private fun createCategory(
        id: Long = 1L,
        name: String = "Laptops"
    ) = Category(
        id = id,
        name = name,
        description = null,
        parentCategory = null,
        slug = "laptops",
        imageUrl = null
    )

    private fun createProduct(
        id: Long = 1L,
        category: Category = createCategory(),
        name: String = "Gaming Laptop X1",
        description: String? = "High-performance gaming laptop",
        sku: String = "LAP-001",
        price: BigDecimal = BigDecimal("1500.00"),
        discountPrice: BigDecimal? = BigDecimal("1350.00"),
        stockQuantity: Int = 10,
        brand: String? = "TechBrand",
        model: String? = "X1-2024",
        specifications: String? = "{\"cpu\": \"Intel i7\", \"ram\": \"16GB\"}",
        images: String? = "image1.jpg",
        isActive: Boolean = true
    ) = Product(
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

    private fun createSearchRequest(query: String? = null) = SearchRequest(query = query)

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("search")
    inner class SearchTests {

        @Test
        fun `should return mapped products and facets`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val product = createProduct(id = 1L)
            val page = PageImpl(listOf(product), pageable, 1)

            every { productSearchRepository.searchWithFilters(any(), pageable) } returns page

            val categoryFacets = listOf(CategoryFacet(categoryId = 1L, categoryName = "Laptops", count = 1L))
            val brandFacets = listOf(BrandFacet(brand = "TechBrand", count = 1L))
            val priceFacets = listOf(PriceRangeFacet(min = BigDecimal("1000.00"), max = BigDecimal("2000.00"), label = "1000-2000", count = 1L))
            val specFacets = listOf(SpecificationFacet(attributeName = "cpu", attributeDisplayName = "Processor", values = listOf(SpecificationValue(value = "Intel i7", count = 1L))))

            every { productSearchRepository.getCategoryFacets(any()) } returns categoryFacets
            every { productSearchRepository.getBrandFacets(any()) } returns brandFacets
            every { productSearchRepository.getPriceRangeFacets(any()) } returns priceFacets
            every { productSearchRepository.getSpecificationFacets(any()) } returns specFacets

            // Act
            val response = sqlSearchService.search(createSearchRequest(query = "laptop"), pageable)

            // Assert
            assertThat(response).isNotNull
            assertThat(response.products.content).hasSize(1)
            val first = response.products.content[0]
            assertThat(first.id).isEqualTo(1L)
            assertThat(first.name).isEqualTo("Gaming Laptop X1")
            assertThat(response.facets.categories).hasSize(1)
            assertThat(response.facets.brands).hasSize(1)
            assertThat(response.facets.priceRanges).hasSize(1)
            assertThat(response.facets.specifications).hasSize(1)
        }

        @Test
        fun `should return empty products when repository returns empty page`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val emptyPage = PageImpl<Product>(emptyList(), pageable, 0)

            every { productSearchRepository.searchWithFilters(any(), pageable) } returns emptyPage
            every { productSearchRepository.getCategoryFacets(any()) } returns emptyList()
            every { productSearchRepository.getBrandFacets(any()) } returns emptyList()
            every { productSearchRepository.getPriceRangeFacets(any()) } returns emptyList()
            every { productSearchRepository.getSpecificationFacets(any()) } returns emptyList()

            // Act
            val response = sqlSearchService.search(createSearchRequest(query = "nothing"), pageable)

            // Assert
            assertThat(response.products.content).isEmpty()
            assertThat(response.facets.categories).isEmpty()
            assertThat(response.facets.brands).isEmpty()
        }
    }

    @Nested
    @DisplayName("getAutoCompleteSuggestions")
    inner class AutoCompleteTests {

        @Test
        fun `should return unique suggestions limited by provided limit`() {
            // Arrange
            val prefix = "Gam"
            val limit = 2
            val pageableForRepo = PageRequest.of(0, limit * 2)

            val product1 = createProduct(id = 1L, name = "Gaming Laptop X1", brand = "GamerBrand")
            val product2 = createProduct(id = 2L, name = "Gamer Mouse", brand = "GamerBrand")
            val product3 = createProduct(id = 3L, name = "Office Chair", brand = "ChairCo")

            val repoPage = PageImpl(listOf(product1, product2, product3), pageableForRepo, 3)

            every { productSearchRepository.searchWithFilters(any(), any()) } returns repoPage

            // Act
            val suggestions = sqlSearchService.getAutoCompleteSuggestions(prefix, limit)

            // Assert
            // Should contain product names/brands that match prefix, deduplicated and limited
            assertThat(suggestions).hasSize(2)
            assertThat(suggestions).containsAnyOf("Gaming Laptop X1", "GamerBrand")
        }

        @Test
        fun `should return empty list when no products match prefix`() {
            // Arrange
            val prefix = "XYZ"
            val limit = 5
            val pageableForRepo = PageRequest.of(0, limit * 2)
            val repoPage = PageImpl<Product>(emptyList(), pageableForRepo, 0)

            every { productSearchRepository.searchWithFilters(any(), any()) } returns repoPage

            // Act
            val suggestions = sqlSearchService.getAutoCompleteSuggestions(prefix, limit)

            // Assert
            assertThat(suggestions).isEmpty()
        }
    }

    @Nested
    @DisplayName("getMoreLikeThis")
    inner class MoreLikeThisTests {

        @Test
        fun `should return empty list when productId is not numeric`() {
            // Act
            val result = sqlSearchService.getMoreLikeThis("not-a-number", 5)

            // Assert
            assertThat(result).isEmpty()
        }

        @Test
        fun `should parse id and return similar products based on categoryId list and inStock flag`() {
            // Arrange
            val productIdString = "5"
            val productIdLong = 5L
            val pageable = PageRequest.of(0, 10)
            val product = createProduct(id = 10L, category = createCategory(id = productIdLong))
            val page = PageImpl(listOf(product), pageable, 1)

            every { productSearchRepository.searchWithFilters(any(), any()) } returns page

            // Act
            val result = sqlSearchService.getMoreLikeThis(productIdString, 10)

            // Assert
            assertThat(result).hasSize(1)
            assertThat(result[0].id).isEqualTo(10L)
            assertThat(result[0].categoryId).isEqualTo(productIdLong)
        }
    }
}
