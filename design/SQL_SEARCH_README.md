# SQL-Based Search with Attribute Filtering

This implementation provides a comprehensive SQL-based search system that replaces Elasticsearch and supports advanced attribute/specification filtering like RAM: 16GB, CPU: Intel Core i7, etc.

## Overview

The search system now uses SQL queries against the EAV (Entity-Attribute-Value) pattern to find products by their specifications. This allows for precise filtering on product attributes like:

- **RAM**: 8GB, 16GB, 32GB
- **CPU/Processor**: Intel Core i7, AMD Ryzen 7, etc.
- **Storage**: 512GB SSD, 1TB HDD, etc.
- **GPU**: RTX 3070, RTX 4080, etc.
- **Screen Size**: 13.3", 15.6", 17.3"
- **Operating System**: Windows 11, macOS, Linux
- And many more...

## API Endpoints

### 1. Advanced Search (POST)

```http
POST /api/search
Content-Type: application/json

{
  "query": "gaming laptop",
  "categoryIds": [5],
  "brands": ["Asus", "MSI"],
  "minPrice": 1000,
  "maxPrice": 2000,
  "inStock": true,
  "specifications": {
    "ram_size": "16",
    "processor": "Intel Core i7",
    "gpu": "RTX",
    "storage_capacity": "512"
  },
  "sortBy": "PRICE",
  "sortOrder": "ASC"
}
```

### 2. Simple Search with URL Parameters (GET)

```http
GET /api/search?query=gaming laptop&ram=16&cpu=Intel Core i7&gpu=RTX 3070&categoryIds=5&page=0&size=20
```

### 3. Search by Specific Attribute

```http
GET /api/search/by-spec?attr=ram_size&value=16&page=0&size=20
```

### 4. Auto-complete

```http
GET /api/search/autocomplete?prefix=Intel
```

## Supported URL Parameters

| Parameter | Description | Example |
|-----------|-------------|---------|
| `query` | General search text | `gaming laptop` |
| `ram` | RAM size | `16` (for 16GB) |
| `cpu` or `processor` | CPU/Processor | `Intel Core i7` |
| `gpu` | Graphics card | `RTX 3070` |
| `storage` | Storage capacity | `512` (for 512GB) |
| `screen_size` | Screen size | `15.6` |
| `os` | Operating system | `Windows 11` |
| `weight` | Weight | `2.5` |
| `brand_cpu` | CPU brand | `Intel` |
| `specs` | Generic specifications | `{"attribute": "value"}` |

## Response Format

```json
{
  "products": {
    "content": [
      {
        "id": 1,
        "name": "ASUS ROG Gaming Laptop",
        "price": 1499.99,
        "brand": "Asus",
        "categoryName": "Gaming Laptops",
        "specifications": "...",
        "..."
      }
    ],
    "totalElements": 45,
    "totalPages": 3,
    "number": 0,
    "size": 20,
    "first": true,
    "last": false
  },
  "facets": {
    "categories": [
      {"categoryId": 5, "categoryName": "Gaming Laptops", "count": 45}
    ],
    "brands": [
      {"brand": "Asus", "count": 30},
      {"brand": "MSI", "count": 15}
    ],
    "priceRanges": [
      {"min": 1000, "max": 2000, "label": "$1,000 - $2,000", "count": 25}
    ],
    "specifications": [
      {
        "attributeName": "ram_size",
        "attributeDisplayName": "RAM Size",
        "values": [
          {"value": "16", "count": 25},
          {"value": "8", "count": 15},
          {"value": "32", "count": 5}
        ]
      },
      {
        "attributeName": "processor",
        "attributeDisplayName": "Processor",
        "values": [
          {"value": "Intel Core i7", "count": 20},
          {"value": "AMD Ryzen 7", "count": 12},
          {"value": "Intel Core i5", "count": 8}
        ]
      }
    ]
  }
}
```

## Database Schema

The system uses an EAV (Entity-Attribute-Value) pattern:

### `attribute_definitions`
```sql
CREATE TABLE attribute_definitions (
    attribute_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,           -- e.g., 'ram_size', 'processor'
    display_name VARCHAR(255) NOT NULL,          -- e.g., 'RAM Size', 'Processor'
    data_type VARCHAR(20) NOT NULL,              -- STRING, NUMERIC, BOOLEAN, ENUM
    unit VARCHAR(50),                            -- e.g., 'GB', 'GHz', 'inches'
    is_filterable BOOLEAN DEFAULT true,
    is_searchable BOOLEAN DEFAULT false,
    sort_order INTEGER DEFAULT 0
);
```

