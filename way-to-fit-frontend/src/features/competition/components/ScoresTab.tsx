import { useQuery } from '@tanstack/react-query';
import { useOutletContext } from 'react-router-dom';
import { scoreApi, stageApi, eventApi } from '@/features/competition/api';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { ScoreStatusBadge } from './ScoreStatusBadge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useState } from 'react';

export function ScoresTab() {
  const { competitionId } = useOutletContext<{ competitionId: string }>();
  const [selectedEventId, setSelectedEventId] = useState<string>('');

  const { data: stages } = useQuery({
    queryKey: ['stages', competitionId],
    queryFn: () => stageApi.getStages(competitionId),
  });

  const { data: eventsData } = useQuery({
    queryKey: ['all-events', competitionId],
    queryFn: async () => {
      if (!stages) return [];
      const allEvents = await Promise.all(
        stages.map(s => eventApi.getEvents(competitionId, s.id))
      );
      return allEvents.flat();
    },
    enabled: !!stages,
  });

  const { data: scores, isLoading } = useQuery({
    queryKey: ['scores', competitionId, selectedEventId],
    queryFn: () => scoreApi.getScoresByEvent(competitionId, selectedEventId),
    enabled: !!selectedEventId,
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-4">
        <h2 className="text-xl font-bold">기록 판독</h2>
        <Select value={selectedEventId} onValueChange={setSelectedEventId}>
          <SelectTrigger className="w-[200px]">
            <SelectValue placeholder="이벤트 선택" />
          </SelectTrigger>
          <SelectContent>
            {eventsData?.map(ev => (
              <SelectItem key={ev.id} value={ev.id}>{ev.name}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>참가자</TableHead>
              <TableHead>기록</TableHead>
              <TableHead>상태</TableHead>
              <TableHead className="text-right">액션</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {!selectedEventId ? (
              <TableRow><TableCell colSpan={4} className="text-center">이벤트를 선택해주세요.</TableCell></TableRow>
            ) : isLoading ? (
              <TableRow><TableCell colSpan={4} className="text-center">로딩 중...</TableCell></TableRow>
            ) : scores?.length === 0 ? (
              <TableRow><TableCell colSpan={4} className="text-center">제출된 기록이 없습니다.</TableCell></TableRow>
            ) : (
              scores?.map(score => (
                <TableRow key={score.id}>
                  <TableCell className="font-medium">{score.registrationId.substring(0, 8)}</TableCell>
                  <TableCell>
                    {score.resultTimeSeconds ? `${Math.floor(score.resultTimeSeconds/60)}:${score.resultTimeSeconds%60}` : ''}
                    {score.resultRounds ? `${score.resultRounds}R ${score.resultReps}reps` : ''}
                    {score.resultWeight ? `${score.resultWeight}` : ''}
                  </TableCell>
                  <TableCell><ScoreStatusBadge status={score.status} /></TableCell>
                  <TableCell className="text-right">
                    <Button size="sm">판독 →</Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
