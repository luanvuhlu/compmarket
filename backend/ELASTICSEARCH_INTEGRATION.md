# Elasticsearch Integration Implementation

## Overview

This document describes the complete Elasticsearch integration implemented using **Option 1: Proper Integration** with automatic synchronization.

## Architecture

```
Product CRUD Operations
        │
        ▼
   ProductService
        │
        ├─ Save/Update/Delete Product in PostgreSQL
        │
        ▼
  Publish Events
  (ProductCreatedEvent, ProductUpdatedEvent, ProductDeletedEvent)
        │
        ▼
  ProductEventListener (@Async)
        │
        ▼
  ProductIndexService
        │
        ▼
   Elasticsearch Index
```

## Components Implemented

### 1. Event System

**Event Classes** (`ProductEvents.kt`):
- `ProductCreatedEvent` - Published when a product is created
- `ProductUpdatedEvent` - Published when a product is updated
- `ProductDeletedEvent` - Published when a product is deleted (soft delete)

**Event Listener** (`ProductEventListener.kt`):
- Listens for product events asynchronously using `@Async`
- Automatically syncs changes to Elasticsearch
- Graceful error handling with logging
- Conditional activation (only when Elasticsearch is enabled)

### 2. Automatic Synchronization

**ProductService Integration**:
- `createProduct()` - Publishes `ProductCreatedEvent` after saving
- `updateProduct()` - Publishes `ProductUpdatedEvent` after saving
- `deleteProduct()` - Publishes `ProductDeletedEvent` after soft delete

