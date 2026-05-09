import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RootLayout, RoleGuard } from '@/components/common';
import { DashboardPage, LoginPage, AuthCallbackPage, ForbiddenPage } from '@/pages';
import { CompetitionListPage } from '@/pages/competition/CompetitionListPage';
import { CompetitionCreatePage } from '@/pages/competition/CompetitionCreatePage';
import { CompetitionEditPage } from '@/pages/competition/CompetitionEditPage';
import { CompetitionDetailPage } from '@/pages/competition/CompetitionDetailPage';
import { ConfigTab } from '@/features/competition/components/ConfigTab';
import { RegistrationsTab } from '@/features/competition/components/RegistrationsTab';
import { ScoresTab } from '@/features/competition/components/ScoresTab';
import { LeaderboardTab } from '@/features/competition/components/LeaderboardTab';
import '@/utils/date';
import axios from 'axios';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        if (axios.isAxiosError(error)) {
          const status = error.response?.status;
          if (status === 401 || status === 403) {
            return false;
          }
          if (!error.response || (status !== undefined && status >= 500)) {
            return failureCount < 1;
          }
        }
        return false;
      },
      refetchOnWindowFocus: false,
      staleTime: 1000 * 60 * 5,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/auth/callback" element={<AuthCallbackPage />} />
          <Route element={<RootLayout />}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/forbidden" element={<ForbiddenPage />} />
            <Route path="/members" element={<Navigate to="/" replace />} />
            <Route path="/members/:memberId" element={<Navigate to="/" replace />} />

            {/* 대회 관리 */}
            <Route path="/competitions" element={<CompetitionListPage />} />
            <Route
              path="/competitions/new"
              element={
                <RoleGuard allowedRoles={['ORGANIZER', 'SUPER_ADMIN']}>
                  <CompetitionCreatePage />
                </RoleGuard>
              }
            />
            <Route
              path="/competitions/:competitionId/edit"
              element={
                <RoleGuard allowedRoles={['ORGANIZER', 'SUPER_ADMIN']}>
                  <CompetitionEditPage />
                </RoleGuard>
              }
            />
            <Route path="/competitions/:competitionId" element={<CompetitionDetailPage />}>
              <Route index element={<Navigate to="config" replace />} />
              <Route path="config" element={<ConfigTab />} />
              <Route path="registrations" element={<RegistrationsTab />} />
              <Route path="scores" element={<ScoresTab />} />
              <Route path="leaderboard" element={<LeaderboardTab />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
