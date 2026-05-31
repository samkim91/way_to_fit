import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ExternalLink, Play } from 'lucide-react';
import { useOutletContext } from 'react-router-dom';
import { scoreApi, stageApi, eventApi, registrationApi } from '@/features/competition/api';
import type {
  CompetitionEvent,
  CompetitionScore,
  Registration,
  ResultStatus,
  ReviewScoreRequest,
  ScoreStatus,
} from '@/features/competition/types';
import { scoreStatusLabels } from '@/features/competition/labels';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Textarea } from '@/components/ui/textarea';
import { ScoreStatusBadge } from './ScoreStatusBadge';

type StatusFilter = 'ALL' | ScoreStatus;

type ReviewFormState = {
  status: ScoreStatus;
  reviewerNote: string;
  resultTimeMinutes: string;
  resultTimeSeconds: string;
  resultRounds: string;
  resultReps: string;
  resultWeight: string;
  resultCustom: string;
  resultStatus: ResultStatus;
};

const STATUS_FILTERS: Array<{ value: StatusFilter; label: string }> = [
  { value: 'ALL', label: '전체' },
  { value: 'SUBMITTED', label: scoreStatusLabels.SUBMITTED },
  { value: 'APPROVED', label: scoreStatusLabels.APPROVED },
  { value: 'ADJUSTED', label: scoreStatusLabels.ADJUSTED },
  { value: 'REJECTED', label: scoreStatusLabels.REJECTED },
];

const GENDER_LABELS: Record<string, string> = {
  MALE: '남',
  FEMALE: '여',
  MIXED: '혼성',
};

function parseEventOrder(name: string): number | null {
  const match = name.match(/(?:event|wod)\s*([0-9]+)/i);
  return match ? Number(match[1]) : null;
}

function formatEventLabel(event: CompetitionEvent): string {
  const order = event.order > 0 ? event.order : parseEventOrder(event.name);
  const base = order ? `Event ${order}` : 'Event';
  return `${base} - ${event.name}`;
}

function formatEventButtonLabel(event: CompetitionEvent): string {
  const order = event.order > 0 ? event.order : parseEventOrder(event.name);
  return order ? `Event ${order}` : event.name;
}

function formatDateTime(value: string | null): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '-';
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date);
}

function formatResult(score: CompetitionScore): string {
  if (score.resultStatus === 'DNF') return 'DNF';
  if (score.resultTimeSeconds !== null) {
    const minutes = Math.floor(score.resultTimeSeconds / 60);
    const seconds = score.resultTimeSeconds % 60;
    return `${minutes}:${String(seconds).padStart(2, '0')}`;
  }
  if (score.resultRounds !== null || score.resultReps !== null) {
    const rounds = score.resultRounds ?? 0;
    const reps = score.resultReps ?? 0;
    return `${rounds}R ${reps}reps`;
  }
  if (score.resultWeight) return score.resultWeight;
  if (score.resultCustom) return score.resultCustom;
  return '-';
}

function buildReviewForm(score: CompetitionScore): ReviewFormState {
  const totalSeconds = score.resultTimeSeconds ?? 0;
  const defaultStatus: ScoreStatus =
    score.status === 'APPROVED' || score.status === 'ADJUSTED' || score.status === 'REJECTED'
      ? score.status
      : 'APPROVED';

  return {
    status: defaultStatus,
    reviewerNote: score.reviewerNote ?? '',
    resultTimeMinutes:
      score.resultTimeSeconds !== null ? String(Math.floor(totalSeconds / 60)) : '',
    resultTimeSeconds:
      score.resultTimeSeconds !== null ? String(totalSeconds % 60).padStart(2, '0') : '',
    resultRounds: score.resultRounds !== null ? String(score.resultRounds) : '',
    resultReps: score.resultReps !== null ? String(score.resultReps) : '',
    resultWeight: score.resultWeight ?? '',
    resultCustom: score.resultCustom ?? '',
    resultStatus: score.resultStatus,
  };
}

