import api from './api';
import type { Cart } from '../types';

export const cartService = {
  getCart: async (): Promise<Cart> => {
    const response = await api.get<{ success: boolean, data: Cart }>('/cart');
    return response.data.data;
  },

  addToCart: async (productId: number, quantity: number): Promise<Cart> => {
    const response = await api.post<{ success: boolean; data: Cart }>('/cart/items', {
      productId,
      quantity,
    });
    return response.data.data;
  },

  updateCartItem: async (itemId: number, quantity: number): Promise<Cart> => {
    const response = await api.put<{ success: boolean; data: Cart }>(`/cart/items/${itemId}`, {
      quantity,
    });
    return response.data.data;
  },

  removeFromCart: async (itemId: number): Promise<Cart> => {
    const response = await api.delete<{ success: boolean; data: Cart }>(`/cart/items/${itemId}`);
    return response.data.data;
  },

  clearCart: async (): Promise<void> => {
    await api.delete('/cart');
  },
};
