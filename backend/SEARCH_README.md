# Advanced Product Search Feature

## Overview

This e-commerce platform now includes an advanced product search system using a hybrid architecture:
- **PostgreSQL with EAV (Entity-Attribute-Value)** for flexible product specifications
- **Elasticsearch** for fast, full-text search with natural language support
- **Faceted search** with dynamic filtering options

## Features

### 🔍 Search Capabilities
- **Natural Language Search**: "Asus i7 16GB RAM gaming laptop"
- **Structured Filters**: Category, brand, price range, RAM, storage, etc.
- **Faceted Search**: Dynamic filter options with product counts
- **Auto-Complete**: Real-time suggestions as users type
- **Similar Products**: "More Like This" recommendations
- **Typo Tolerance**: Handles common spelling mistakes (when Elasticsearch is enabled)

### 📊 Product Specifications (EAV Model)
- **Flexible Attributes**: Add any product specification without schema changes
- **Typed Values**: String, numeric, boolean, and enum types
- **Filterable Attributes**: Mark attributes as filterable or searchable
- **Predefined Options**: Dropdown values for enum types

## Database Schema

### Attribute Definitions
```sql
-- Defines available attributes
attribute_definitions (
    attribute_id, name, display_name, data_type,
    unit, is_filterable, is_searchable, sort_order
)
```

### Attribute Options
```sql
-- Predefined values for ENUM attributes
attribute_options (
    option_id, attribute_id, option_value, display_label
)
```

### Product Specifications
```sql
-- Links products to their specifications (EAV)
product_specifications (
    spec_id, product_id, attribute_id,
    value_string, value_numeric, value_boolean
)
```

## API Endpoints

### 1. Advanced Search (POST)
```http
POST /api/search
Content-Type: application/json

{
  "query": "gaming laptop",
  "categoryIds": [5],
  "brands": ["Asus", "Dell"],
  "minPrice": 1000,
  "maxPrice": 2000,
  "inStock": true,
  "sortBy": "PRICE",
  "sortOrder": "ASC"
}
```

**Response:**
```json
{
  "products": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  },
  "facets": {
    "categories": [...],
    "brands": [...],
    "priceRanges": [...]
  }
}
```

### 2. Simple Search (GET)
```http
GET /api/search?query=asus&minPrice=500&maxPrice=1500&page=0&size=20
```

### 3. Auto-Complete
```http
GET /api/search/autocomplete?prefix=asu&limit=10
```

**Response:**
```json
["Asus", "Asus ROG", "Asus TUF Gaming", "Asus VivoBook"]
```

### 4. Similar Products
```http
GET /api/search/similar/101?limit=10
```

## Search Options

### Sort Options
- `RELEVANCE` - Sort by relevance score (default)
- `PRICE` - Sort by price
- `NAME` - Sort by product name
- `NEWEST` - Sort by creation date

### Sort Order
- `ASC` - Ascending order
- `DESC` - Descending order (default)

## Predefined Attributes

The system comes pre-configured with common computer/laptop specifications:

| Attribute | Type | Unit | Filterable |
|-----------|------|------|------------|
| processor | STRING | - | ✓ |
| processor_brand | ENUM | - | ✓ |
| ram_size | NUMERIC | GB | ✓ |
| ram_type | ENUM | - | ✓ |
| storage_capacity | NUMERIC | GB | ✓ |
| storage_type | ENUM | - | ✓ |
| graphics_card | STRING | - | ✓ |
| screen_size | NUMERIC | inches | ✓ |
| screen_resolution | ENUM | - | ✓ |
| operating_system | ENUM | - | ✓ |
| warranty | NUMERIC | months | ✗ |
| color | ENUM | - | ✓ |
| weight | NUMERIC | kg | ✗ |

## Configuration

### Enable/Disable Elasticsearch

In `application.yml`:
```yaml
app:
  elasticsearch:
    enabled: false  # Set to true to enable Elasticsearch
    uris: http://localhost:9200
```

**Note**: When Elasticsearch is disabled, the search feature will fall back to PostgreSQL-based search using the existing `ProductRepository` search methods.

