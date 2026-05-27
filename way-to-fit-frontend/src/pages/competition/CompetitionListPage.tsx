import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Trophy, Plus, Calendar, Users, ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { formatDate } from '@/utils';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { competitionApi } from '@/features/competition/api';
import { CompetitionStatusBadge } from '@/features/competition/components/CompetitionStatusBadge';
import { CompetitionVisibilityBadge } from '@/features/competition/components/CompetitionVisibilityBadge';
import type { CompetitionLifecycle } from '@/features/competition/types';
import { useState } from 'react';

const PAGE_SIZE = 20;

export function CompetitionListPage() {
  const navigate = useNavigate();
  const [filter, setFilter] = useState<'ALL' | CompetitionLifecycle>('ALL');
  const [page, setPage] = useState(0);

  const { data: pageData, isLoading, isFetching } = useQuery({
    queryKey: ['my-competitions', filter, page, PAGE_SIZE],
    queryFn: () =>
      competitionApi.getMyCompetitions({
        lifecycles: filter === 'ALL' ? undefined : [filter],
        page,
        size: PAGE_SIZE,
      }),
  });

  const competitions = pageData?.content ?? [];
  const isEmpty = competitions.length === 0;
  const isFilteredEmpty = filter !== 'ALL' && pageData && pageData.totalElements === 0;
  const pageNumber = (pageData?.number ?? page) + 1;
  const totalPages = pageData?.totalPages ?? 0;
  const totalElements = pageData?.totalElements ?? 0;

  const handleFilterChange = (nextFilter: 'ALL' | CompetitionLifecycle) => {
    setFilter(nextFilter);
    setPage(0);
  };

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
          onClick={() => handleFilterChange('ALL')}
        >
          전체
        </Button>
        <Button
          variant={filter === 'REGISTRATION_OPEN' ? 'default' : 'outline'}
          size="sm"
          onClick={() => handleFilterChange('REGISTRATION_OPEN')}
        >
          신청중
        </Button>
        <Button
          variant={filter === 'IN_PROGRESS' ? 'default' : 'outline'}
          size="sm"
          onClick={() => handleFilterChange('IN_PROGRESS')}
        >
          진행중
        </Button>
        <Button
          variant={filter === 'COMPLETED' ? 'default' : 'outline'}
          size="sm"
          onClick={() => handleFilterChange('COMPLETED')}
        >
          종료
        </Button>
      </div>

      {isLoading ? (
        <div className="flex h-40 items-center justify-center">
          <p className="text-muted-foreground">로딩 중...</p>
        </div>
      ) : isEmpty ? (
        <div className="flex flex-col items-center justify-center rounded-lg border border-dashed p-12 text-center">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
            <Trophy className="h-6 w-6 text-primary" />
          </div>
          <h3 className="mt-4 text-lg font-semibold">
            {isFilteredEmpty ? '조건에 맞는 대회가 없습니다' : '대회가 없습니다'}
          </h3>
          <p className="mb-4 mt-2 text-sm text-muted-foreground">
            {isFilteredEmpty
              ? '다른 상태를 선택하거나 새 대회를 만들어 운영을 시작해보세요.'
              : '첫 번째 대회를 만들어 운영을 시작해보세요.'}
          </p>
          <Button onClick={() => navigate('/competitions/new')}>
            <Plus className="mr-2 h-4 w-4" />새 대회 만들기
          </Button>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="flex items-center justify-between text-sm text-muted-foreground">
            <span>총 {totalElements}개</span>
            {isFetching && <span>목록을 새로 불러오는 중...</span>}
          </div>

          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {competitions.map((competition) => (
              <Card
                key={competition.id}
                className="flex cursor-pointer flex-col justify-between transition-colors hover:bg-muted/40"
                onClick={() => navigate(`/competitions/${competition.id}/config`)}
              >
                <CardHeader className="pb-4">
                  <div className="flex items-start justify-between gap-3">
                    <CardTitle className="line-clamp-2 text-lg">{competition.name}</CardTitle>
                    <div className="flex items-center gap-2">
                      <CompetitionVisibilityBadge visibility={competition.visibility} />
                      <CompetitionStatusBadge status={competition.lifecycle} />
                    </div>
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
                        {formatDate(competition.startAt)} - {formatDate(competition.endAt)}
                      </span>
                    </div>
                    <div className="flex items-center">
                      <Users className="mr-2 h-4 w-4" />
                      <span>참가자 0명</span>
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
                    {competition.visibility === 'PRIVATE' ? (
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

          <div className="flex flex-col gap-3 rounded-lg border bg-card px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
            <span className="text-sm text-muted-foreground">
              {pageNumber} / {Math.max(totalPages, 1)} 페이지
            </span>
            <div className="flex items-center gap-2 self-end sm:self-auto">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                disabled={page === 0 || isFetching}
              >
                <ChevronLeft className="mr-1 h-4 w-4" />
                이전
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((prev) => prev + 1)}
                disabled={totalPages === 0 || page >= totalPages - 1 || isFetching}
              >
                다음
                <ChevronRight className="ml-1 h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
