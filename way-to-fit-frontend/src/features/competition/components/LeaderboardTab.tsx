import { useQuery } from '@tanstack/react-query';
import { useOutletContext } from 'react-router-dom';
import { leaderboardApi, stageApi } from '@/features/competition/api';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useState, useEffect, useMemo } from 'react';
import { Maximize2, Wifi, WifiOff } from 'lucide-react';
import { useCompetitionWebSocket } from '@/hooks/useCompetitionWebSocket';

export function LeaderboardTab() {
  const { competitionId } = useOutletContext<{ competitionId: string }>();
  const [selectedStageId, setSelectedStageId] = useState<string>('');
  const [scaleCategory, setScaleCategory] = useState<string>('ALL');
  const [wsConnected, setWsConnected] = useState(false);
  const [wsData, setWsData] = useState<any>(null);

  const { data: stages } = useQuery({
    queryKey: ['stages', competitionId],
    queryFn: () => stageApi.getStages(competitionId),
  });

  useEffect(() => {
    if (stages && stages.length > 0 && !selectedStageId) {
      setSelectedStageId(stages[0].id);
    }
  }, [stages, selectedStageId]);

  const { data: initialLeaderboard, isLoading } = useQuery({
    queryKey: ['leaderboard', 'overall', competitionId, selectedStageId],
    queryFn: () => leaderboardApi.getOverallLeaderboard(competitionId, selectedStageId),
    enabled: !!selectedStageId,
  });

  const { isConnectedRef } = useCompetitionWebSocket({
    competitionId,
    stageId: selectedStageId,
    onOverallLeaderboardUpdate: (data) => {
      setWsData(data);
    }
  });

  useEffect(() => {
    const interval = setInterval(() => {
      setWsConnected(isConnectedRef.current);
    }, 1000);
    return () => clearInterval(interval);
  }, [isConnectedRef]);

  const leaderboard = wsData || initialLeaderboard;

  const allScaleCategories = useMemo(
    () => Array.from(new Set((leaderboard?.entries ?? []).map((e: any) => e.scaleCategory as string))).sort(),
    [leaderboard],
  );

  const displayEntries = useMemo(
    () => scaleCategory === 'ALL'
      ? (leaderboard?.entries ?? [])
      : (leaderboard?.entries ?? []).filter((e: any) => e.scaleCategory === scaleCategory),
    [leaderboard, scaleCategory],
  );

  const handleFullscreen = () => {
    const elem = document.documentElement;
    if (elem.requestFullscreen) {
      elem.requestFullscreen();
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <h2 className="text-xl font-bold">리더보드</h2>
          <Select value={selectedStageId} onValueChange={setSelectedStageId}>
            <SelectTrigger className="w-[200px]">
              <SelectValue placeholder="스테이지 선택" />
            </SelectTrigger>
            <SelectContent>
              {stages?.map(st => (
                <SelectItem key={st.id} value={st.id}>{st.name}</SelectItem>
              ))}
            </SelectContent>
          </Select>
          {allScaleCategories.length > 0 && (
            <Select value={scaleCategory} onValueChange={setScaleCategory}>
              <SelectTrigger className="w-32">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">스케일 전체</SelectItem>
                {allScaleCategories.map(cat => (
                  <SelectItem key={cat} value={cat}>{cat}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        </div>
        <div className="flex items-center space-x-4">
          <div className={`flex items-center px-3 py-1 rounded-full text-xs ${wsConnected ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
            {wsConnected ? <Wifi className="w-3 h-3 mr-1" /> : <WifiOff className="w-3 h-3 mr-1" />}
            {wsConnected ? '실시간 연결됨' : '연결 끊김'}
          </div>
          <Button variant="outline" size="sm" onClick={handleFullscreen}>
            <Maximize2 className="mr-2 h-4 w-4" /> 전체화면
          </Button>
        </div>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-16 text-center">#</TableHead>
              <TableHead>이름</TableHead>
              <TableHead>스케일</TableHead>
              <TableHead className="text-right">합계</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading && !wsData ? (
              <TableRow><TableCell colSpan={4} className="text-center">로딩 중...</TableCell></TableRow>
            ) : displayEntries.length === 0 ? (
              <TableRow><TableCell colSpan={4} className="text-center">데이터가 없습니다.</TableCell></TableRow>
            ) : (
              displayEntries.map((entry: any) => (
                <TableRow key={entry.registrationId}>
                  <TableCell className="text-center font-bold">{entry.rank}</TableCell>
                  <TableCell className="font-medium">
                    {entry.participantName}
                    {entry.manualRank && <span className="ml-2 text-orange-500 text-xs">⚑</span>}
                  </TableCell>
                  <TableCell>{entry.scaleCategory}</TableCell>
                  <TableCell className="text-right font-bold">{entry.totalPoints}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
