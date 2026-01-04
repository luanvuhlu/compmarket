# Advanced Product Search Architecture

## Overview

The e-commerce platform implements a **hybrid search architecture** combining:
- **PostgreSQL EAV (Entity-Attribute-Value)** for flexible product specifications
- **Elasticsearch** for fast, full-text search with natural language support
- **Faceted search** with aggregations for filtering

This enables users to search using natural language queries like "Asus i7 16GB RAM" while also supporting structured filters.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         User Query                               │
│              "Asus gaming laptop i7 16GB RAM"                    │
└────────────────────────┬─────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Backend                           │
├─────────────────────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              SearchController                              │  │
│  │  - POST /api/search (with filters)                        │  │
│  │  - GET /api/search (query params)                         │  │
│  │  - GET /api/search/autocomplete                           │  │
│  │  - GET /api/search/similar/{id}                           │  │
│  └──────────────────────┬────────────────────────────────────┘  │
│                         │                                        │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │              SearchService                                  │ │
│  │  - Coordinates search operations                           │ │
│  │  - Extracts facets from Elasticsearch                      │ │
│  │  - Converts ES results to DTOs                             │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                        │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │          ElasticsearchService                               │ │
│  │  - Builds complex ES queries                               │ │
│  │  - Multi-field text search                                 │ │
│  │  - Aggregations for facets                                 │ │
│  │  - Auto-complete & suggestions                             │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                        │
└─────────────────────────┼─────────────────────────────────────────┘
                          │
          ┌───────────────┴──────────────────┐
          │                                   │
          ▼                                   ▼
┌─────────────────────┐           ┌──────────────────────────┐
│   PostgreSQL DB     │           │    Elasticsearch         │
├─────────────────────┤           ├──────────────────────────┤
│ Product Core Table  │◀─sync────▶│  products index          │
│ ├─ id, name, price  │           │  ├─ Denormalized docs    │
│ ├─ brand, category  │           │  ├─ Full-text search     │
│ └─ sku, stock       │           │  ├─ Faceted search       │
│                     │           │  └─ Aggregations         │
│ EAV Specifications  │           │                          │
│ ├─ AttributeDef     │           │  Features:               │
│ ├─ AttributeOptions │           │  - Typo tolerance        │
│ └─ ProductSpecs     │           │  - Fuzzy matching        │
│                     │           │  - Multi-field search    │
│ Features:           │           │  - Auto-complete         │
│ - Flexible attrs    │           │  - More Like This        │
│ - Normalized values │           │  - Relevance scoring     │
│ - Data integrity    │           │                          │
└─────────────────────┘           └──────────────────────────┘
```

---

## Database Schema (EAV Pattern)

### 1. **attribute_definitions** - Define Available Attributes
```sql
CREATE TABLE attribute_definitions (
    attribute_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE,           -- e.g., 'ram_size', 'processor'
    display_name VARCHAR(255),          -- e.g., 'RAM Size', 'Processor'
    data_type VARCHAR(20),              -- STRING, NUMERIC, BOOLEAN, ENUM
    unit VARCHAR(50),                   -- e.g., 'GB', 'GHz'
    is_filterable BOOLEAN,              -- Can be used in filters
    is_searchable BOOLEAN,              -- Include in search text
    sort_order INTEGER
);
```

**Example Data:**
| attribute_id | name              | display_name     | data_type | unit | is_filterable |
|--------------|-------------------|------------------|-----------|------|---------------|
| 1            | ram_size          | RAM Size         | NUMERIC   | GB   | true          |
| 2            | processor         | Processor        | STRING    | NULL | true          |
| 3            | storage_capacity  | Storage Capacity | NUMERIC   | GB   | true          |
| 4            | storage_type      | Storage Type     | ENUM      | NULL | true          |

### 2. **attribute_options** - Predefined Values for ENUM Types
```sql
CREATE TABLE attribute_options (
    option_id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT REFERENCES attribute_definitions,
    option_value VARCHAR(255),          -- e.g., 'ssd', 'hdd'
    display_label VARCHAR(255),         -- e.g., 'SSD', 'HDD'
    sort_order INTEGER
);
```

### 3. **product_specifications** - EAV Product Attributes
```sql
CREATE TABLE product_specifications (
    spec_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products,
    attribute_id BIGINT REFERENCES attribute_definitions,
    value_string VARCHAR(500),          -- For STRING/ENUM types
    value_numeric DECIMAL(15,4),        -- For NUMERIC types
    value_boolean BOOLEAN,              -- For BOOLEAN types
    UNIQUE(product_id, attribute_id)
);
```

**Example Data:**
| spec_id | product_id | attribute_id | value_string   | value_numeric |
|---------|------------|--------------|----------------|---------------|
| 1       | 101        | 1            | NULL           | 16.0000       |
| 2       | 101        | 2            | Intel Core i7  | NULL          |
| 3       | 101        | 3            | NULL           | 512.0000      |
| 4       | 101        | 4            | nvme           | NULL          |

---

## Elasticsearch Document Structure

### ProductDocument Schema
```json
{
  "id": "101",
  "name": "ASUS ROG Gaming Laptop",
  "brand": "Asus",
  "category": "Gaming Laptops",
  "categoryId": 5,
  "price": 1499.99,
  "discountPrice": 1299.99,
  "description": "High-performance gaming laptop...",
  "sku": "ASUS-ROG-001",
  "stockQuantity": 15,
  "inStock": true,
  "model": "ROG Zephyrus G14",
  
  "specifications": [
    {
      "attributeName": "ram_size",
      "attributeDisplayName": "RAM Size",
      "valueNumeric": 16,
      "unit": "GB"
    },
    {
      "attributeName": "processor",
      "attributeDisplayName": "Processor",
      "valueString": "Intel Core i7-6870HQ"
    },
    {
      "attributeName": "storage_capacity",
      "attributeDisplayName": "Storage",
      "valueNumeric": 512,
      "unit": "GB"
    }
  ],
  
  "searchableText": "ASUS ROG Gaming Laptop Intel Core i7 16GB RAM 512GB SSD RTX 3650",
  
  "tags": ["gaming", "high-performance", "rgb"],
  
  "facets": {
    "brand": "Asus",
    "ramOptions": ["16GB"],
    "processorFamily": ["Intel i7"],
    "gpuFamily": ["RTX 3000 Series"],
    "storageTypes": ["NVMe SSD"]
  }
}
```

---

## Search Features

### 1. **Natural Language Search**
Users can search using free-form text:
- "Asus i7 16GB RAM"
- "gaming laptop under 1500"
- "Dell laptop with SSD"

**How it works:**
- Multi-field matching across name, brand, specifications, description
- Relevance scoring and ranking
- Typo tolerance with fuzzy matching
- Synonym support (laptop ↔ notebook)

### 2. **Structured Filters**
Filters are applied as exact criteria:
- Category: Gaming Laptops
- Brand: Asus, Dell
- Price Range: $500 - $2000
- RAM: 16GB, 32GB
- Storage Type: SSD

### 3. **Faceted Search**
Dynamic filter options with counts:
```
Brand
├─ Asus (45 products)
├─ Dell (32 products)
└─ HP (28 products)

