import type {
  CompetitionLifecycle,
  CompetitionVisibility,
  EventType,
  GenderCategory,
  PaymentStatus,
  ScoreStatus,
  StageFormat,
  StageType,
  WodType,
} from './types';

const COMPETITION_LIFECYCLE_VALUES = [
  'OPEN',
  'REGISTRATION_OPEN',
  'REGISTRATION_CLOSED',
  'IN_PROGRESS',
  'COMPLETED',
] as const satisfies readonly CompetitionLifecycle[];

const COMPETITION_VISIBILITY_VALUES = ['PUBLIC', 'PRIVATE'] as const satisfies readonly CompetitionVisibility[];

export const competitionLifecycleLabels: Record<CompetitionLifecycle, string> = {
  OPEN: '오픈',
  REGISTRATION_OPEN: '신청중',
  REGISTRATION_CLOSED: '신청 마감',
  IN_PROGRESS: '진행중',
  COMPLETED: '종료',
};

export const competitionLifecycleOptions = COMPETITION_LIFECYCLE_VALUES.map((value) => ({
  value,
  label: competitionLifecycleLabels[value],
}));

export const competitionVisibilityLabels: Record<CompetitionVisibility, string> = {
  PUBLIC: '공개',
  PRIVATE: '비공개',
};

export const competitionVisibilityOptions = COMPETITION_VISIBILITY_VALUES.map((value) => ({
  value,
  label: competitionVisibilityLabels[value],
}));

export function normalizeCompetitionVisibility(
  value: string | null | undefined,
): CompetitionVisibility {
  return COMPETITION_VISIBILITY_VALUES.includes(value as CompetitionVisibility)
    ? (value as CompetitionVisibility)
    : 'PRIVATE';
}

export const paymentStatusLabels: Record<PaymentStatus, string> = {
  PENDING: '대기중',
  CONFIRMED: '승인됨',
  REJECTED: '거절됨',
};

export const scoreStatusLabels: Record<ScoreStatus, string> = {
  SUBMITTED: '제출됨',
  UNDER_REVIEW: '검토 중',
  APPROVED: '승인',
  ADJUSTED: '조정됨',
  REJECTED: '거절됨',
};

export const stageTypeLabels: Record<StageType, string> = {
  QUALIFIER: '예선',
  FINAL: '본선',
};

export const stageFormatLabels: Record<StageFormat, string> = {
  ONLINE: '온라인',
  OFFLINE: '오프라인',
};

export const eventTypeLabels: Record<EventType, string> = {
  INDIVIDUAL: '개인전',
  TEAM: '팀전',
};

export const genderCategoryLabels: Record<GenderCategory, string> = {
  MEN: '남자',
  WOMEN: '여자',
  MIXED: '혼성',
};

export const wodTypeLabels: Record<WodType, string> = {
  FOR_TIME: 'For Time Of',
  AMRAP: 'AMRAP',
  EMOM: 'EMOM',
  MAX_WEIGHT: '최대 중량',
  CUSTOM: '기타',
};