## Docker Setup

The `docker-compose.yml` includes Elasticsearch service:

```yaml
elasticsearch:
  image: docker.elastic.co/elasticsearch/elasticsearch:8.11.1
  environment:
    - discovery.type=single-node
    - xpack.security.enabled=false
  ports:
    - "9200:9200"
```

To start all services including Elasticsearch:
```bash
docker-compose up -d
```

## Indexing Products to Elasticsearch

### Manual Indexing

Use the `ProductIndexService` to index products:

```kotlin
// Index a single product
productIndexService.indexProduct(productId)

// Index all products
productIndexService.indexAllProducts()

// Delete from index
productIndexService.deleteFromIndex(productId)
```

### Automatic Indexing

**TODO**: Implement automatic indexing on product create/update/delete events.

## Data Synchronization

Products need to be synchronized between PostgreSQL and Elasticsearch:

1. **PostgreSQL** - Source of truth for transactional data
2. **Elasticsearch** - Denormalized search index

### Sync Strategy

```
Product CRUD → PostgreSQL → ProductIndexService → Elasticsearch
```

### When to Re-index

- After creating a new product
- After updating product details
- After adding/updating specifications
- After bulk imports
- After Elasticsearch recovery

## Example: Adding Product Specifications

```kotlin
// 1. Get attribute definitions
val ramAttribute = attributeDefinitionRepository.findByName("ram_size").get()
val processorAttribute = attributeDefinitionRepository.findByName("processor").get()

// 2. Create specifications
val ramSpec = ProductSpecification(
    product = product,
    attribute = ramAttribute,
    valueNumeric = BigDecimal("16")  // 16 GB
)

val processorSpec = ProductSpecification(
    product = product,
    attribute = processorAttribute,
    valueString = "Intel Core i7-12700H"
)

// 3. Save specifications
productSpecificationRepository.saveAll(listOf(ramSpec, processorSpec))

// 4. Index to Elasticsearch
productIndexService.indexProduct(product.id!!)
```

## Performance Considerations

### PostgreSQL
- **EAV Queries**: Can be slow for complex filtering
- **Indexes**: GIN indexes on search_vector for full-text search
- **Best For**: Transactional operations, data integrity

### Elasticsearch
- **Fast Queries**: Sub-second search on millions of products
- **Scalable**: Distributed architecture
- **Best For**: Search, filtering, aggregations

### Recommended Approach
- Use PostgreSQL for CRUD operations
- Sync to Elasticsearch for search features
- Cache frequently accessed data in Redis

## Testing with Swagger UI

1. Start the application
2. Navigate to http://localhost:8080/swagger-ui.html
3. Find the "Search" section
4. Try the search endpoints

## Troubleshooting

### Elasticsearch Not Connecting
```
Error: Connection refused to Elasticsearch
```
**Solution**: Ensure Elasticsearch is running and accessible:
```bash
curl http://localhost:9200
docker ps | grep elasticsearch
```

### No Search Results
**Check**:
1. Products are indexed: `productIndexService.indexAllProducts()`
2. Elasticsearch index exists
3. Products have `inStock = true`

### Build Errors
```
Error: Could not find Elasticsearch client
```
**Solution**: Elasticsearch is disabled by default. Enable it in application.yml or ignore the search endpoints.

## Future Enhancements

- [ ] Automatic indexing on product changes (Event listeners)
- [ ] Advanced aggregations (price histograms, attribute distributions)
- [ ] Search analytics and tracking
- [ ] Spell correction ("Did you mean...?")
- [ ] Voice search support
- [ ] Image-based search
- [ ] ML-based personalization
- [ ] A/B testing for search relevance

## Architecture Documentation

See [design/search-architecture.md](../design/search-architecture.md) for detailed architecture documentation.

## Support

For issues or questions about the search feature:
1. Check the architecture documentation
2. Review API examples in Swagger UI
3. Check Elasticsearch logs: `docker logs gearvn-elasticsearch`
4. Check application logs for indexing errors
