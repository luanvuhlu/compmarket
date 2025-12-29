# GearVN E-Commerce Platform

A modern e-commerce platform for computers and accessories built with Spring Boot 3.x (Kotlin) and React.

## Technology Stack

### Backend
- **Language**: Kotlin with Java 21
- **Framework**: Spring Boot 3.x (latest)
- **Build Tool**: Gradle
- **Database**: PostgreSQL 18.1
- **Cache**: Redis 8
- **Migration**: Flyway
- **Security**: Spring Security with JWT
- **API Documentation**: Swagger UI (SpringDoc OpenAPI)

### Architecture
- Monolithic 3-tier architecture
- RESTful API design
- JWT-based authentication
- PostgreSQL full-text search
- Redis caching for performance

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── kotlin/com/gearvn/ecommerce/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── repository/      # Spring Data repositories
│   │   │   ├── security/        # Security & JWT
│   │   │   └── EcommerceApplication.kt
│   │   └── resources/
│   │       ├── db/migration/    # Flyway migrations
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
├── build.gradle.kts
├── settings.gradle.kts
└── Dockerfile
```

## Prerequisites

- Java 21 (JDK)
- Docker & Docker Compose
- PostgreSQL 18.1 (or use Docker)
- Redis 8 (or use Docker)

## Getting Started

### Using Docker Compose (Recommended)

1. **Start all services**:
   ```bash
   docker-compose up -d
   ```

   This will start:
   - PostgreSQL 18.1 on port 5432
   - Redis 8 on port 6379
   - Spring Boot app on port 8080
   - MailHog (email testing) on ports 1025 (SMTP) and 8025 (Web UI)

2. **Access the application**:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/api/docs
   - MailHog UI: http://localhost:8025

3. **Stop all services**:
   ```bash
   docker-compose down
   ```

### Manual Setup

1. **Install PostgreSQL 18.1**:
   ```bash
   # Create database
   createdb gearvn_ecommerce
   ```

2. **Install Redis 8**:
   ```bash
   # Start Redis
   redis-server
   ```

3. **Build and run the application**:
   ```bash
   cd backend
   ./gradlew bootRun
   ```

## Configuration

### Environment Variables

For production, set these environment variables:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/gearvn_ecommerce
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=your-very-long-secret-key
ALLOWED_ORIGINS=https://yourdomain.com
```

### Application Profiles

- **dev**: Development profile (detailed logging, H2 console)
- **prod**: Production profile (optimized settings)

Activate profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Database Schema

The application uses Flyway for database migrations. Migrations are located in:
```
src/main/resources/db/migration/
```

Schema includes:
- Users & Roles (authentication)
- Products & Categories (catalog)
- Cart & Cart Items
- Orders & Order Items
- Payments
- Addresses
- Admin Users

## API Endpoints

### Public Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/products` - List products
- `GET /api/products/{id}` - Product details
- `GET /api/products/search` - Search products
- `GET /api/categories` - List categories

### Protected Endpoints (Requires JWT)
- `GET /api/cart` - Get user's cart
- `POST /api/cart/items` - Add to cart
- `POST /api/orders` - Create order
- `GET /api/orders` - List user's orders

### Admin Endpoints (Requires ADMIN role)
- `POST /api/admin/products` - Create product
- `PUT /api/admin/products/{id}` - Update product
- `DELETE /api/admin/products/{id}` - Delete product
- `GET /api/admin/orders` - Manage orders

## Development

### Build Commands

```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Run application
./gradlew bootRun

# Build Docker image
docker build -t gearvn-ecommerce:latest .
```

### Database Migrations

```bash
# Run migrations manually
./gradlew flywayMigrate

# Clean database (dev only)
./gradlew flywayClean
```

## Testing

Access Swagger UI for API testing:
- URL: http://localhost:8080/swagger-ui.html
- All endpoints are documented with request/response examples

## Monitoring

Spring Boot Actuator endpoints:
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Info: http://localhost:8080/actuator/info

## License

Copyright © 2025 GearVN
