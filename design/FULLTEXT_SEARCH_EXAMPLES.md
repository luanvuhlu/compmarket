# Full-Text Search API Examples

## Quick Test Examples

### 1. Basic Full-Text Search
```bash
# Search for gaming laptops
curl -X GET "http://localhost:8080/api/products/search/fulltext?query=gaming%20laptop"

# Search for specific brand
curl -X GET "http://localhost:8080/api/products/search/fulltext?query=asus"

# Search with multiple terms
curl -X GET "http://localhost:8080/api/products/search/fulltext?query=intel%20core%20i7"
```

### 2. Comparison with Basic Search

```bash
# Basic search (LIKE queries)
curl -X GET "http://localhost:8080/api/products/search?keyword=gaming%20laptop"

# Full-text search (PostgreSQL FTS)
curl -X GET "http://localhost:8080/api/products/search/fulltext?query=gaming%20laptop"
```

### 3. Paginated Search
```bash
# Get first page (20 items)
curl -X GET "http://localhost:8080/api/products/search/fulltext?query=laptop&page=0&size=20"

# Get second page
curl -X GET "http://localhost:8080/api/products/search/fulltext?query=laptop&page=1&size=20"

# Custom page size
curl -X GET "http://localhost:8080/api/products/search/fulltext?query=laptop&page=0&size=10"
```

### 4. JavaScript/Frontend Usage
```javascript
// Async function to search products
async function searchProducts(query, page = 0, size = 20) {
    try {
        const response = await fetch(
            `/api/products/search/fulltext?query=${encodeURIComponent(query)}&page=${page}&size=${size}`
        );
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Search failed:', error);
        return null;
    }
}

// Usage examples
searchProducts("gaming laptop")
    .then(results => {
        if (results) {
            console.log(`Found ${results.totalElements} products`);
            results.content.forEach(product => {
                console.log(`${product.name} - $${product.price}`);
            });
        }
    });

// Search with pagination
searchProducts("intel core i7", 1, 10)
    .then(results => {
        console.log(`Page 2 of search results: ${results.content.length} items`);
    });
```

### 5. React Component Example
```jsx
import React, { useState, useEffect } from 'react';

function ProductSearch() {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleSearch = async (searchQuery) => {
        if (!searchQuery.trim()) return;
        
        setLoading(true);
        try {
            const response = await fetch(
                `/api/products/search/fulltext?query=${encodeURIComponent(searchQuery)}`
            );
            const data = await response.json();
            setResults(data);
        } catch (error) {
            console.error('Search failed:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const timeoutId = setTimeout(() => {
            if (query) {
                handleSearch(query);
            }
        }, 500); // Debounce search

        return () => clearTimeout(timeoutId);
    }, [query]);

    return (
        <div>
            <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search products..."
                className="search-input"
            />
            
            {loading && <div>Searching...</div>}
            
            {results && (
                <div>
                    <h3>Found {results.totalElements} products</h3>
                    <div className="product-list">
                        {results.content.map(product => (
                            <div key={product.id} className="product-item">
                                <h4>{product.name}</h4>
                                <p>{product.brand} - ${product.price}</p>
                                <p>{product.description}</p>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}

export default ProductSearch;
```

### 6. Expected Response Format
```json
{
  "content": [
    {
      "id": 1,
      "categoryId": 5,
      "categoryName": "Gaming Laptops",
      "name": "ASUS ROG Gaming Laptop",
      "description": "High-performance gaming laptop with Intel Core i7 processor",
      "sku": "ASUS-ROG-001", 
      "price": 1499.99,
      "discountPrice": 1299.99,
      "stockQuantity": 15,
      "brand": "Asus",
      "model": "ROG Zephyrus G14",
      "specifications": "{\"ram\":\"16GB\",\"cpu\":\"Intel Core i7\"}",
      "images": "[{\"url\":\"laptop1.jpg\"}]",
      "isActive": true
    }
  ],
  "totalElements": 25,
  "totalPages": 2,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

### 7. Error Handling Examples
```javascript
async function searchWithErrorHandling(query) {
    try {
        const response = await fetch(
            `/api/products/search/fulltext?query=${encodeURIComponent(query)}`
        );
        
        if (!response.ok) {
            switch (response.status) {
                case 400:
                    throw new Error('Invalid search query');
                case 404:
                    throw new Error('Search endpoint not found');
                case 500:
                    throw new Error('Server error during search');
                default:
                    throw new Error(`Unexpected error: ${response.status}`);
            }
        }
        
        const data = await response.json();
        
        if (data.content.length === 0) {
            console.log('No products found for:', query);
            return { message: 'No results found', data: null };
        }
        
        return { message: 'Success', data };
        
    } catch (error) {
        console.error('Search error:', error.message);
        return { message: error.message, data: null };
    }
}
```

## Testing the Database Setup

### Verify Search Vector Setup
```sql
-- Check if search_vector column exists and is populated
SELECT 
    name, 
    brand, 
    search_vector 
FROM products 
WHERE search_vector IS NOT NULL 
LIMIT 5;

-- Check index exists
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'products' 
AND indexname = 'idx_products_search_vector';
```

### Test Full-Text Query Manually
```sql
-- Test the exact query that the API generates
SELECT 
    name,
    brand,
    ts_rank(search_vector, to_tsquery('english', 'gaming:* & laptop:*')) as rank
FROM products 
WHERE search_vector @@ to_tsquery('english', 'gaming:* & laptop:*')
ORDER BY rank DESC
LIMIT 10;
```

### Performance Testing
```sql
-- Analyze query performance
EXPLAIN ANALYZE 
SELECT * FROM products 
WHERE is_active = true 
AND search_vector @@ to_tsquery('english', 'gaming:* & laptop:*')
ORDER BY ts_rank(search_vector, to_tsquery('english', 'gaming:* & laptop:*')) DESC;
```

This API provides a powerful, scalable search solution using PostgreSQL's built-in full-text search capabilities!
