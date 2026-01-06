# PostgreSQL Full-Text Search API

This document describes the implementation and usage of PostgreSQL's full-text search capabilities for product search.

## Overview

The full-text search API uses PostgreSQL's built-in text search features, including:
- **tsvector**: Preprocessed, tokenized, and indexed text
- **tsquery**: Structured text search queries
- **ts_rank()**: Relevance ranking function
- **GIN index**: Fast full-text search index

## API Endpoints

### 1. Full-Text Search Endpoint

```http
GET /api/products/search/fulltext?query=gaming laptop intel&page=0&size=20
```

**Parameters:**
- `query` (required): Search query text
- `page` (optional): Page number, default 0
- `size` (optional): Page size, default 20

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "ASUS ROG Gaming Laptop",
      "description": "High-performance gaming laptop with Intel Core i7",
      "brand": "Asus",
      "price": 1499.99,
      "categoryName": "Gaming Laptops",
      "..."
    }
  ],
  "totalElements": 25,
  "totalPages": 2,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

### 2. Comparison with Basic Search

| Endpoint | Search Method | Use Case |
|----------|---------------|----------|
| `/api/products/search` | LIKE queries | Simple keyword matching |
| `/api/products/search/fulltext` | PostgreSQL FTS | Advanced text search with ranking |

## Search Query Examples

### Basic Queries
```http
# Single word
GET /api/products/search/fulltext?query=gaming

# Multiple words (AND operation)
GET /api/products/search/fulltext?query=gaming laptop

# Brand specific search
GET /api/products/search/fulltext?query=asus intel
```

### Advanced Queries
```http
# Phrase search (will be processed as individual terms)
GET /api/products/search/fulltext?query=core i7 processor

# Product type with specifications
GET /api/products/search/fulltext?query=laptop ssd 16gb

# Brand and feature combination
GET /api/products/search/fulltext?query=dell xps ultrabook
```

## Query Processing

The system automatically processes queries to optimize PostgreSQL full-text search:

### Input Processing
```kotlin
// Input: "gaming laptop intel"
// Output: "gaming:* & laptop:* & intel:*"

// Input: "ASUS ROG Gaming"
// Output: "asus:* & rog:* & gaming:*"
```

### Benefits of Processing
1. **Wildcard matching**: `gaming:*` matches "gaming", "gamer", "games"
2. **Case insensitive**: Automatically handled
3. **AND operation**: All terms must be present
4. **Stemming**: PostgreSQL handles word variations

## Database Schema

### Search Vector Column
```sql
-- Add search_vector column to products table
ALTER TABLE products ADD COLUMN search_vector tsvector;

-- Create GIN index for fast search
CREATE INDEX idx_products_search_vector ON products USING GIN(search_vector);
```

### Trigger for Automatic Updates
```sql
CREATE OR REPLACE FUNCTION update_search_vector() RETURNS trigger AS $$
BEGIN
  NEW.search_vector := to_tsvector('english', 
    COALESCE(NEW.name, '') || ' ' || 
    COALESCE(NEW.description, '') || ' ' || 
    COALESCE(NEW.brand, '')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER products_search_vector_update 
  BEFORE INSERT OR UPDATE ON products 
  FOR EACH ROW EXECUTE FUNCTION update_search_vector();
```

### Update Existing Records
```sql
UPDATE products SET search_vector = to_tsvector('english', 
  COALESCE(name, '') || ' ' || 
  COALESCE(description, '') || ' ' || 
  COALESCE(brand, '')
);
```

## Search Fields

The search_vector currently includes:
- **Product Name**: Primary search field
- **Description**: Detailed product information  
- **Brand**: Manufacturer name

