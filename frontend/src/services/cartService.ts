import api from './api';
import type { Cart } from '../types';

export const cartService = {
  getCart: async (): Promise<Cart> => {
    const response = await api.get<Cart>('/cart');
    return response.data;
  },

  addToCart: async (productId: number, quantity: number): Promise<Cart> => {
    const response = await api.post<Cart>('/cart/items', {
      productId,
      quantity,
    });
    return response.data;
  },

  updateCartItem: async (itemId: number, quantity: number): Promise<Cart> => {
    const response = await api.put<Cart>(`/cart/items/${itemId}`, {
      quantity,
    });
    return response.data;
  },

  removeFromCart: async (itemId: number): Promise<Cart> => {
    const response = await api.delete<Cart>(`/cart/items/${itemId}`);
    return response.data;
  },

  clearCart: async (): Promise<void> => {
    await api.delete('/cart');
  },
};