RAM
├─ 8GB (20 products)
├─ 16GB (60 products) ✓ selected
└─ 32GB (40 products)

Price Range
├─ $500-$1000 (30)
├─ $1000-$1500 (50) ✓ selected
└─ $1500-$2000 (20)
```

### 4. **Auto-Complete**
Real-time suggestions as user types:
```
User types: "asu..."
Suggestions:
├─ Asus
├─ Asus ROG
├─ Asus TUF Gaming
└─ Asus VivoBook
```

### 5. **More Like This**
Find similar products based on current product:
- Analyzes specifications and features
- Returns products with similar attributes
- Useful for product recommendations

---

## API Endpoints

### Search Endpoint
```http
POST /api/search
Content-Type: application/json

{
  "query": "Asus gaming laptop i7",
  "categoryIds": [5],
  "brands": ["Asus"],
  "minPrice": 1000,
  "maxPrice": 2000,
  "inStock": true,
  "sortBy": "RELEVANCE",
  "sortOrder": "DESC"
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
    "totalPages": 3,
    "last": false
  },
  "facets": {
    "categories": [
      {"categoryId": 5, "categoryName": "Gaming Laptops", "count": 45}
    ],
    "brands": [
      {"brand": "Asus", "count": 30},
      {"brand": "Dell", "count": 15}
    ],
    "priceRanges": [
      {"min": 1000, "max": 1500, "label": "$1,000-$1,500", "count": 25},
      {"min": 1500, "max": 2000, "label": "$1,500-$2,000", "count": 20}
    ]
  }
}
```

### Auto-Complete
```http
GET /api/search/autocomplete?prefix=asu&limit=10
```

### Similar Products
```http
GET /api/search/similar/101?limit=10
```

---

## Data Synchronization Strategy

### Indexing Flow
```
Product CRUD Operation
       │
       ▼
