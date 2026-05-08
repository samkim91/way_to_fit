import apiClient from '@/api/client';
import type {
  Competition,
  CompetitionStage,
  CompetitionEvent,
  Registration,
  CompetitionScore,
  EventLeaderboardResponse,
  OverallLeaderboardResponse,
  CreateCompetitionRequest,
  UpdateCompetitionRequest,
  CreateStageRequest,
  UpdateStageRequest,
  CreateEventRequest,
  UpdateEventRequest,
  UpdatePaymentStatusRequest,
  ReviewScoreRequest,
  Page,
  PaymentStatus,
  ScoreStatus,
  RegistrationType,
  CompetitionStatus,
} from './types';

// ─── Shared helper ────────────────────────────────────────────────────────────

const unwrap = <T>(response: { data: { data: T } }) => response.data.data;

// ─── Competition ──────────────────────────────────────────────────────────────

export const competitionApi = {
  /** 내 대회 목록 (DRAFT 포함) */
  getMyCompetitions: (params?: {
    statuses?: CompetitionStatus[];
    page?: number;
    size?: number;
  }) =>
    apiClient
      .get<{ data: Page<Competition> }>('/competitions/my', { params })
      .then(unwrap),

  /** 공개 대회 목록 (DRAFT 제외, 페이징) */
  getCompetitions: (page = 0, size = 20) =>
    apiClient
      .get<{ data: Page<Competition> }>('/competitions', { params: { page, size } })
      .then(unwrap),

  /** 대회 상세 */
  getCompetition: (id: string) =>
    apiClient.get<{ data: Competition }>(`/competitions/${id}`).then(unwrap),

  /** 대회 생성 */
  createCompetition: (data: CreateCompetitionRequest) =>
    apiClient.post<{ data: Competition }>('/competitions', data).then(unwrap),

  /** 대회 수정 */
  updateCompetition: (id: string, data: UpdateCompetitionRequest) =>
    apiClient.patch<{ data: Competition }>(`/competitions/${id}`, data).then(unwrap),
};

// ─── Stage ────────────────────────────────────────────────────────────────────

export const stageApi = {
  /** 대회별 스테이지 목록 */
  getStages: (competitionId: string) =>
    apiClient
      .get<{ data: CompetitionStage[] }>(`/competitions/${competitionId}/stages`)
      .then(unwrap),

  /** 스테이지 생성 */
  createStage: (competitionId: string, data: CreateStageRequest) =>
    apiClient
      .post<{ data: CompetitionStage }>(`/competitions/${competitionId}/stages`, data)
      .then(unwrap),

  /** 스테이지 수정 */
  updateStage: (competitionId: string, stageId: string, data: UpdateStageRequest) =>
    apiClient
      .patch<{ data: CompetitionStage }>(
        `/competitions/${competitionId}/stages/${stageId}`,
        data,
      )
      .then(unwrap),

  /** 본선 진출자 저장 */
  selectFinalists: (competitionId: string, stageId: string, registrationIds: string[]) =>
    apiClient
      .post(`/competitions/${competitionId}/stages/${stageId}/finalists`, { registrationIds })
      .then(unwrap),

  /** 본선 진출자 ID 목록 조회 */
  getFinalists: (competitionId: string, stageId: string) =>
    apiClient
      .get<{ data: string[] }>(`/competitions/${competitionId}/stages/${stageId}/finalists`)
      .then(unwrap),
};

// ─── Event ────────────────────────────────────────────────────────────────────

export const eventApi = {
  /** 스테이지별 이벤트 목록 */
  getEvents: (competitionId: string, stageId: string) =>
    apiClient
      .get<{ data: CompetitionEvent[] }>(
        `/competitions/${competitionId}/stages/${stageId}/events`,
      )
      .then(unwrap),

  /** 이벤트 생성 */
  createEvent: (competitionId: string, stageId: string, data: CreateEventRequest) =>
    apiClient
      .post<{ data: CompetitionEvent }>(
        `/competitions/${competitionId}/stages/${stageId}/events`,
        data,
      )
      .then(unwrap),

  /** 이벤트 수정 */
  updateEvent: (
    competitionId: string,
    stageId: string,
    eventId: string,
    data: UpdateEventRequest,
  ) =>
    apiClient
      .patch<{ data: CompetitionEvent }>(
        `/competitions/${competitionId}/stages/${stageId}/events/${eventId}`,
        data,
      )
      .then(unwrap),

  /** 이벤트 삭제 */
  deleteEvent: (competitionId: string, stageId: string, eventId: string) =>
    apiClient
      .delete(`/competitions/${competitionId}/stages/${stageId}/events/${eventId}`)
      .then(unwrap),
};

// ─── Registration ─────────────────────────────────────────────────────────────

export const registrationApi = {
  /** [주최자] 신청 목록 (필터 + 페이징) */
  getRegistrations: (
    competitionId: string,
    params?: { paymentStatus?: PaymentStatus; page?: number; size?: number },
  ) =>
    apiClient
      .get<{ data: Page<Registration> }>(`/competitions/${competitionId}/registrations`, {
        params: { size: 100, ...params },
      })
      .then(unwrap),

  /** [주최자] 결제 상태 변경 */
  updatePaymentStatus: (
    competitionId: string,
    registrationId: string,
    data: UpdatePaymentStatusRequest,
  ) =>
    apiClient
      .patch<{ data: Registration }>(
        `/competitions/${competitionId}/registrations/${registrationId}/payment`,
        data,
      )
      .then(unwrap),
};

// ─── Score ────────────────────────────────────────────────────────────────────

export const scoreApi = {
  /** [주최자] 이벤트별 기록 목록 */
  getScoresByEvent: (competitionId: string, eventId: string, status?: ScoreStatus) =>
    apiClient
      .get<{ data: CompetitionScore[] }>(
        `/competitions/${competitionId}/events/${eventId}/scores/all`,
        { params: status ? { status } : undefined },
      )
      .then(unwrap),

  /** [주최자] 기록 판독 */
  reviewScore: (competitionId: string, eventId: string, scoreId: string, data: ReviewScoreRequest) =>
    apiClient
      .patch<{ data: CompetitionScore }>(
        `/competitions/${competitionId}/events/${eventId}/scores/${scoreId}/review`,
        data,
      )
      .then(unwrap),
};

// ─── Leaderboard ──────────────────────────────────────────────────────────────

export const leaderboardApi = {
  /** 이벤트별 리더보드 */
  getEventLeaderboard: (
    competitionId: string,
    eventId: string,
    params?: { gender?: string; scaleCategory?: string },
  ) =>
    apiClient
      .get<{ data: EventLeaderboardResponse }>(
        `/competitions/${competitionId}/events/${eventId}/leaderboard`,
        { params },
      )
      .then(unwrap),

  /** 스테이지별 종합 리더보드 */
  getOverallLeaderboard: (
    competitionId: string,
    stageId: string,
    params?: { registrationType?: RegistrationType; gender?: string; scaleCategory?: string },
  ) =>
    apiClient
      .get<{ data: OverallLeaderboardResponse }>(
        `/competitions/${competitionId}/stages/${stageId}/leaderboard`,
        { params },
      )
      .then(unwrap),

  /** [주최자] 순위 수동 조정 */
  overrideRank: (competitionId: string, registrationId: string, manualRank: number | null) =>
    apiClient
      .patch(`/competitions/${competitionId}/leaderboard/${registrationId}/rank`, {
        manualRank,
      })
      .then(unwrap),
};