To add more fields (e.g., model, specifications), modify the trigger:
```sql
CREATE OR REPLACE FUNCTION update_search_vector() RETURNS trigger AS $$
BEGIN
  NEW.search_vector := to_tsvector('english', 
    COALESCE(NEW.name, '') || ' ' || 
    COALESCE(NEW.description, '') || ' ' || 
    COALESCE(NEW.brand, '') || ' ' ||
    COALESCE(NEW.model, '') || ' ' ||
    COALESCE(NEW.specifications, '')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

## Performance Characteristics

### Full-Text Search Advantages
✅ **Fast**: GIN index provides O(log n) search performance  
✅ **Relevant**: Results ranked by relevance score  
✅ **Stemming**: Finds word variations automatically  
✅ **Language aware**: English language processing  
✅ **Scalable**: Handles large datasets efficiently  

### Basic Search Limitations
❌ **Slower**: LIKE queries scan more data  
❌ **No ranking**: Results not ordered by relevance  
❌ **Exact match**: No stemming or language processing  
❌ **Case sensitive**: Requires LOWER() functions  

## Usage Examples

### Frontend Integration
```javascript
// Full-text search
const searchProducts = async (query) => {
  const response = await fetch(
    `/api/products/search/fulltext?query=${encodeURIComponent(query)}&page=0&size=20`
  );
  return response.json();
};

// Usage
searchProducts("gaming laptop intel").then(results => {
  console.log(`Found ${results.totalElements} products`);
  results.content.forEach(product => {
    console.log(`${product.name} - ${product.brand}`);
  });
});
```

### cURL Examples
```bash
# Search for gaming laptops
curl "http://localhost:8080/api/products/search/fulltext?query=gaming%20laptop"

# Search for specific brand and feature
curl "http://localhost:8080/api/products/search/fulltext?query=asus%20intel%20ssd"

# Paginated search
curl "http://localhost:8080/api/products/search/fulltext?query=laptop&page=1&size=10"
```

## Caching

Search results are cached using Spring Cache:
```kotlin
@Cacheable(value = ["product-search"], key = "#query + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
```

**Cache Key Format**: `"gaming laptop_0_20"`  
**Cache Duration**: Configured in Redis settings  
**Cache Invalidation**: Automatic when products are modified  

## Error Handling

### Invalid Query Handling
```kotlin
// Empty or whitespace query
prepareFullTextQuery("   ") // Returns: "''::tsquery"

// Special characters are cleaned
prepareFullTextQuery("gaming!@#$%laptop") // Returns: "gaming:* & laptop:*"
```

### Exception Scenarios
- **Empty Results**: Returns empty page with `totalElements: 0`
- **Database Error**: Returns 500 Internal Server Error
- **Invalid Parameters**: Returns 400 Bad Request

## Monitoring and Analytics

### Query Performance
```sql
-- Check search vector size
SELECT pg_size_pretty(pg_total_relation_size('idx_products_search_vector')) as index_size;

-- Analyze search performance
EXPLAIN ANALYZE SELECT * FROM products 
WHERE search_vector @@ to_tsquery('english', 'gaming:* & laptop:*');
```

### Search Analytics
Track popular search terms:
```sql
-- Add search analytics table
CREATE TABLE search_analytics (
  id BIGSERIAL PRIMARY KEY,
  query TEXT NOT NULL,
  results_count INTEGER,
  search_time TIMESTAMP DEFAULT NOW()
);
```

## Future Enhancements

1. **Query Suggestions**: Auto-complete using trigram similarity
2. **Search Highlighting**: Highlight matched terms in results  
3. **Faceted Search**: Combine with category/brand filters
4. **Search Analytics**: Track popular queries and click-through rates
5. **Spell Correction**: Handle typos using fuzzy matching
6. **Synonym Support**: Expand queries with synonyms

## Troubleshooting

### Common Issues

**No Results Found:**
```sql
-- Check if search_vector is populated
SELECT name, search_vector FROM products WHERE search_vector IS NOT NULL LIMIT 5;

-- Verify index exists
SELECT indexname FROM pg_indexes WHERE tablename = 'products';
```

**Poor Performance:**
```sql
-- Analyze index usage
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM products 
WHERE search_vector @@ to_tsquery('english', 'gaming:*');
```

**Trigger Not Working:**
```sql
-- Test trigger manually
UPDATE products SET name = name WHERE id = 1;

-- Check if search_vector was updated
SELECT search_vector FROM products WHERE id = 1;
```

This full-text search API provides a robust, scalable solution for product search with PostgreSQL's advanced text search capabilities.
