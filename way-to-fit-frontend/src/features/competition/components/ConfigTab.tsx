import { useQuery } from '@tanstack/react-query';
import { useOutletContext } from 'react-router-dom';
import { stageApi, eventApi } from '@/features/competition/api';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  eventTypeLabels,
  genderCategoryLabels,
  stageFormatLabels,
  stageTypeLabels,
  wodTypeLabels,
} from '@/features/competition/labels';

export function ConfigTab() {
  const { competitionId } = useOutletContext<{ competitionId: string }>();

  const { data: stages, isLoading } = useQuery({
    queryKey: ['stages', competitionId],
    queryFn: () => stageApi.getStages(competitionId),
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold">스테이지 구성</h2>
        <Button>
          <Plus className="mr-2 h-4 w-4" /> 스테이지 추가
        </Button>
      </div>

      {isLoading ? (
        <p>로딩 중...</p>
      ) : stages?.length === 0 ? (
        <Card className="border-dashed">
          <CardContent className="flex flex-col items-center justify-center p-12 text-center text-muted-foreground">
            <p>아직 등록된 스테이지가 없습니다.</p>
            <p className="mb-4">첫 번째 스테이지를 추가하여 대회를 구성해보세요.</p>
            <Button variant="outline">
              <Plus className="mr-2 h-4 w-4" /> 스테이지 추가
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {stages?.map((stage) => (
            <Card key={stage.id}>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-lg">{stage.name}</CardTitle>
                <div className="flex space-x-2">
                  {stage.stageType === 'QUALIFIER' && (
                    <Button variant="outline" size="sm">
                      본선 진출 선별
                    </Button>
                  )}
                  <Button variant="secondary" size="sm">편집</Button>
                </div>
              </CardHeader>
              <CardContent>
                <div className="mb-4 flex space-x-4 text-sm text-muted-foreground">
                  <span>타입: {stageTypeLabels[stage.stageType]}</span>
                  <span>포맷: {stageFormatLabels[stage.stageFormat]}</span>
                  <span>
                    기간: {new Date(stage.startAt).toLocaleDateString()} ~{' '}
                    {new Date(stage.endAt).toLocaleDateString()}
                  </span>
                </div>
                
                <div className="mt-4 border-t pt-4">
                  <div className="mb-4 flex items-center justify-between">
                    <h3 className="font-semibold">이벤트 목록</h3>
                    <Button variant="outline" size="sm">
                      <Plus className="mr-2 h-4 w-4" /> 이벤트 추가
                    </Button>
                  </div>
                  <EventList competitionId={competitionId} stageId={stage.id} />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

function EventList({ competitionId, stageId }: { competitionId: string; stageId: string }) {
  const { data: events, isLoading } = useQuery({
    queryKey: ['events', competitionId, stageId],
    queryFn: () => eventApi.getEvents(competitionId, stageId),
  });

  if (isLoading) return <p className="text-sm">이벤트 불러오는 중...</p>;
  if (!events || events.length === 0) return <p className="text-sm text-muted-foreground">등록된 이벤트가 없습니다.</p>;

  return (
    <div className="grid gap-3 md:grid-cols-2">
      {events.map(event => (
        <Card key={event.id} className="bg-slate-50 dark:bg-slate-900">
          <CardContent className="p-4">
            <div className="flex items-start justify-between">
              <div>
                <p className="font-semibold">
                  <span className="mr-2 text-muted-foreground">#{event.order}</span>
                  {event.name}
                </p>
                <p className="mt-1 text-xs text-muted-foreground">
                  {eventTypeLabels[event.eventType]} • {genderCategoryLabels[event.gender]} • {wodTypeLabels[event.wodType]}
                </p>
                <p className="mt-1 text-xs text-muted-foreground">
                  스케일: {event.scaleCategories.join(', ')}
                </p>
              </div>
              <div className="flex flex-col space-y-2">
                <Button variant="ghost" size="sm" className="h-6 px-2 text-xs">편집</Button>
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
