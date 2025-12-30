import api from './api';
import type { Product } from '../types';

export const adminService = {
  // Product Management
  createProduct: async (productData: {
    name: string;
    description: string;
    price: number;
    stockQuantity: number;
    categoryId?: number;
    imageUrl?: string;
  }): Promise<Product> => {
    const response = await api.post<Product>('/admin/products', productData);
    return response.data;
  },

  updateProduct: async (id: number, productData: {
    name: string;
    description: string;
    price: number;
    stockQuantity: number;
    categoryId?: number;
    imageUrl?: string;
  }): Promise<Product> => {
    const response = await api.put<Product>(`/admin/products/${id}`, productData);
    return response.data;
  },

  deleteProduct: async (id: number): Promise<void> => {
    await api.delete(`/admin/products/${id}`);
  },

  // Stats & Dashboard
  getDashboardStats: async () => {
    const response = await api.get('/admin/dashboard/stats');
    return response.data;
  },

  // Orders Management
  getAllOrders: async (page = 0, size = 20) => {
    const response = await api.get('/admin/orders', {
      params: { page, size },
    });
    return response.data;
  },

  updateOrderStatus: async (orderId: number, status: string) => {
    const response = await api.put(`/admin/orders/${orderId}/status`, { status });
    return response.data;
  },
};
