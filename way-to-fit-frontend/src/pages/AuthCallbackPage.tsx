import { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ACCESS_TOKEN_KEY } from '@/lib/constants';
import { useAuthStore } from '@/store/auth.store';
import { Dumbbell } from 'lucide-react';
import apiClient from '@/api/client';

export function AuthCallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { setUser } = useAuthStore();
  const isExchanging = useRef(false);

  useEffect(() => {
    const code = searchParams.get('code');
    
    if (code && !isExchanging.current) {
      isExchanging.current = true;
      
      const exchangeToken = async () => {
        try {
          const { data } = await apiClient.post('/auth/token', { code });
          
          if (data.code === '0000' && data.data?.accessToken) {
            const token = data.data.accessToken;
            localStorage.setItem(ACCESS_TOKEN_KEY, token);
            
            // JWT 디코딩하여 유저 정보 파싱
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

            navigate('/', { replace: true });
          } else {
            console.error('Failed to exchange token:', data.message);
            navigate('/login', { replace: true });
          }
        } catch (error) {
          console.error('Error exchanging token:', error);
          navigate('/login', { replace: true });
        }
      };

      exchangeToken();
    } else if (!code) {
      navigate('/login', { replace: true });
    }
  }, [searchParams, navigate, setUser]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <div className="flex flex-col items-center gap-6 animate-pulse">
        <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-primary shadow-lg">
          <Dumbbell className="h-8 w-8 text-primary-foreground animate-spin" style={{ animationDuration: '3s' }} />
        </div>
        <p className="text-muted-foreground text-lg font-medium">
          로그인 처리 중입니다...
        </p>
      </div>
    </div>
  );
}
