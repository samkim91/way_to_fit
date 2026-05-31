import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useOutletContext } from 'react-router-dom';
import { stageApi, eventApi, competitionApi } from '@/features/competition/api';
import { Button } from '@/components/ui/button';
import { formatDate } from '@/utils';
import { Plus } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  eventTypeLabels,
  genderCategoryLabels,
  stageFormatLabels,
  stageTypeLabels,
  wodTypeLabels,
} from '@/features/competition/labels';
import type { CompetitionEvent, CompetitionStage } from '@/features/competition/types';
import { StageFormDialog } from './StageFormDialog';
import { EventFormDialog } from './EventFormDialog';
import { FinalistSelectSheet } from './FinalistSelectSheet';

export function ConfigTab() {
  const { competitionId } = useOutletContext<{ competitionId: string }>();

  const [stageDialogOpen, setStageDialogOpen] = useState(false);
  const [editingStage, setEditingStage] = useState<CompetitionStage | null>(null);
  const [finalistSheetOpen, setFinalistSheetOpen] = useState(false);
  const [finalistStage, setFinalistStage] = useState<CompetitionStage | null>(null);

  const { data: competition } = useQuery({
    queryKey: ['competition', competitionId],
    queryFn: () => competitionApi.getCompetition(competitionId),
  });

  const { data: stages, isLoading } = useQuery({
    queryKey: ['stages', competitionId],
    queryFn: () => stageApi.getStages(competitionId),
  });

  const competitionScaleCategories = competition?.scaleCategories ?? [];

  const openCreateStage = () => { setEditingStage(null); setStageDialogOpen(true); };
  const openEditStage = (stage: CompetitionStage) => { setEditingStage(stage); setStageDialogOpen(true); };
  const openFinalistSheet = (stage: CompetitionStage) => { setFinalistStage(stage); setFinalistSheetOpen(true); };

  return (
    <div className="space-y-6">
      {competitionScaleCategories.length > 0 && (
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-base">참가 부문</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {competitionScaleCategories.map((cat) => (
                <span
                  key={cat}
                  className="rounded-full bg-secondary px-3 py-1 text-sm font-medium text-secondary-foreground"
                >
                  {cat}
                </span>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold">스테이지 구성</h2>
        <Button onClick={openCreateStage}>
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
            <Button variant="outline" onClick={openCreateStage}>
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
                    <Button variant="outline" size="sm" onClick={() => openFinalistSheet(stage)}>
                      본선 진출 선별
                    </Button>
                  )}
                  <Button variant="secondary" size="sm" onClick={() => openEditStage(stage)}>편집</Button>
                </div>
              </CardHeader>
              <CardContent>
                <div className="mb-4 flex space-x-4 text-sm text-muted-foreground">
                  <span>타입: {stageTypeLabels[stage.stageType]}</span>
                  <span>포맷: {stageFormatLabels[stage.stageFormat]}</span>
                  <span>
                    기간: {formatDate(stage.startAt)} ~ {formatDate(stage.endAt)}
                  </span>
                </div>

                <div className="mt-4 border-t pt-4">
                  <EventList
                    competitionId={competitionId}
                    stageId={stage.id}
                    competitionScaleCategories={competitionScaleCategories}
                  />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <StageFormDialog
        open={stageDialogOpen}
        onOpenChange={setStageDialogOpen}
        competitionId={competitionId}
        stage={editingStage}
      />
      <FinalistSelectSheet
        open={finalistSheetOpen}
        onOpenChange={setFinalistSheetOpen}
        competitionId={competitionId}
        stage={finalistStage}
      />
    </div>
  );
}

function EventList({
  competitionId,
  stageId,
  competitionScaleCategories,
}: {
  competitionId: string;
  stageId: string;
  competitionScaleCategories: string[];
}) {
  const queryClient = useQueryClient();
  const [eventDialogOpen, setEventDialogOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<CompetitionEvent | null>(null);

  const { data: events, isLoading } = useQuery({
    queryKey: ['events', competitionId, stageId],
    queryFn: () => eventApi.getEvents(competitionId, stageId),
  });

  const deleteMutation = useMutation({
    mutationFn: (eventId: string) => eventApi.deleteEvent(competitionId, stageId, eventId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events', competitionId, stageId] });
    },
    onError: () => {
      alert('이벤트 삭제에 실패했습니다. 기록이 있는 이벤트는 삭제할 수 없습니다.');
    },
  });

  const openCreateEvent = () => { setEditingEvent(null); setEventDialogOpen(true); };
  const openEditEvent = (event: CompetitionEvent) => { setEditingEvent(event); setEventDialogOpen(true); };
  const handleDelete = (event: CompetitionEvent) => {
    if (window.confirm(`"${event.name}" 이벤트를 삭제하시겠습니까?`)) {
      deleteMutation.mutate(event.id);
    }
  };

  if (isLoading) return <p className="text-sm">이벤트 불러오는 중...</p>;

  return (
    <>
      <div className="mb-4 flex items-center justify-between">
        <h3 className="font-semibold">이벤트 목록</h3>
        <Button variant="outline" size="sm" onClick={openCreateEvent}>
          <Plus className="mr-2 h-4 w-4" /> 이벤트 추가
        </Button>
      </div>

      {!events || events.length === 0 ? (
        <p className="text-sm text-muted-foreground">등록된 이벤트가 없습니다.</p>
      ) : (
        <div className="grid gap-3 md:grid-cols-2">
          {events.map((event) => (
            <Card
              key={event.id}
              className="cursor-pointer bg-slate-50 transition-colors hover:bg-slate-100 dark:bg-slate-900 dark:hover:bg-slate-800"
              onClick={() => openEditEvent(event)}
            >
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
                  <div className="flex flex-col space-y-1">
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-6 px-2 text-xs text-destructive hover:text-destructive"
                      onClick={(e) => { e.stopPropagation(); handleDelete(event); }}
                      disabled={deleteMutation.isPending}
                    >
                      삭제
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <EventFormDialog
        open={eventDialogOpen}
        onOpenChange={setEventDialogOpen}
        competitionId={competitionId}
        stageId={stageId}
        event={editingEvent}
        competitionScaleCategories={competitionScaleCategories}
      />
    </>
  );
}
