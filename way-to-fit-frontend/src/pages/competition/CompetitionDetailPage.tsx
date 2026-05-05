import { useQuery } from '@tanstack/react-query';
import { useParams, Link, useLocation, Outlet, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { competitionApi } from '@/features/competition/api';
import { CompetitionStatusBadge } from '@/features/competition/components/CompetitionStatusBadge';

import { ConfigTab } from '@/features/competition/components/ConfigTab';
import { RegistrationsTab } from '@/features/competition/components/RegistrationsTab';
import { ScoresTab } from '@/features/competition/components/ScoresTab';
import { LeaderboardTab } from '@/features/competition/components/LeaderboardTab';

export function CompetitionDetailPage() {
  const { competitionId } = useParams<{ competitionId: string }>();
  const location = useLocation();
  const navigate = useNavigate();

  const { data: competition, isLoading } = useQuery({
    queryKey: ['competition', competitionId],
    queryFn: () => competitionApi.getCompetition(competitionId!),
    enabled: !!competitionId,
  });

  if (isLoading) return <div className="p-8">로딩 중...</div>;
  if (!competition) return <div className="p-8">대회를 찾을 수 없습니다.</div>;

  const tabs = [
    { name: '구성', path: `/competitions/${competitionId}/config`, component: <ConfigTab /> },
    { name: '신청 관리', path: `/competitions/${competitionId}/registrations`, component: <RegistrationsTab /> },
    { name: '기록 판독', path: `/competitions/${competitionId}/scores`, component: <ScoresTab /> },
    { name: '리더보드', path: `/competitions/${competitionId}/leaderboard`, component: <LeaderboardTab /> },
  ];

  return (
    <div className="flex h-full flex-col">
      <div className="flex items-center space-x-4 pb-4 pt-2">
        <Button variant="ghost" onClick={() => navigate('/competitions')}>
          ← 대회 목록
        </Button>
        <h1 className="text-2xl font-bold">{competition.name}</h1>
        <CompetitionStatusBadge status={competition.status} />
        <div className="flex-1" />
        <Button variant="outline" onClick={() => navigate(`/competitions/${competitionId}/edit`)}>
          대회 편집
        </Button>
      </div>

      <div className="mb-6 flex space-x-1 border-b">
        {tabs.map((tab) => {
          const isActive = location.pathname.includes(tab.path);
          return (
            <Link
              key={tab.name}
              to={tab.path}
              className={`px-4 py-2 text-sm font-medium ${
                isActive
                  ? 'border-b-2 border-primary text-primary'
                  : 'text-muted-foreground hover:text-primary'
              }`}
            >
              {tab.name}
            </Link>
          );
        })}
      </div>

      <div className="flex-1 overflow-auto">
        <Outlet context={{ competitionId }} />
      </div>
    </div>
  );
}