function buildReviewPayload(form: ReviewFormState): ReviewScoreRequest {
  const minutes = form.resultTimeMinutes.trim();
  const seconds = form.resultTimeSeconds.trim();
  const hasTime = minutes !== '' || seconds !== '';

  return {
    status: form.status,
    reviewerNote: form.reviewerNote.trim() || undefined,
    resultTimeSeconds: hasTime ? Number(minutes || '0') * 60 + Number(seconds || '0') : null,
    resultRounds: form.resultRounds.trim() ? Number(form.resultRounds) : null,
    resultReps: form.resultReps.trim() ? Number(form.resultReps) : null,
    resultWeight: form.resultWeight.trim() || null,
    resultCustom: form.resultCustom.trim() || null,
    resultStatus: form.resultStatus,
  };
}

export function ScoresTab() {
  const { competitionId } = useOutletContext<{ competitionId: string }>();
  const queryClient = useQueryClient();
  const [selectedEventId, setSelectedEventId] = useState<'ALL' | string>('ALL');
  const [statusFilter, setStatusFilter] = useState<StatusFilter>(STATUS_FILTERS[0].value);
  const [activeScore, setActiveScore] = useState<CompetitionScore | null>(null);
  const [reviewForm, setReviewForm] = useState<ReviewFormState | null>(null);

  const isAllMode = selectedEventId === 'ALL';

  const { data: stages } = useQuery({
    queryKey: ['stages', competitionId],
    queryFn: () => stageApi.getStages(competitionId),
  });

  const { data: events, isLoading: isEventsLoading } = useQuery({
    queryKey: ['score-events', competitionId, stages?.map((stage) => stage.id).join(',')],
    queryFn: async () => {
      if (!stages?.length) return [];
      const allEvents = await Promise.all(
        stages.map(async (stage) => {
          const stageEvents = await eventApi.getEvents(competitionId, stage.id);
          return stageEvents.map((event) => ({ ...event, stageName: stage.name }));
        }),
      );
      return allEvents.flat().sort((a, b) => a.order - b.order);
    },
    enabled: !!stages,
  });

  const eventMap = useMemo(
    () => (events ?? []).reduce<Record<string, CompetitionEvent>>((acc, e) => { acc[e.id] = e; return acc; }, {}),
    [events],
  );

  const { data: pageData } = useQuery({
    queryKey: ['registrations', competitionId],
    queryFn: () => registrationApi.getRegistrations(competitionId),
  });

  const registrations = pageData?.content ?? [];
  const registrationMap = useMemo(
    () =>
      registrations.reduce<Record<string, Registration>>((acc, registration) => {
        acc[registration.id] = registration;
        return acc;
      }, {}),
    [registrations],
  );

  const scoreStatusParam = statusFilter === 'ALL' ? undefined : statusFilter;

  const { data: singleEventScores, isLoading: isSingleLoading } = useQuery({
    queryKey: ['scores', competitionId, selectedEventId, scoreStatusParam],
    queryFn: () => scoreApi.getScoresByEvent(competitionId, selectedEventId, scoreStatusParam),
    enabled: !isAllMode && !!selectedEventId,
  });

  const { data: allEventScores, isLoading: isAllLoading } = useQuery({
    queryKey: ['scores-all', competitionId, scoreStatusParam, events?.map((e) => e.id).join(',')],
    queryFn: async () => {
      const results = await Promise.all(
        (events ?? []).map((e) => scoreApi.getScoresByEvent(competitionId, e.id, scoreStatusParam)),
      );
      return results.flat();
    },
    enabled: isAllMode && !!events?.length,
  });

  const scores = isAllMode ? allEventScores : singleEventScores;
  const isScoresLoading = isAllMode ? isAllLoading : isSingleLoading;

  const selectedEvent = isAllMode
    ? (activeScore ? (eventMap[activeScore.eventId] ?? null) : null)
    : (events?.find((e) => e.id === selectedEventId) ?? null);

  const reviewMutation = useMutation({
    mutationFn: (payload: ReviewScoreRequest) => {
      if (!activeScore) throw new Error('판독 대상 기록이 없습니다.');
      return scoreApi.reviewScore(competitionId, activeScore.eventId, activeScore.id, payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['scores', competitionId] });
      queryClient.invalidateQueries({ queryKey: ['scores-all', competitionId] });
      queryClient.invalidateQueries({ queryKey: ['leaderboard', 'overall', competitionId] });
      setActiveScore(null);
      setReviewForm(null);
    },
  });

  const openReviewDialog = (score: CompetitionScore) => {
    setActiveScore(score);
    setReviewForm(buildReviewForm(score));
  };

  const closeReviewDialog = () => {
    setActiveScore(null);
    setReviewForm(null);
  };

  const handleSubmitReview = () => {
    if (!reviewForm) return;
    if (
      (reviewForm.status === 'ADJUSTED' || reviewForm.status === 'REJECTED') &&
      !reviewForm.reviewerNote.trim()
    ) {
      window.alert('조정 또는 거절 시 사유를 입력해주세요.');
      return;
    }
    reviewMutation.mutate(buildReviewPayload(reviewForm));
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center gap-4">
        <div className="flex flex-wrap items-center gap-1">
          {isEventsLoading ? (
            <span className="text-sm text-muted-foreground">이벤트 로딩 중...</span>
          ) : events?.length ? (
            <>
              <Button
                variant={isAllMode ? 'default' : 'outline'}
                size="sm"
                onClick={() => setSelectedEventId('ALL')}
              >
                전체
              </Button>
              {events.map((event) => (
                <Button
                  key={event.id}
                  variant={selectedEventId === event.id ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => setSelectedEventId(event.id)}
                >
                  {formatEventButtonLabel(event)}
                </Button>
              ))}
            </>
          ) : (
            <span className="text-sm text-muted-foreground">등록된 이벤트 없음</span>
          )}
        </div>

        {!!events?.length && <div className="h-5 w-px bg-border" />}

        <div className="flex flex-wrap items-center gap-1">
          {STATUS_FILTERS.map(({ value, label }) => (
            <Button
              key={value}
              variant={statusFilter === value ? 'secondary' : 'ghost'}
              size="sm"
              onClick={() => setStatusFilter(value)}
            >
              {label}
            </Button>
          ))}
        </div>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              {isAllMode && <TableHead>이벤트</TableHead>}
              <TableHead>참가자</TableHead>
              <TableHead>성별</TableHead>
              <TableHead>기록</TableHead>
              <TableHead>스케일</TableHead>
              <TableHead>영상</TableHead>
              <TableHead>상태</TableHead>
              <TableHead className="text-right">판독</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {!events?.length && !isEventsLoading ? (
              <TableRow>
                <TableCell colSpan={isAllMode ? 8 : 7} className="h-32 text-center text-muted-foreground">
                  등록된 이벤트가 없습니다.
                </TableCell>
              </TableRow>
            ) : isEventsLoading || isScoresLoading ? (
              <TableRow>
                <TableCell colSpan={isAllMode ? 8 : 7} className="h-32 text-center text-muted-foreground">
                  기록을 불러오는 중입니다.
                </TableCell>
              </TableRow>
            ) : !scores?.length ? (
              <TableRow>
                <TableCell colSpan={isAllMode ? 8 : 7} className="h-32 text-center text-muted-foreground">
                  제출된 기록이 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              scores.map((score) => {
                const registration = registrationMap[score.registrationId];
                const participantName =
                  registration?.registrationType === 'TEAM'
                    ? registration.teamName || '팀'
                    : registration?.athleteName || score.registrationId.slice(0, 8);

                const gender = registration?.gender
                  ? (GENDER_LABELS[registration.gender] ?? registration.gender)
                  : '-';

                return (
                  <TableRow key={score.id}>
                    {isAllMode && (
                      <TableCell className="text-sm text-muted-foreground">
                        {formatEventButtonLabel(eventMap[score.eventId] ?? { order: 0, name: score.eventId.slice(0, 8) } as CompetitionEvent)}
                      </TableCell>
                    )}
                    <TableCell className="font-medium">{participantName}</TableCell>
                    <TableCell>{gender}</TableCell>
                    <TableCell className="font-medium">{formatResult(score)}</TableCell>
                    <TableCell>{registration?.scaleCategory ?? '-'}</TableCell>
                    <TableCell>
                      {score.videoUrl ? (
                        <Button variant="outline" size="sm" asChild>
                          <a href={score.videoUrl} target="_blank" rel="noreferrer">
                            <Play className="mr-2 h-3.5 w-3.5" />
                            영상 보기
                          </a>
                        </Button>
                      ) : (
                        <span className="text-sm text-muted-foreground">-</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <ScoreStatusBadge status={score.status} />
                    </TableCell>
                    <TableCell className="text-right">
                      <Button size="sm" variant="outline" onClick={() => openReviewDialog(score)}>
                        판독
                      </Button>
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>

      <Dialog open={!!activeScore && !!reviewForm} onOpenChange={(open) => !open && closeReviewDialog()}>
        <DialogContent className="max-h-[90vh] max-w-2xl overflow-y-auto">
          {activeScore && reviewForm && (
            <>
              <DialogHeader>
                <DialogTitle>
                  기록 판독 -{' '}
                  {registrationMap[activeScore.registrationId]?.athleteName ??
                    registrationMap[activeScore.registrationId]?.teamName ??
                    activeScore.registrationId.slice(0, 8)}
                  {selectedEvent ? ` · ${formatEventLabel(selectedEvent)}` : ''}
                </DialogTitle>
                <DialogDescription>
                  제출 기록과 영상을 확인한 뒤 판독 결과를 저장합니다.
                </DialogDescription>
              </DialogHeader>

              <div className="rounded-lg bg-muted/60 p-4">
                <dl className="grid gap-3 text-sm sm:grid-cols-2">
                  <div>
                    <dt className="text-muted-foreground">제출된 기록</dt>
                    <dd className="mt-1 font-semibold">{formatResult(activeScore)}</dd>
                  </div>
                  <div>
                    <dt className="text-muted-foreground">현재 상태</dt>
                    <dd className="mt-1">
                      <ScoreStatusBadge status={activeScore.status} />
                    </dd>
                  </div>
                  <div>
                    <dt className="text-muted-foreground">제출자</dt>
                    <dd className="mt-1 font-medium">
                      {registrationMap[activeScore.registrationId]?.registrationType === 'TEAM'
                        ? registrationMap[activeScore.registrationId]?.teamName
                        : registrationMap[activeScore.registrationId]?.athleteName}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-muted-foreground">제출 시각</dt>
                    <dd className="mt-1 font-medium">{formatDateTime(activeScore.createdAt)}</dd>
                  </div>
                </dl>
              </div>

              <div className="space-y-2">
                <Label>YouTube 영상</Label>
                <div className="flex flex-col gap-3 rounded-lg border bg-muted/30 p-3 sm:flex-row sm:items-center sm:justify-between">
                  <div className="truncate text-sm text-muted-foreground">{activeScore.videoUrl}</div>
                  <Button variant="outline" size="sm" asChild>
                    <a href={activeScore.videoUrl} target="_blank" rel="noreferrer">
                      <ExternalLink className="mr-2 h-3.5 w-3.5" />
                      새 탭에서 보기
                    </a>
                  </Button>
                </div>
              </div>

              <div className="space-y-3">
                <Label>판독 결과</Label>
                <div className="grid gap-2 sm:grid-cols-3">
                  {(['APPROVED', 'ADJUSTED', 'REJECTED'] as ScoreStatus[]).map((status) => (
                    <Button
                      key={status}
                      type="button"
                      variant={reviewForm.status === status ? 'default' : 'outline'}
                      onClick={() => setReviewForm((prev) => (prev ? { ...prev, status } : prev))}
                    >
                      {scoreStatusLabels[status]}
                    </Button>
                  ))}
                </div>
              </div>

              {reviewForm.status === 'ADJUSTED' && (
                <div className="space-y-4 rounded-lg border p-4">
                  <div className="grid gap-4 sm:grid-cols-2">
                    <div className="space-y-2">
                      <Label>조정 기록 - 시간</Label>
                      <div className="grid grid-cols-[1fr_auto_1fr_auto] items-center gap-2">
                        <Input
                          inputMode="numeric"
                          value={reviewForm.resultTimeMinutes}
                          onChange={(event) =>
                            setReviewForm((prev) =>
                              prev ? { ...prev, resultTimeMinutes: event.target.value } : prev,
                            )
                          }
                        />
                        <span className="text-sm text-muted-foreground">분</span>
                        <Input
                          inputMode="numeric"
                          value={reviewForm.resultTimeSeconds}
                          onChange={(event) =>
                            setReviewForm((prev) =>
                              prev ? { ...prev, resultTimeSeconds: event.target.value } : prev,
                            )
                          }
                        />
                        <span className="text-sm text-muted-foreground">초</span>
                      </div>
                    </div>
                    <div className="space-y-2">
                      <Label>결과 상태</Label>
                      <Select
                        value={reviewForm.resultStatus}
                        onValueChange={(value) =>
                          setReviewForm((prev) =>
                            prev ? { ...prev, resultStatus: value as ResultStatus } : prev,
                          )
                        }
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="COMPLETED">완료</SelectItem>
                          <SelectItem value="DNF">DNF</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="space-y-2">
                      <Label>Rounds</Label>
                      <Input
                        inputMode="numeric"
                        value={reviewForm.resultRounds}
                        onChange={(event) =>
                          setReviewForm((prev) =>
                            prev ? { ...prev, resultRounds: event.target.value } : prev,
                          )
                        }
                      />
                    </div>
                    <div className="space-y-2">
                      <Label>Reps</Label>
                      <Input
                        inputMode="numeric"
                        value={reviewForm.resultReps}
                        onChange={(event) =>
                          setReviewForm((prev) =>
                            prev ? { ...prev, resultReps: event.target.value } : prev,
                          )
                        }
                      />
                    </div>
                    <div className="space-y-2">
                      <Label>Weight</Label>
                      <Input
                        value={reviewForm.resultWeight}
                        onChange={(event) =>
                          setReviewForm((prev) =>
                            prev ? { ...prev, resultWeight: event.target.value } : prev,
                          )
                        }
                      />
                    </div>
                    <div className="space-y-2">
                      <Label>기타 기록</Label>
                      <Input
                        value={reviewForm.resultCustom}
                        onChange={(event) =>
                          setReviewForm((prev) =>
                            prev ? { ...prev, resultCustom: event.target.value } : prev,
                          )
                        }
                      />
                    </div>
                  </div>
                </div>
              )}

              {(reviewForm.status === 'ADJUSTED' || reviewForm.status === 'REJECTED') && (
                <div className="space-y-2">
                  <Label>{reviewForm.status === 'ADJUSTED' ? '조정 메모' : '거절 사유'}</Label>
                  <Textarea
                    placeholder={
                      reviewForm.status === 'ADJUSTED'
                        ? '기록을 조정한 이유를 입력해주세요.'
                        : '거절 사유를 입력해주세요.'
                    }
                    value={reviewForm.reviewerNote}
                    onChange={(event) =>
                      setReviewForm((prev) =>
                        prev ? { ...prev, reviewerNote: event.target.value } : prev,
                      )
                    }
                  />
                </div>
              )}

              <DialogFooter>
                <Button variant="outline" onClick={closeReviewDialog}>
                  취소
                </Button>
                <Button onClick={handleSubmitReview} disabled={reviewMutation.isPending}>
                  {reviewMutation.isPending ? '저장 중...' : '판독 완료'}
                </Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
