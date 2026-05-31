// ─── Enums ────────────────────────────────────────────────────────────────────

export type CompetitionLifecycle =
  | 'OPEN'
  | 'REGISTRATION_OPEN'
  | 'REGISTRATION_CLOSED'
  | 'IN_PROGRESS'
  | 'COMPLETED';

export type CompetitionVisibility = 'PUBLIC' | 'PRIVATE';

export type StageType = 'QUALIFIER' | 'FINAL';
export type StageFormat = 'ONLINE' | 'OFFLINE';

export type EventType = 'INDIVIDUAL' | 'TEAM';
export type GenderCategory = 'MEN' | 'WOMEN' | 'MIXED';
export type WodType = 'FOR_TIME' | 'AMRAP' | 'EMOM' | 'MAX_WEIGHT' | 'CUSTOM';
export type WeightUnit = 'KG' | 'LB';

export type RegistrationType = 'INDIVIDUAL' | 'TEAM';
export type PaymentStatus = 'PENDING' | 'CONFIRMED' | 'REJECTED';

export type ScoreStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'ADJUSTED' | 'REJECTED';
export type ResultStatus = 'COMPLETED' | 'DNF';

// ─── Competition ──────────────────────────────────────────────────────────────

export interface Competition {
  id: string;
  name: string;
  description: string;
  startAt: string;
  endAt: string;
  registrationStartAt: string;
  registrationEndAt: string;
  visibility: CompetitionVisibility;
  lifecycle: CompetitionLifecycle;
  bankName: string;
  accountNumber: string;
  accountHolder: string;
  entryFee: number;
  bannerImageUrl: string | null;
  createdAt: string | null;
  scaleCategories: string[];
}

export interface CreateCompetitionRequest {
  name: string;
  description: string;
  startAt: string;
  endAt: string;
  registrationStartAt: string;
  registrationEndAt: string;
  bankName: string;
  accountNumber: string;
  accountHolder: string;
  entryFee: number;
  bannerImageUrl?: string | null;
  scaleCategories: string[];
}

export interface UpdateCompetitionRequest {
  name?: string;
  description?: string;
  startAt?: string;
  endAt?: string;
  registrationStartAt?: string;
  registrationEndAt?: string;
  visibility?: CompetitionVisibility;
  bankName?: string;
  accountNumber?: string;
  accountHolder?: string;
  entryFee?: number;
  bannerImageUrl?: string | null;
  scaleCategories?: string[];
}

// ─── Stage ────────────────────────────────────────────────────────────────────

export interface CompetitionStage {
  id: string;
  competitionId: string;
  name: string;
  stageType: StageType;
  stageFormat: StageFormat;
  startAt: string;
  endAt: string;
}

export interface CreateStageRequest {
  name: string;
  stageType: StageType;
  stageFormat: StageFormat;
  startAt: string;
  endAt: string;
}

export interface UpdateStageRequest {
  name?: string;
  stageType?: StageType;
  stageFormat?: StageFormat;
  startAt?: string;
  endAt?: string;
}

// ─── Event ────────────────────────────────────────────────────────────────────

export interface CompetitionEvent {
  id: string;
  stageId: string;
  competitionId: string;
  name: string;
  description: string;
  eventType: EventType;
  wodType: WodType;
  order: number;
  scaleCategories: string[];
  submissionDeadline: string;
  gender: GenderCategory;
  releaseAt: string | null;
  timeCap: number | null;
  amrapDuration: number | null;
  emomDuration: number | null;
  weightUnit: WeightUnit | null;
}

export interface CreateEventRequest {
  name: string;
  description: string;
  eventType: EventType;
  wodType: WodType;
  order: number;
  scaleCategories: string[];
  submissionDeadline: string;
  gender: GenderCategory;
  releaseAt?: string | null;
  timeCap?: number | null;
  amrapDuration?: number | null;
  emomDuration?: number | null;
  weightUnit?: WeightUnit | null;
}

export interface UpdateEventRequest {
  name?: string;
  description?: string;
  eventType?: EventType;
  wodType?: WodType;
  order?: number;
  scaleCategories?: string[];
  submissionDeadline?: string;
  gender?: GenderCategory;
  releaseAt?: string | null;
  timeCap?: number | null;
  amrapDuration?: number | null;
  emomDuration?: number | null;
  weightUnit?: WeightUnit | null;
}

// ─── Registration ─────────────────────────────────────────────────────────────

export interface TeamMemberResponse {
  userId: string;
  gender: string; // Gender enum from user domain
}

export interface Registration {
  id: string;
  competitionId: string;
  userId: string;
  athleteName: string | null;
  registrationType: RegistrationType;
  teamName: string | null;
  scaleCategory: string;
  paymentStatus: PaymentStatus;
  gender: string;
  paymentNote: string | null;
  members: TeamMemberResponse[] | null;
  createdAt: string | null;
}

export interface UpdatePaymentStatusRequest {
  paymentStatus: PaymentStatus;
}

// ─── Score ────────────────────────────────────────────────────────────────────

export interface CompetitionScore {
  id: string;
  eventId: string;
  registrationId: string;
  videoUrl: string;
  status: ScoreStatus;
  reviewerNote: string | null;
  resultTimeSeconds: number | null;
  resultRounds: number | null;
  resultReps: number | null;
  resultWeight: string | null;
  resultCustom: string | null;
  resultStatus: ResultStatus;
  createdAt: string | null;
}

export interface ReviewScoreRequest {
  status: ScoreStatus;
  reviewerNote?: string;
  resultTimeSeconds?: number | null;
  resultRounds?: number | null;
  resultReps?: number | null;
  resultWeight?: string | null;
  resultCustom?: string | null;
  resultStatus?: ResultStatus | null;
}

// ─── Leaderboard ──────────────────────────────────────────────────────────────

export interface LeaderboardEntry {
  rank: number;
  registrationId: string;
  participantName: string;
  scaleCategory: string;
  resultTimeSeconds: number | null;
  resultRounds: number | null;
  resultReps: number | null;
  resultWeight: string | null;
  resultCustom: string | null;
  resultStatus: ResultStatus | null;
  videoUrl: string;
  memberIds: string[];
}

export interface EventLeaderboardResponse {
  eventId: string;
  entries: LeaderboardEntry[];
}

export interface OverallLeaderboardEntry {
  rank: number;
  registrationId: string;
  participantName: string;
  scaleCategory: string;
  totalPoints: number;
  eventRanks: Record<string, number>;
  manualRank: number | null;
  memberIds: string[];
}

export interface OverallLeaderboardResponse {
  stageId: string;
  entries: OverallLeaderboardEntry[];
}

// ─── Pagination ───────────────────────────────────────────────────────────────

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
