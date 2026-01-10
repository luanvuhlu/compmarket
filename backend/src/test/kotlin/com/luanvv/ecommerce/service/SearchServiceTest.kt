package com.luanvv.ecommerce.service

import com.luanvv.ecommerce.dto.PageResponse
import com.luanvv.ecommerce.dto.ProductResponse
import com.luanvv.ecommerce.dto.SearchFacets
import com.luanvv.ecommerce.dto.SearchRequest
import com.luanvv.ecommerce.elasticsearch.ElasticsearchService
import com.luanvv.ecommerce.elasticsearch.FacetFields
import com.luanvv.ecommerce.elasticsearch.ProductDocument
import com.luanvv.ecommerce.elasticsearch.SpecificationField
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class SearchServiceTest {

    @MockK
    private lateinit var elasticsearchService: ElasticsearchService

    @InjectMockKs
    private lateinit var searchService: SearchService

    // Factory methods (must be above tests)
    private fun createFacetFields(
        brand: String? = "TechBrand",
        ramOptions: List<String> = emptyList(),
        processorFamily: List<String> = emptyList(),
        gpuFamily: List<String> = emptyList(),
        storageTypes: List<String> = emptyList(),
        colors: List<String> = emptyList()
    ) = FacetFields(
        brand = brand,
        ramOptions = ramOptions,
        processorFamily = processorFamily,
        gpuFamily = gpuFamily,
        storageTypes = storageTypes,
        colors = colors
    )

    private fun createSpecificationField(
        attributeName: String = "ram_size",
        attributeDisplayName: String = "RAM",
        valueString: String? = null,
        valueNumeric: BigDecimal? = null,
        valueBoolean: Boolean? = null,
        unit: String? = "GB"
    ) = SpecificationField(
        attributeName = attributeName,
        attributeDisplayName = attributeDisplayName,
        valueString = valueString,
        valueNumeric = valueNumeric,
        valueBoolean = valueBoolean,
        unit = unit
    )

    private fun createProductDocument(
        id: String = "1",
        name: String = "Gaming Laptop",
        brand: String? = "TechBrand",
        category: String = "Laptops",
        categoryId: Long = 1L,
        price: BigDecimal = BigDecimal("1000.00"),
        discountPrice: BigDecimal? = null,
        description: String? = "Nice laptop",
        sku: String = "SKU-1",
        stockQuantity: Int = 5,
        inStock: Boolean = true,
        model: String? = "M1",
        images: List<String> = listOf("http://img1.jpg"),
        specifications: List<SpecificationField> = emptyList(),
        searchableText: String = "Gaming Laptop",
        tags: List<String> = listOf("laptops"),
        facets: FacetFields = createFacetFields()
    ) = ProductDocument(
        id = id,
        name = name,
        brand = brand,
        category = category,
        categoryId = categoryId,
        price = price,
        discountPrice = discountPrice,
        description = description,
        sku = sku,
        stockQuantity = stockQuantity,
        inStock = inStock,
        model = model,
        images = images,
        specifications = specifications,
        searchableText = searchableText,
        tags = tags,
        facets = facets
    )

    private fun createSearchRequest(query: String? = null) = SearchRequest(query = query)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should map search results to SearchResponse with page metadata`() {
        // Arrange
        val pageable: Pageable = PageRequest.of(0, 10)
        val doc = createProductDocument(id = "10", name = "Gaming Laptop X1", images = listOf("http://a.jpg"))
        val page = PageImpl(listOf(doc), pageable, 1)
        val request = createSearchRequest(query = "gaming")

        every { elasticsearchService.search(any(), any()) } returns page

        // Act
        val response = searchService.search(request, pageable)

        // Assert
        assertThat(response).isNotNull
        val productsPage: PageResponse<ProductResponse> = response.products
        assertThat(productsPage.content).hasSize(1)
        val product = productsPage.content[0]
        assertThat(product.id).isEqualTo(10L)
        assertThat(product.name).isEqualTo("Gaming Laptop X1")
        assertThat(productsPage.totalElements).isEqualTo(1)
        assertThat(productsPage.size).isEqualTo(10)
        assertThat(response.facets).isInstanceOf(SearchFacets::class.java)

        verify(exactly = 1) { elasticsearchService.search(any(), any()) }
    }

    @Test
    fun `should return empty products when elasticsearch returns empty page`() {
        // Arrange
        val pageable: Pageable = PageRequest.of(0, 10)
        val emptyPage = PageImpl<ProductDocument>(emptyList(), pageable, 0)
        every { elasticsearchService.search(any(), any()) } returns emptyPage

        // Act
        val response = searchService.search(createSearchRequest(), pageable)

        // Assert
        assertThat(response.products.content).isEmpty()
        assertThat(response.facets.categories).isEmpty()
    }

    @Test
    fun `should delegate autocomplete to elasticsearch service`() {
        // Arrange
        every { elasticsearchService.getAutoCompleteSuggestions("pre", 5) } returns listOf("pre1", "pre2")

        // Act
        val suggestions = searchService.getAutoCompleteSuggestions("pre", 5)

        // Assert
        assertThat(suggestions).containsExactly("pre1", "pre2")
        verify(exactly = 1) { elasticsearchService.getAutoCompleteSuggestions("pre", 5) }
    }

    @Test
    fun `should map more like this results to ProductResponse list`() {
        // Arrange
        val doc1 = createProductDocument(id = "100", name = "Mouse A")
        val doc2 = createProductDocument(id = "101", name = "Mouse B")
        every { elasticsearchService.getMoreLikeThis("100", 2) } returns listOf(doc1, doc2)

        // Act
        val results = searchService.getMoreLikeThis("100", 2)

        // Assert
        assertThat(results).hasSize(2)
        assertThat(results[0].id).isEqualTo(100L)
        assertThat(results[0].name).isEqualTo("Mouse A")
        verify(exactly = 1) { elasticsearchService.getMoreLikeThis("100", 2) }
    }
}

