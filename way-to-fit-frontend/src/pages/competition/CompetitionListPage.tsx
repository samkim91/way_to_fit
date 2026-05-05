import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Trophy, Plus, Calendar, Users } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { competitionApi } from '@/features/competition/api';
import { CompetitionStatusBadge } from '@/features/competition/components/CompetitionStatusBadge';
import type { CompetitionStatus } from '@/features/competition/types';
import { useState } from 'react';

export function CompetitionListPage() {
  const navigate = useNavigate();
  const [filter, setFilter] = useState<'ALL' | CompetitionStatus>('ALL');

  const { data: competitions, isLoading } = useQuery({
    queryKey: ['my-competitions'],
    queryFn: competitionApi.getMyCompetitions,
  });

  const filteredCompetitions = competitions?.filter((c) => {
    if (filter === 'ALL') return true;
    return c.status === filter;
  });

  return (
    <div className="flex flex-col space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">대회 관리</h1>
          <p className="text-muted-foreground">내가 주최하는 대회 목록입니다.</p>
        </div>
        <Button onClick={() => navigate('/competitions/new')}>
          <Plus className="mr-2 h-4 w-4" />새 대회 만들기
        </Button>
      </div>

      <div className="flex space-x-2">
        <Button
          variant={filter === 'ALL' ? 'default' : 'outline'}
          size="sm"
          onClick={() => setFilter('ALL')}
        >
          전체
        </Button>
        <Button
          variant={filter === 'REGISTRATION_OPEN' ? 'default' : 'outline'}
          size="sm"
          onClick={() => setFilter('REGISTRATION_OPEN')}
        >
          신청중
        </Button>
        <Button
          variant={filter === 'IN_PROGRESS' ? 'default' : 'outline'}
          size="sm"
          onClick={() => setFilter('IN_PROGRESS')}
        >
          진행중
        </Button>
        <Button
          variant={filter === 'COMPLETED' ? 'default' : 'outline'}
          size="sm"
          onClick={() => setFilter('COMPLETED')}
        >
          종료
        </Button>
      </div>

      {isLoading ? (
        <div className="flex h-40 items-center justify-center">
          <p className="text-muted-foreground">로딩 중...</p>
        </div>
      ) : filteredCompetitions?.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-lg border border-dashed p-12 text-center">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
            <Trophy className="h-6 w-6 text-primary" />
          </div>
          <h3 className="mt-4 text-lg font-semibold">대회가 없습니다</h3>
          <p className="mb-4 mt-2 text-sm text-muted-foreground">
            첫 번째 대회를 만들어 운영을 시작해보세요.
          </p>
          <Button onClick={() => navigate('/competitions/new')}>
            <Plus className="mr-2 h-4 w-4" />새 대회 만들기
          </Button>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {filteredCompetitions?.map((competition) => (
            <Card
              key={competition.id}
              className="flex cursor-pointer flex-col justify-between transition-colors hover:bg-muted/40"
              onClick={() => navigate(`/competitions/${competition.id}/config`)}
            >
              <CardHeader className="pb-4">
                <div className="flex items-start justify-between">
                  <CardTitle className="line-clamp-2 text-lg">{competition.name}</CardTitle>
                  <CompetitionStatusBadge status={competition.status} />
                </div>
                <CardDescription className="line-clamp-1 mt-2">
                  {competition.description}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid gap-2 text-sm text-muted-foreground">
                  <div className="flex items-center">
                    <Calendar className="mr-2 h-4 w-4" />
                    <span>
                      {new Date(competition.startAt).toLocaleDateString()} -{' '}
                      {new Date(competition.endAt).toLocaleDateString()}
                    </span>
                  </div>
                  <div className="flex items-center">
                    <Users className="mr-2 h-4 w-4" />
                    <span>참가자 0명</span> {/* MVP: 참가자 수 하드코딩 */}
                  </div>
                </div>
                <div className="mt-4 flex space-x-2">
                  <Button
                    variant="outline"
                    className="flex-1"
                    onClick={(event) => {
                      event.stopPropagation();
                      navigate(`/competitions/${competition.id}/scores`);
                    }}
                  >
                    기록 판독
                  </Button>
                  {competition.status === 'DRAFT' ? (
                    <Button
                      variant="secondary"
                      className="flex-1"
                      onClick={(event) => {
                        event.stopPropagation();
                        navigate(`/competitions/${competition.id}/edit`);
                      }}
                    >
                      편집
                    </Button>
                  ) : (
                    <Button
                      variant="secondary"
                      className="flex-1"
                      onClick={(event) => {
                        event.stopPropagation();
                        navigate(`/competitions/${competition.id}/registrations`);
                      }}
                    >
                      신청 관리
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
