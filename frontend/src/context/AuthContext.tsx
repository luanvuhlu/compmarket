import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { User } from '../types';
import { authService } from '../services/authService';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, firstName: string, lastName: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const token = localStorage.getItem('token');
      const userDataStr = localStorage.getItem('userData');
      if (token && userDataStr) {
        try {
          const userData = JSON.parse(userDataStr);
          setUser(userData);
        } catch (error) {
          localStorage.removeItem('token');
          localStorage.removeItem('userData');
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email: string, password: string) => {
    const response = await authService.login({ email, password });
    // Create user object from auth response
    const userData = {
      id: 0, // Backend doesn't return this yet
      email: response.email,
      firstName: email.split('@')[0], // Temporary until backend returns proper user data
      lastName: '',
      role: response.roles[0] || 'CUSTOMER'
    };
    localStorage.setItem('userData', JSON.stringify(userData));
    setUser(userData);
  };

  const register = async (email: string, password: string, firstName: string, lastName: string) => {
    const response = await authService.register({ email, password, firstName, lastName });
    // Create user object from auth response
    const userData = {
      id: 0, // Backend doesn't return this yet
      email: response.email,
      firstName,
      lastName,
      role: response.roles[0] || 'CUSTOMER'
    };
    localStorage.setItem('userData', JSON.stringify(userData));
    setUser(userData);
  };

  const logout = () => {
    authService.logout();
    localStorage.removeItem('userData');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
