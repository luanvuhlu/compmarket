package com.luanvv.ecommerce.elasticsearch

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.math.BigDecimal

/**
 * Elasticsearch document for product search
 * Denormalized structure for fast querying
 */
@Document(indexName = "products")
data class ProductDocument(
    @Id
    val id: String,
    
    @Field(type = FieldType.Text, analyzer = "standard")
    val name: String,
    
    @Field(type = FieldType.Keyword)
    val brand: String?,
    
    @Field(type = FieldType.Keyword)
    val category: String,
    
    @Field(type = FieldType.Long)
    val categoryId: Long,
    
    @Field(type = FieldType.Double)
    val price: BigDecimal,
    
    @Field(type = FieldType.Double)
    val discountPrice: BigDecimal?,
    
    @Field(type = FieldType.Text, analyzer = "standard")
    val description: String?,
    
    @Field(type = FieldType.Keyword)
    val sku: String,
    
    @Field(type = FieldType.Integer)
    val stockQuantity: Int,
    
    @Field(type = FieldType.Boolean)
    val inStock: Boolean,
    
    @Field(type = FieldType.Keyword)
    val model: String?,
    
    @Field(type = FieldType.Text)
    val images: List<String> = emptyList(),
    
    // Specifications as nested objects for filtering
    @Field(type = FieldType.Nested)
    val specifications: List<SpecificationField> = emptyList(),
    
    // Searchable text combining all relevant fields
    @Field(type = FieldType.Text, analyzer = "standard")
    val searchableText: String,
    
    @Field(type = FieldType.Keyword)
    val tags: List<String> = emptyList(),
    
    // Facet fields for easy filtering
    @Field(type = FieldType.Object)
    val facets: FacetFields
)

/**
 * Nested specification field for filtering
 */
data class SpecificationField(
    @Field(type = FieldType.Keyword)
    val attributeName: String,
    
    @Field(type = FieldType.Text)
    val attributeDisplayName: String,
    
    @Field(type = FieldType.Text)
    val valueString: String?,
    
    @Field(type = FieldType.Double)
    val valueNumeric: BigDecimal?,
    
    @Field(type = FieldType.Boolean)
    val valueBoolean: Boolean?,
    
    @Field(type = FieldType.Keyword)
    val unit: String?
)

/**
 * Pre-computed facet fields for aggregations
 */
data class FacetFields(
    @Field(type = FieldType.Keyword)
    val brand: String?,
    
    @Field(type = FieldType.Keyword)
    val ramOptions: List<String> = emptyList(),
    
    @Field(type = FieldType.Keyword)
    val processorFamily: List<String> = emptyList(),
    
    @Field(type = FieldType.Keyword)
    val gpuFamily: List<String> = emptyList(),
    
    @Field(type = FieldType.Keyword)
    val storageTypes: List<String> = emptyList(),
    
    @Field(type = FieldType.Keyword)
    val colors: List<String> = emptyList()
)