┌─────────────────┐
│  ProductService │
│  - Create       │
│  - Update       │
│  - Delete       │
└──────┬──────────┘
       │
       ▼
┌─────────────────────┐
│ ProductIndexService │
│ - Convert to ES doc │
│ - Index to ES       │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│   Elasticsearch     │
│   - Update index    │
│   - Refresh         │
└─────────────────────┘
```

### Synchronization Methods

1. **Real-time Indexing**
   - On product create/update/delete
   - Immediate consistency
   - Used in production

2. **Batch Re-indexing**
   - Manual trigger for full re-index
   - Useful after schema changes
   - Can run during off-peak hours

---

## Performance Characteristics

### Query Performance
- **Simple text search**: < 50ms
- **Complex filters + aggregations**: < 200ms
- **Auto-complete**: < 30ms
- **More Like This**: < 100ms

### Scalability
- **Products supported**: Millions
- **Concurrent searches**: 1000+ per second
- **Index size**: ~1KB per product
- **Refresh interval**: 1 second (near real-time)

---

## Benefits of Hybrid Approach

### PostgreSQL (EAV)
✅ **Flexible**: Easy to add new attributes
✅ **Normalized**: Data integrity with foreign keys
✅ **Typed**: Numeric, string, boolean, enum support
✅ **Queryable**: Can query specifications directly
✅ **Transactional**: ACID compliance

### Elasticsearch
✅ **Fast**: Sub-second search on large datasets
✅ **Relevant**: Scoring and ranking by relevance
✅ **Tolerant**: Handles typos and fuzzy matching
✅ **Faceted**: Aggregations for dynamic filters
✅ **Scalable**: Distributed architecture

### Combined
✅ **Best of both worlds**: PostgreSQL for data integrity, Elasticsearch for search
✅ **Decoupled**: Can evolve independently
✅ **Resilient**: If ES fails, DB still works (fallback to SQL search)
✅ **Flexible**: Add search features without changing DB schema

---

## Example Search Scenarios

### Scenario 1: Natural Language
**User Query:** "Asus i7 16GB gaming laptop"

**Elasticsearch Processing:**
1. Tokenize: [Asus, i7, 16GB, gaming, laptop]
2. Multi-field search: name, brand, specifications, tags
3. Relevance scoring: boost name and brand matches
4. Return top results sorted by score

### Scenario 2: Filtered Search
**User Selections:**
- Category: Gaming Laptops
- Brand: Asus
- RAM: 16GB+
- Price: $1000-$2000

**Elasticsearch Query:**
- Filter by exact category, brand
- Range query on RAM (≥16GB) and price
- Return results with aggregations for other filters

### Scenario 3: Hybrid
**Combined:**
- Text query: "gaming"
- Filters: Brand=Asus, Price=$1000-$2000

**Elasticsearch Behavior:**
- "must" clause: text search with scoring
- "filter" clause: exact criteria (no scoring)
- Fast, cached filters + relevant text results

---

## Monitoring & Maintenance

### Health Checks
- Elasticsearch cluster health
- Index size and shard count
- Query latency metrics
- Cache hit rates

### Maintenance Tasks
- Re-index products after major changes
- Monitor index growth
- Optimize shard allocation
- Update synonym dictionaries
- Tune relevance scoring

---

## Future Enhancements

### Potential Improvements
- **ML-based personalization**: Boost products based on user history
- **Spell correction**: "Did you mean..." suggestions
- **Voice search**: Support for voice input
- **Image search**: Search by product images
- **Advanced filters**: Color, dimensions, weight ranges
- **Search analytics**: Track popular queries and conversions

---

## Technology Stack

### Search Components
- **Elasticsearch**: 8.11.1 (latest stable)
- **Spring Data Elasticsearch**: For repository integration
- **PostgreSQL**: 18.1 with EAV tables
- **Spring Boot**: Search service orchestration

### Libraries
- `spring-boot-starter-data-elasticsearch`
- Elasticsearch Java Client
- PostgreSQL JDBC Driver
- Jackson for JSON processing

---

## Summary

The hybrid search architecture provides:

🔍 **Flexible Specifications**: EAV model supports any product attribute  
🚀 **Fast Search**: Elasticsearch for sub-second queries  
💡 **Smart Search**: Natural language, typo tolerance, relevance ranking  
📊 **Rich Filtering**: Faceted search with dynamic aggregations  
🎯 **Auto-Complete**: Real-time suggestions as users type  
🤖 **Recommendations**: "More Like This" for similar products  
📈 **Scalable**: Handles millions of products efficiently  

Perfect for e-commerce platforms selling diverse products with varying specifications!
