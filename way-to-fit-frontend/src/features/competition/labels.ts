import type {
  CompetitionStatus,
  EventType,
  GenderCategory,
  PaymentStatus,
  ScoreStatus,
  StageFormat,
  StageType,
  WodType,
} from './types';

const COMPETITION_STATUS_VALUES = [
  'DRAFT',
  'PUBLISHED',
  'REGISTRATION_OPEN',
  'REGISTRATION_CLOSED',
  'IN_PROGRESS',
  'COMPLETED',
] as const satisfies readonly CompetitionStatus[];

export const competitionStatusLabels: Record<CompetitionStatus, string> = {
  DRAFT: '작성 중',
  PUBLISHED: '공개됨',
  REGISTRATION_OPEN: '신청중',
  REGISTRATION_CLOSED: '신청 마감',
  IN_PROGRESS: '진행중',
  COMPLETED: '종료',
};

export const competitionStatusOptions = COMPETITION_STATUS_VALUES.map((value) => ({
  value,
  label: competitionStatusLabels[value],
}));

export function normalizeCompetitionStatus(value: string | null | undefined): CompetitionStatus {
  return COMPETITION_STATUS_VALUES.includes(value as CompetitionStatus)
    ? (value as CompetitionStatus)
    : 'DRAFT';
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
  OTHER: '기타',
};

export const stageFormatLabels: Record<StageFormat, string> = {
  ONLINE: '온라인',
  OFFLINE: '오프라인',
  HYBRID: '온/오프라인',
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
  FOR_TIME: '시간 기록',
  AMRAP: 'AMRAP',
  EMOM: 'EMOM',
  MAX_WEIGHT: '최대 중량',
  CUSTOM: '기타',
};
