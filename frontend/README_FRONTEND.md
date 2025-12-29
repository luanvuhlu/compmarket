# GearVN E-Commerce Frontend

A modern React + TypeScript frontend for the GearVN e-commerce platform.

## Tech Stack

- **React 19** - UI Library
- **TypeScript** - Type Safety
- **Vite** - Build Tool & Dev Server
- **React Router v7** - Routing
- **Axios** - HTTP Client
- **ESLint** - Code Linting

## Project Structure

```
frontend/
├── src/
│   ├── components/       # Reusable UI components
│   ├── pages/           # Page components
│   │   ├── HomePage.tsx
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   ├── ProductsPage.tsx
│   │   ├── ProductDetailPage.tsx
│   │   └── CartPage.tsx
│   ├── services/        # API services
│   │   ├── api.ts
│   │   ├── authService.ts
│   │   ├── productService.ts
│   │   └── cartService.ts
│   ├── context/         # React Context
│   │   └── AuthContext.tsx
│   ├── types/           # TypeScript types
│   │   └── index.ts
│   ├── hooks/           # Custom hooks
│   └── utils/           # Utility functions
├── public/              # Static assets
├── .env.development     # Development environment variables
├── .env.production      # Production environment variables
├── Dockerfile           # Docker configuration
├── nginx.conf           # Nginx configuration for production
└── vite.config.ts       # Vite configuration
```

## Getting Started

### Prerequisites

- Node.js 20+ (or use Docker)
- npm or yarn

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

The app will be available at http://localhost:5173

### Available Scripts

- `npm run dev` - Start development server with hot reload
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Environment Variables

Create `.env.development` and `.env.production` files:

```env
VITE_API_URL=http://localhost:8080/api
```

## Features

- ✅ User Authentication (Login/Register)
- ✅ Product Browsing & Search
- ✅ Product Details
- ✅ Shopping Cart Management
- ✅ JWT Token Management
- ✅ Protected Routes
- ✅ Responsive Design
- ✅ API Integration with Backend

## API Integration

The frontend communicates with the backend API through Axios with:
- Automatic JWT token injection
- Request/Response interceptors
- Error handling
- Base URL configuration

## Docker

Build and run with Docker:

```bash
# Build
docker build -t gearvn-frontend .

# Run
docker run -p 3000:80 gearvn-frontend
```

Or use docker-compose from the root directory:

```bash
docker-compose up frontend
```

## Development Notes

- The development server proxies API requests to `http://localhost:8080`
- In production, Nginx handles routing and proxies API requests
- JWT tokens are stored in localStorage
- Authentication state is managed through React Context

## Building for Production

```bash
npm run build
```

The optimized build will be in the `dist/` directory.

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
