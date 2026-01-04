// User Types
export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role?: string;
}

// Auth Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  email: string;
  roles: string[];
}

// Product Types
export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  imageUrls?: string;
  category?: Category;
  createdAt?: string;
  updatedAt?: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}

// Cart Types
export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productPrice: number;
  imageUrls?: string;
  quantity: number;
}

export interface Cart {
  id: number;
  items: CartItem[];
  totalItems: number;
  totalPrice: number;
}

// Order Types
export interface Order {
  id: number;
  orderNumber: string;
  orderDate: string;
  status: string;
  totalAmount: number;
  items: OrderItem[];
  shippingAddress?: Address;
}

export interface OrderItem {
  id: number;
  product: Product;
  quantity: number;
  price: number;
}

// Address Types
export interface Address {
  id?: number;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

// API Response Types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}
