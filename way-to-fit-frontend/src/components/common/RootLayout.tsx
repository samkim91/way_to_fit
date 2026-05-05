import { Outlet, Navigate } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { ACCESS_TOKEN_KEY } from '@/lib/constants';
import { useAuthStore } from '@/store/auth.store';
import { useEffect } from 'react';

export function RootLayout() {
  const token = localStorage.getItem(ACCESS_TOKEN_KEY);
  const { user, setUser } = useAuthStore();

  // 새로고침 시 토큰이 있다면 상태 복원 (hydration)
  useEffect(() => {
    if (token && !user) {
      try {
        const payloadBase64 = token.split('.')[1];
        const base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
        const binaryString = atob(base64);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
          bytes[i] = binaryString.charCodeAt(i);
        }
        const jsonPayload = new TextDecoder().decode(bytes);
        const decodedPayload = JSON.parse(jsonPayload);
        
        const userObj = {
          id: decodedPayload.sub,
          email: decodedPayload.email || '', 
          name: decodedPayload.name || 'User',
          role: decodedPayload.role,
        };
        setUser(userObj);
      } catch (e) {
        console.error('Failed to parse JWT payload on RootLayout', e);
        // 토큰이 유효하지 않으면 삭제하고 로그인으로
        localStorage.removeItem(ACCESS_TOKEN_KEY);
        window.location.href = '/login';
      }
    }
  }, [token, user, setUser]);

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // user 상태가 아직 로드되지 않았으면 렌더링 지연 (RoleGuard 깜빡임 방지)
  if (!user) {
    return null;
  }

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      <Sidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        <Header />
        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
