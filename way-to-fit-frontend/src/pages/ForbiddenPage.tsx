import { useNavigate } from 'react-router-dom';
import { ShieldOff } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useAuthStore } from '@/store/auth.store';
import { ROLE_LABELS } from '@/lib/constants';

export function ForbiddenPage() {
  const navigate = useNavigate();
  const { user } = useAuthStore();

  return (
    <div className="flex flex-1 flex-col items-center justify-center gap-6 p-8 text-center">
      <ShieldOff className="h-16 w-16 text-muted-foreground" />
      <div className="space-y-2">
        <h1 className="text-2xl font-bold">접근 권한이 없습니다</h1>
        <p className="text-muted-foreground">
          이 페이지는 대회 주최자 이상의 권한이 필요합니다.
        </p>
        {user && (
          <p className="text-sm text-muted-foreground">
            현재 계정: {user.name} ({ROLE_LABELS[user.role] ?? user.role})
          </p>
        )}
      </div>
      <Button onClick={() => navigate('/')}>홈으로 돌아가기</Button>
    </div>
  );
}
