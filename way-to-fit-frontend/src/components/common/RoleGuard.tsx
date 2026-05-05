import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '@/store/auth.store';
import type { GlobalRole } from '@/store/auth.store';

interface RoleGuardProps {
  children: ReactNode;
  allowedRoles: GlobalRole[];
  fallbackTo?: string; // 권한 없을 시 리다이렉트할 경로 (기본: 메인 페이지)
}

export function RoleGuard({ children, allowedRoles, fallbackTo = '/' }: RoleGuardProps) {
  const { user } = useAuthStore();
  const currentRole = user?.role;

  // 현재 유저의 역할이 허용된 역할 목록에 있는지 확인
  const hasAccess = currentRole && allowedRoles.includes(currentRole);

  if (!hasAccess) {
    // 권한이 없으면 fallback 경로로 리다이렉트
    return <Navigate to={fallbackTo} replace />;
  }

  // 권한이 있으면 정상적으로 컴포넌트 렌더링
  return <>{children}</>;
}
