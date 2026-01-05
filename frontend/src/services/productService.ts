import api from './api';
import type { Product, PaginatedResponse } from '../types';

export const productService = {
  getAllProducts: async (page = 0, size = 20) => {
    const response = await api.get<PaginatedResponse<Product>>('/products', {
      params: { page, size },
    });
    return response.data;
  },

  getProductById: async (id: number): Promise<Product> => {
    const response = await api.get<{ success: boolean; data: Product }>(`/products/${id}`);
    return response.data.data;
  },

  searchProducts: async (query: string, page = 0, size = 20) => {
    const response = await api.get<PaginatedResponse<Product>>('/products/search', {
      params: { query, page, size },
    });
    return response.data;
  },

  getProductsByCategory: async (categoryId: number, page = 0, size = 20) => {
    const response = await api.get<PaginatedResponse<Product>>('/products/category/' + categoryId, {
      params: { page, size },
    });
    return response.data;
  },
};