### `product_specifications`
```sql
CREATE TABLE product_specifications (
    spec_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(product_id),
    attribute_id BIGINT REFERENCES attribute_definitions(attribute_id),
    value_string VARCHAR(500),                   -- For text values
    value_numeric DECIMAL(15,4),                 -- For numeric values  
    value_boolean BOOLEAN,                       -- For boolean values
    UNIQUE(product_id, attribute_id)
);
```

## Example Usage Scenarios

### 1. Find Gaming Laptops with 16GB RAM and Intel CPU
```http
GET /api/search?query=gaming laptop&ram=16&cpu=Intel&categoryIds=5
```

### 2. Find Laptops under $1500 with SSD Storage
```http
GET /api/search?maxPrice=1500&specs={"storage_type":"SSD"}
```

### 3. Find All Products with RTX Graphics Cards
```http
GET /api/search?gpu=RTX
```

### 4. Advanced Search with Multiple Specifications
```http
POST /api/search
{
  "specifications": {
    "ram_size": "16",
    "processor": "Intel Core i7",
    "storage_capacity": "512",
    "gpu": "RTX 3070",
    "screen_size": "15.6"
  },
  "minPrice": 1200,
  "maxPrice": 1800,
  "inStock": true
}
```

## Technical Implementation

### SQL Query Generation
The system generates complex SQL queries with EXISTS subqueries for each specification filter:

```sql
SELECT DISTINCT p FROM Product p
LEFT JOIN ProductSpecification ps ON ps.product.id = p.id
LEFT JOIN AttributeDefinition ad ON ad.id = ps.attribute.id
WHERE p.isActive = true
AND EXISTS (
    SELECT 1 FROM ProductSpecification ps2
    JOIN AttributeDefinition ad2 ON ad2.id = ps2.attribute.id
    WHERE ps2.product.id = p.id
    AND LOWER(ad2.name) = LOWER('ram_size')
    AND (
        LOWER(ps2.valueString) LIKE LOWER('%16%')
        OR CAST(ps2.valueNumeric AS STRING) LIKE '%16%'
    )
)
AND EXISTS (
    SELECT 1 FROM ProductSpecification ps2
    JOIN AttributeDefinition ad2 ON ad2.id = ps2.attribute.id  
    WHERE ps2.product.id = p.id
    AND LOWER(ad2.name) = LOWER('processor')
    AND LOWER(ps2.valueString) LIKE LOWER('%Intel Core i7%')
)
ORDER BY p.price ASC
```

### Faceted Search
The system provides faceted search results showing available filter values:
- **Categories**: Available product categories with counts
- **Brands**: Available brands with counts
- **Price Ranges**: Predefined price ranges with counts
- **Specifications**: Available attribute values with counts

## Performance Considerations

1. **Indexes**: Ensure proper indexes on:
   - `products.is_active`
   - `product_specifications.product_id`
   - `product_specifications.attribute_id`
   - `attribute_definitions.name`
   - `products.category_id`
   - `products.price`

2. **Query Optimization**: 
   - Uses EXISTS subqueries for efficient filtering
   - Separate count and data queries to avoid GROUP BY issues
   - DISTINCT to handle multiple specifications per product

3. **Caching**: Results are cached using Redis for improved performance

## Migration from Elasticsearch

This implementation replaces the Elasticsearch-based search with pure SQL, providing:

✅ **Advantages**:
- No dependency on Elasticsearch infrastructure
- More precise attribute filtering
- Better integration with existing SQL database
- Easier to maintain and debug
- Real-time consistency with database

❌ **Trade-offs**:
- Less advanced text search capabilities
- No built-in relevance scoring
- Potentially slower for very large datasets
- Limited fuzzy matching and typo tolerance

## Adding New Searchable Attributes

1. **Add Attribute Definition**:
```sql
INSERT INTO attribute_definitions (name, display_name, data_type, unit, is_filterable)
VALUES ('screen_resolution', 'Screen Resolution', 'STRING', 'pixels', true);
```

2. **Add Product Specifications**:
```sql
INSERT INTO product_specifications (product_id, attribute_id, value_string)
SELECT p.product_id, ad.attribute_id, '1920x1080'
FROM products p, attribute_definitions ad
WHERE p.product_id = 1 AND ad.name = 'screen_resolution';
```

3. **Update Controller** (optional):
Add new URL parameter in `SearchController.searchWithParams()`:
```kotlin
@RequestParam(required = false) screen_resolution: String?
// ...
screen_resolution?.let { specifications["screen_resolution"] = it }
```

The system will automatically include the new attribute in faceted search results and allow filtering on it.