**Benefits**:
- ✅ Automatic sync on all product changes
- ✅ Asynchronous processing (doesn't block main thread)
- ✅ Event-driven architecture (decoupled)
- ✅ Reliable with transactional boundaries

### 3. Admin Endpoints for Reindexing

**New Admin Endpoints** (added to `AdminProductController`):

```http
POST /api/admin/products/reindex
```
Triggers bulk reindexing of ALL products to Elasticsearch.

**Response:**
```json
{
  "success": true,
  "message": "Bulk reindexing started successfully",
  "data": {
    "status": "reindexing"
  }
}
```

```http
POST /api/admin/products/{id}/reindex
```
Triggers reindexing of a SINGLE product to Elasticsearch.

**Response:**
```json
{
  "success": true,
  "message": "Product reindexed successfully",
  "data": {
    "productId": 123,
    "status": "indexed"
  }
}
```

**Access Control**:
- Both endpoints require `ADMIN` or `SUPER_ADMIN` role
- Protected with JWT bearer authentication
- Return appropriate message if Elasticsearch is disabled

### 4. Async Configuration

**AsyncConfig** (`AsyncConfig.kt`):
- Enables `@Async` annotation support
- Allows event listeners to process asynchronously
- Prevents blocking of main application threads

## How It Works

### Automatic Sync Flow

1. **Admin creates/updates product** via `POST /api/admin/products` or `PUT /api/admin/products/{id}`
2. **ProductService** saves to PostgreSQL within transaction
3. **ProductService** publishes event (e.g., `ProductCreatedEvent`)
4. **ProductEventListener** receives event asynchronously
5. **ProductIndexService** syncs to Elasticsearch
6. **ProductDocument** is indexed/updated in Elasticsearch

### Manual Reindex Flow

1. **Admin calls** `POST /api/admin/products/reindex`
2. **AdminProductController** checks if Elasticsearch is enabled
3. **ProductIndexService.indexAllProducts()** fetches all products from PostgreSQL
4. **Products are batch-indexed** to Elasticsearch
5. **Response** confirms reindexing started

## Configuration

### Enable Elasticsearch

In `application.yml`:
```yaml
app:
  elasticsearch:
    enabled: true  # Set to false to disable
    uris: http://localhost:9200
```

### Enable Async Processing

Already enabled via `@EnableAsync` in `AsyncConfig.kt`.

## Testing

### 1. Test Automatic Sync

**Create a product:**
```bash
curl -X POST http://localhost:8080/api/admin/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "name": "Test Laptop",
    "sku": "TEST-001",
    "price": 999.99,
    "stockQuantity": 10,
    "brand": "Test Brand"
  }'
```

**Verify in logs:**
```
Product created event received for product ID: 123
Indexed product: Test Laptop (ID: 123)
```

**Search for it:**
```bash
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{"query": "Test Laptop"}'
```

### 2. Test Manual Reindex

**Reindex all products:**
```bash
curl -X POST http://localhost:8080/api/admin/products/reindex \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Reindex single product:**
```bash
curl -X POST http://localhost:8080/api/admin/products/123/reindex \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Comparison: Option 1 vs Option 3

| Feature | Option 1 (Implemented) | Option 3 (CDC) |
|---------|------------------------|----------------|
| **Complexity** | Medium | High |
| **Setup** | Spring events + listeners | Debezium + Kafka/Connect |
| **Dependencies** | None extra | Kafka, Debezium, Connect |
| **Latency** | Near real-time (async) | Very low latency |
| **Reliability** | Good (event-driven) | Excellent (CDC log-based) |
| **Maintenance** | Easy | Complex |
| **Monitoring** | Application logs | Multiple systems |
| **Transaction Safety** | Yes (after commit) | Yes (log-based) |
| **Best For** | Most use cases | Very high scale, critical sync |

**Why Option 1 is recommended:**
- ✅ Simple to implement and maintain
- ✅ No additional infrastructure required
- ✅ Sufficient for most e-commerce applications
- ✅ Easy to debug and monitor
- ✅ Works within existing Spring ecosystem
- ✅ Good performance with async processing

**When to consider Option 3 (CDC):**
- Very high scale (millions of products, frequent updates)
- Need guaranteed eventual consistency
- Complex data transformations required
- Multi-system synchronization needed
- Already using Kafka infrastructure

## Benefits of This Implementation

1. **Automatic Synchronization**
   - No manual intervention needed
   - Changes automatically reflected in search

2. **Asynchronous Processing**
   - Main API requests not blocked
   - Better performance and user experience

3. **Decoupled Architecture**
   - Event-driven design
   - Easy to add more listeners if needed

4. **Graceful Degradation**
   - Works with or without Elasticsearch
   - Conditional bean loading prevents errors

5. **Admin Control**
   - Manual reindex endpoints for recovery
   - Bulk reindex for initial setup or maintenance

6. **Production Ready**
   - Error handling and logging
   - Transaction-safe
   - Role-based access control

## Monitoring

**Application Logs:**
```
[INFO] Product created event received for product ID: 123
[INFO] Indexed product: Test Laptop (ID: 123)
[INFO] Product updated event received for product ID: 123
[INFO] Indexed product: Test Laptop (ID: 123)
[INFO] Product deleted event received for product ID: 123
[INFO] Deleted product from index: 123
```

**Error Logs:**
```
[ERROR] Failed to index product on creation: 123
[ERROR] Failed to reindex product on update: 123
[ERROR] Failed to delete product from index: 123
```

## Troubleshooting

### Products not appearing in search

1. **Check Elasticsearch is enabled:**
   ```yaml
   app.elasticsearch.enabled: true
   ```

2. **Check Elasticsearch is running:**
   ```bash
   curl http://localhost:9200
   ```

3. **Check logs for indexing errors**

4. **Manually trigger reindex:**
   ```bash
   curl -X POST http://localhost:8080/api/admin/products/reindex \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

### Event listener not firing

1. **Verify @EnableAsync is present** in `AsyncConfig.kt`
2. **Check logs** for event publishing
3. **Verify Elasticsearch is enabled** (listener is conditional)

## Future Enhancements

- [ ] Add retry mechanism for failed indexing
- [ ] Implement dead letter queue for failed events
- [ ] Add metrics for indexing performance
- [ ] Implement partial updates (instead of full reindex)
- [ ] Add batch event processing for better performance
- [ ] Implement index versioning and zero-downtime reindex

## Summary

This implementation provides a **complete, production-ready Elasticsearch integration** with:
- ✅ Automatic synchronization on product CRUD operations
- ✅ Asynchronous event processing
- ✅ Admin endpoints for manual reindexing
- ✅ Graceful degradation when Elasticsearch is disabled
- ✅ Transaction-safe and reliable
- ✅ Easy to maintain and monitor

The system follows Spring best practices and provides a solid foundation for search functionality in the e-commerce application.
