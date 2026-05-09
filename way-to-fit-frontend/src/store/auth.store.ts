import { create } from 'zustand';
import { ACCESS_TOKEN_KEY } from '@/lib/constants';

export type GlobalRole = 'USER' | 'ORGANIZER' | 'SUPER_ADMIN';

interface User {
  id: string;
  email: string;
  name: string;
  role: GlobalRole;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  
  setUser: (user: User | null) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  
  setUser: (user) => set({ user, isAuthenticated: !!user }),
  logout: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    set({ 
      user: null, 
      isAuthenticated: false
    });
  },
}));
