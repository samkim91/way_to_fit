import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { leaderboardApi, stageApi } from '@/features/competition/api';
import type { CompetitionStage, GenderCategory, RegistrationType } from '@/features/competition/types';

interface FinalistSelectSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  competitionId: string;
  stage: CompetitionStage | null;
}

export function FinalistSelectSheet({ open, onOpenChange, competitionId, stage }: FinalistSelectSheetProps) {
  if (!stage) return null;

  const contentKey = `${stage.id}:${open ? 'open' : 'closed'}`;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-full max-w-lg overflow-y-auto">
        <SheetHeader>
          <SheetTitle>본선 진출자 선별 - {stage.name}</SheetTitle>
          <p className="text-sm text-muted-foreground">선택한 참가자/팀을 본선 진출 확정으로 저장합니다.</p>
        </SheetHeader>
        <FinalistSelectSheetContent
          key={contentKey}
          onOpenChange={onOpenChange}
          competitionId={competitionId}
          stage={stage}
        />
      </SheetContent>
    </Sheet>
  );
}

function FinalistSelectSheetContent({
  onOpenChange,
  competitionId,
  stage,
}: Omit<FinalistSelectSheetProps, 'open'> & { stage: CompetitionStage }) {
  const queryClient = useQueryClient();
  const [registrationType, setRegistrationType] = useState<RegistrationType | 'ALL'>('ALL');
  const [gender, setGender] = useState<GenderCategory | 'ALL'>('ALL');
  const [scaleCategory, setScaleCategory] = useState<string>('ALL');
  const [selectedIdsOverride, setSelectedIdsOverride] = useState<Set<string> | null>(null);

  const leaderboardParams = {
    registrationType: registrationType !== 'ALL' ? registrationType : undefined,
    gender: gender !== 'ALL' ? gender : undefined,
    scaleCategory: scaleCategory !== 'ALL' ? scaleCategory : undefined,
  };

  const { data: leaderboard, isLoading: leaderboardLoading } = useQuery({
    queryKey: ['leaderboard-overall', competitionId, stage.id, leaderboardParams],
    queryFn: () => leaderboardApi.getOverallLeaderboard(competitionId, stage.id, leaderboardParams),
  });

  const { data: existingFinalists } = useQuery({
    queryKey: ['finalists', competitionId, stage.id],
    queryFn: () => stageApi.getFinalists(competitionId, stage.id),
  });
  const selectedIds = selectedIdsOverride ?? new Set(existingFinalists ?? []);

  const saveMutation = useMutation({
    mutationFn: () =>
      stageApi.selectFinalists(competitionId, stage.id, Array.from(selectedIds)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['finalists', competitionId, stage.id] });
      onOpenChange(false);
    },
    onError: () => {
      alert('저장에 실패했습니다.');
    },
  });

  const entries = leaderboard?.entries ?? [];

  const allScaleCategories = Array.from(new Set(entries.map((e) => e.scaleCategory))).sort();

  const toggle = (id: string) => {
    setSelectedIdsOverride((prev) => {
      const next = new Set(prev ?? existingFinalists ?? []);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  return (
    <div className="mt-4 space-y-4">
          <div className="flex flex-wrap gap-2">
            <Select value={registrationType} onValueChange={(v) => setRegistrationType(v as typeof registrationType)}>
              <SelectTrigger className="w-28">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">전체</SelectItem>
                <SelectItem value="INDIVIDUAL">개인전</SelectItem>
                <SelectItem value="TEAM">팀전</SelectItem>
              </SelectContent>
            </Select>

            <Select value={gender} onValueChange={(v) => setGender(v as typeof gender)}>
              <SelectTrigger className="w-24">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">성별 전체</SelectItem>
                <SelectItem value="MEN">남</SelectItem>
                <SelectItem value="WOMEN">여</SelectItem>
                <SelectItem value="MIXED">혼성</SelectItem>
              </SelectContent>
            </Select>

            {allScaleCategories.length > 0 && (
              <Select value={scaleCategory} onValueChange={setScaleCategory}>
                <SelectTrigger className="w-28">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">스케일 전체</SelectItem>
                  {allScaleCategories.map((cat) => (
                    <SelectItem key={cat} value={cat}>{cat}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
          </div>

          {leaderboardLoading ? (
            <p className="text-sm text-muted-foreground">불러오는 중...</p>
          ) : entries.length === 0 ? (
            <p className="text-sm text-muted-foreground">리더보드 데이터가 없습니다.</p>
          ) : (
            <div className="space-y-1">
              {entries.map((entry) => (
                <label
                  key={entry.registrationId}
                  className="flex cursor-pointer items-center gap-3 rounded-md p-2 hover:bg-muted"
                >
                  <Checkbox
                    checked={selectedIds.has(entry.registrationId)}
                    onCheckedChange={() => toggle(entry.registrationId)}
                  />
                  <span className="w-8 text-sm font-medium text-muted-foreground">#{entry.rank}</span>
                  <span className="flex-1 text-sm font-medium">{entry.participantName}</span>
                  <span className="text-xs text-muted-foreground">{entry.scaleCategory}</span>
                  <span className="text-xs text-muted-foreground">{entry.totalPoints}pt</span>
                </label>
              ))}
            </div>
          )}

          <div className="flex items-center justify-between border-t pt-4">
            <span className="text-sm text-muted-foreground">선택: {selectedIds.size}명</span>
            <div className="flex gap-2">
              <Button variant="outline" onClick={() => onOpenChange(false)}>취소</Button>
              <Button onClick={() => saveMutation.mutate()} disabled={saveMutation.isPending}>
                {saveMutation.isPending ? '저장 중...' : '저장'}
              </Button>
            </div>
          </div>
    </div>
  );
}
