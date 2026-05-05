import { Button } from '@/components/ui/button';
import { useAuthStore, useThemeStore } from '@/store';
import { Moon, Sun, LogOut } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import apiClient from '@/api/client';
import { notifyApiError } from '@/lib/notifier';

export function Header() {
  const { user, logout } = useAuthStore();
  const { theme, setTheme } = useThemeStore();
  const navigate = useNavigate();

  const toggleTheme = () => {
    const next = theme === 'system' ? 'dark' : theme === 'dark' ? 'light' : 'system';
    setTheme(next);
  };

  const handleLogout = async () => {
    try {
      // 백엔드 로그아웃 API 호출 (POST)
      // VITE_API_BASE_URL에 이미 /api가 포함되어 있으므로 /auth/logout만 호출합니다.
      await apiClient.post('/auth/logout');
    } catch (error) {
      console.error('Logout request failed', error);
      notifyApiError(error, '로그아웃 중 오류가 발생했습니다.');
    } finally {
      logout(); // Zustand 상태 초기화 및 localStorage에서 토큰 제거
      navigate('/login', { replace: true });
    }
  };

  return (
    <header className="flex h-16 items-center justify-between border-b border-border bg-background px-6">
      <div />

      <div className="flex items-center gap-3">
        {/* Theme Toggle */}
        <Button variant="ghost" size="icon" onClick={toggleTheme}>
          {theme === 'dark' ? (
            <Sun className="h-4 w-4" />
          ) : (
            <Moon className="h-4 w-4" />
          )}
        </Button>

        {/* User Info & Logout */}
        {user && (
          <div className="flex items-center gap-3">
            <span className="text-sm text-muted-foreground">{user.name}</span>
            <Button variant="ghost" size="icon" onClick={handleLogout}>
              <LogOut className="h-4 w-4" />
            </Button>
          </div>
        )}
      </div>
    </header>
  );
}
