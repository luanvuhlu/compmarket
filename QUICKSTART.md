# GearVN E-Commerce - Quick Start Guide

## 🚀 Quick Start (Docker - Recommended)

### Prerequisites
- Docker Desktop installed and running
- Git

### Steps

1. **Navigate to project directory**:
   ```powershell
   cd e:\Git\gearvn
   ```

2. **Start all services**:
   ```powershell
   docker-compose up -d
   ```

   This starts:
   - PostgreSQL 18.1 (port 5432)
   - Redis 8 (port 6379)
   - Spring Boot API (port 8080)
   - MailHog (SMTP: 1025, Web UI: 8025)

3. **Wait for services to be ready** (about 30-60 seconds)

4. **Access the application**:
   - 📚 **Swagger UI**: http://localhost:8080/swagger-ui.html
   - 📖 **API Docs**: http://localhost:8080/api/docs
   - 📧 **MailHog**: http://localhost:8025
   - ✅ **Health Check**: http://localhost:8080/actuator/health

5. **Test the API with Swagger UI**:
   - Open http://localhost:8080/swagger-ui.html
   - Try the `/api/auth/register` endpoint to create a user
   - Use `/api/auth/login` to get a JWT token
   - Click "Authorize" button and paste the token
   - Test protected endpoints like `/api/cart`

6. **Stop services**:
   ```powershell
   docker-compose down
   ```

## 📋 API Endpoints Overview

### Public Endpoints (No authentication required)

#### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

#### Products
- `GET /api/products` - List all products (paginated)
- `GET /api/products/{id}` - Get product details
- `GET /api/products/search?keyword={keyword}` - Search products
- `GET /api/products/category/{categoryId}` - Products by category

### Protected Endpoints (Requires JWT token)

#### Shopping Cart
- `GET /api/cart` - Get user's cart
- `POST /api/cart/items` - Add item to cart
- `PUT /api/cart/items/{itemId}` - Update cart item quantity
- `DELETE /api/cart/items/{itemId}` - Remove item from cart
- `DELETE /api/cart` - Clear cart

### Admin Endpoints (Requires ADMIN or SUPER_ADMIN role)

#### Product Management
- `POST /api/admin/products` - Create product
- `PUT /api/admin/products/{id}` - Update product
- `DELETE /api/admin/products/{id}` - Delete product

## 🧪 Testing with Swagger UI

### 1. Register a User
```json
POST /api/auth/register
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "1234567890"
}
```

### 2. Login
```json
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}
```

Copy the `token` from the response.

### 3. Authorize Swagger
- Click the **"Authorize"** button at the top
- Enter: `Bearer YOUR_TOKEN_HERE`
- Click **"Authorize"** then **"Close"**

### 4. Test Protected Endpoints
Now you can access protected endpoints like:
- `GET /api/cart` - View your cart
- `POST /api/cart/items` - Add products to cart

## 🗄️ Database Access

### Using Docker
```powershell
# Connect to PostgreSQL container
docker exec -it gearvn-postgres psql -U postgres -d gearvn_ecommerce

# View tables
\dt

# Query users
SELECT * FROM users;

# Exit
\q
```

### Using pgAdmin or DBeaver
- Host: localhost
- Port: 5432
- Database: gearvn_ecommerce
- Username: postgres
- Password: postgres

## 🔍 View Logs

```powershell
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f app
docker-compose logs -f postgres
docker-compose logs -f redis
```

## 🛠️ Development Workflow

### Making Code Changes

1. **Make your changes** in the backend code

2. **Rebuild and restart**:
   ```powershell
   docker-compose up -d --build
   ```

### Running Locally (without Docker)

1. **Start PostgreSQL and Redis**:
   ```powershell
   docker-compose up -d postgres redis mailhog
   ```

2. **Run Spring Boot**:
   ```powershell
   cd backend
   .\gradlew.bat bootRun
   ```

## 📦 Project Structure

```
backend/
├── src/main/kotlin/com/gearvn/ecommerce/
│   ├── config/           # Configuration (Security, CORS, Swagger)
│   ├── controller/       # REST Controllers
│   ├── dto/              # Data Transfer Objects
│   ├── entity/           # JPA Entities
│   ├── exception/        # Custom Exceptions & Global Handler
│   ├── repository/       # Spring Data Repositories
│   ├── security/         # JWT & Security
│   └── service/          # Business Logic
├── src/main/resources/
│   ├── db/migration/     # Flyway SQL Scripts
│   └── application*.yml  # Configuration Files
└── build.gradle.kts      # Gradle Build File
```

## 🎯 Next Steps

1. ✅ Test all endpoints with Swagger UI
2. ✅ Create sample products and categories
3. ✅ Test the shopping cart workflow
4. 🔜 Build the React frontend
5. 🔜 Implement order and payment features
6. 🔜 Add file upload for product images

## 🐛 Troubleshooting

### Port Already in Use
```powershell
# Check what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID)
taskkill /PID <PID> /F
```

### Database Connection Issues
```powershell
# Recreate database container
docker-compose down -v
docker-compose up -d
```

### View Application Logs
```powershell
# Inside container
docker-compose logs -f app

# Local file
cat backend/logs/application.log
```

## 📞 Support

For issues or questions, check:
- API Documentation: http://localhost:8080/swagger-ui.html
- Application Logs: `docker-compose logs -f app`
- Database Logs: `docker-compose logs -f postgres`
