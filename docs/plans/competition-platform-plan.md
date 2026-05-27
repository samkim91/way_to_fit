# Implementation Plan: CrossFit Competition Platform v1.0

## Overview

기존 `way-to-fit` 백엔드에 `competition` 독립 도메인을 추가하고, 웹 어드민(React)에 주최자 기능을 붙이며, Flutter 앱으로 참가자/관중 경험을 제공한다. 총 25개 태스크, 7개 페이즈.

## Dependency Graph

```
Phase 0: 인프라 (WebSocket 설정, CompetitionOrganizer 권한)
    │
Phase 1: 대회 구조 (Competition → Stage → Event)
    │
    ├── Phase 2: 참가 신청 (Registration → TeamMember → EventLineup)
    │       │
    │       └── Phase 3: 기록 (Score 제출 → 판독)
    │               │
    │               └── Phase 4: 리더보드 (이벤트별 → 종합 → WebSocket)
    │
    ├── Phase 5: 선수 프로필 (AthleteProfile ← Phase 1에서 병렬 가능)
    │
    └── Phase 8: 성능 최적화 (N+1 최적화, 캐싱, 스냅샷)
    
Phase 6: 웹 어드민 UI ← Phase 1~4 API 완료 후 (Phase 5와 병렬)
Phase 7: Flutter 앱    ← Phase 1~5 API 완료 후 (Phase 6와 병렬)
```

## Naming Convention

태스크 ID: `[플랫폼]-[페이즈]-[번호]`
- `B` = Backend, `W` = Web Admin, `F` = Flutter App

---

## Phase 0: 인프라 기반

### Task B-0-1: WebSocket 인프라 설정 [S]

**Description:** STOMP 기반 WebSocket을 Spring에 추가. JWT 핸드셰이크 인터셉터로 인증 세션만 구독 허용.

**Acceptance criteria:**
- [ ] `spring-boot-starter-websocket` 의존성 추가
- [ ] STOMP 엔드포인트 `/ws` 등록
- [ ] JWT 검증 핸드셰이크 인터셉터 적용 (인증 실패 시 연결 거부)
- [ ] `./gradlew build` 성공

**Verification:** `./gradlew build`

**Dependencies:** None

**Files:**
- `build.gradle.kts`
- `global/config/WebSocketConfig.kt`
- `global/security/jwt/WebSocketJwtHandshakeInterceptor.kt`

---

### Task B-0-2: CompetitionOrganizer 전체 슬라이스 [M]

**Description:** 대회별 주최자 권한 테이블. MVP에서는 DB 직접 삽입으로 권한 부여. 권한 체크 유틸리티 포함.

**Acceptance criteria:**
- [ ] `competition_organizer` 테이블 생성 (`competition_id`, `user_id`, PK)
- [ ] `isOrganizer(competitionId, userId): Boolean` 체크 메서드 동작
- [ ] 권한 없는 사용자가 주최자 API 호출 시 403 반환

**Verification:** `./gradlew test --tests "*.CompetitionOrganizerTest"`

**Dependencies:** B-0-1

**Files:**
- `competition/domain/CompetitionOrganizer.kt`
- `competition/application/port/out/CompetitionOrganizerRepository.kt`
- `competition/adapter/out/persistence/entity/CompetitionOrganizerEntity.kt`
- `competition/adapter/out/persistence/repository/CompetitionOrganizerJpaRepository.kt`
- `competition/adapter/out/persistence/CompetitionOrganizerPersistenceAdapter.kt`

---

### Checkpoint 0
- [ ] `./gradlew build` 성공
- [ ] WebSocket 연결 테스트 (ws://localhost:8080/ws)
- [ ] CompetitionOrganizer 테이블 DB에 존재 확인

---

## Phase 1: 대회 구조

### Task B-1-1: Competition 전체 슬라이스 [M]

**Description:** 대회 CRUD. `status`, `bankInfo`, 신청 기간 포함. 대회 목록(공개)과 상세(공개) API.

**Acceptance criteria:**
- [ ] `POST /api/competitions` — 주최자만 생성 가능, 생성 시 status=DRAFT
- [ ] `GET /api/competitions` — 공개 목록, DRAFT 제외, 페이징
- [ ] `GET /api/competitions/{id}` — 공개 상세
- [ ] `PATCH /api/competitions/{id}` — 주최자만 수정
- [ ] `competition` 테이블 마이그레이션 정상 동작

**Verification:** `./gradlew test --tests "*.CompetitionControllerTest"`

**Dependencies:** B-0-2

**Files:**
- `competition/domain/Competition.kt`, `BankInfo.kt`
- `competition/domain/enums/CompetitionStatus.kt`
- `competition/application/port/in/{Create,Update,Get}CompetitionUseCase.kt`
- `competition/application/port/out/CompetitionRepository.kt`
- `competition/application/service/CompetitionService.kt`
- `competition/adapter/out/persistence/entity/CompetitionEntity.kt`
- `competition/adapter/out/persistence/repository/CompetitionJpaRepository.kt`
- `competition/adapter/out/persistence/CompetitionPersistenceAdapter.kt`
- `competition/adapter/in/web/CompetitionController.kt`
- `competition/adapter/in/web/dto/request/CreateCompetitionRequest.kt`
- `competition/adapter/in/web/dto/response/CompetitionResponse.kt`

---

### Task B-1-2: CompetitionStage 전체 슬라이스 [M]

**Description:** 예선/본선 Stage CRUD. 본선 진출자 수동 선별 API 포함.

**Acceptance criteria:**
- [ ] `POST /api/competitions/{id}/stages` — Stage 생성 (QUALIFIER/FINAL, ONLINE/OFFLINE/HYBRID)
- [ ] `PATCH /api/competitions/{id}/stages/{stageId}` — Stage 수정
- [ ] `POST /api/competitions/{id}/stages/{stageId}/finalists` — registrationId 목록으로 본선 진출자 저장

**Verification:** `./gradlew test --tests "*.CompetitionStageTest"`

**Dependencies:** B-1-1

**Files:**
- `competition/domain/CompetitionStage.kt`
- `competition/domain/enums/{StageType,StageFormat}.kt`
- `competition/application/port/in/{Create,Update}StageUseCase.kt`, `SelectFinalistsUseCase.kt`
- `competition/application/port/out/CompetitionStageRepository.kt`
- `competition/application/service/CompetitionService.kt` (확장)
- `competition/adapter/out/persistence/entity/CompetitionStageEntity.kt`
- `competition/adapter/out/persistence/repository/CompetitionStageJpaRepository.kt`
- `competition/adapter/in/web/CompetitionStageController.kt`
- `competition/adapter/in/web/dto/request/CreateStageRequest.kt`

---

### Task B-1-3: CompetitionEvent 전체 슬라이스 [M]

**Description:** 이벤트 CRUD. WodType 재활용. 기록 존재 시 order 변경 불가 제약.

**Acceptance criteria:**
- [ ] `POST /api/competitions/{id}/stages/{stageId}/events` — 이벤트 생성
- [ ] `PATCH /api/competitions/{id}/events/{eventId}` — 수정 (Score 존재 시 order 변경 → 400)
- [ ] `DELETE /api/competitions/{id}/events/{eventId}` — Score 없을 때만 삭제 가능
- [ ] `scaleCategories` 는 String 리스트로 자유 입력 ("RXD", "SCALED", "MASTERS 55+" 등)
- [ ] `CreateEventRequest`에 `releaseAt: Instant?` 포함 (null 허용)
- [ ] 참가자/관중용 이벤트 목록 조회 시 `competition.status IN (PUBLISHED, REGISTRATION_OPEN, REGISTRATION_CLOSED, IN_PROGRESS, COMPLETED) AND (releaseAt IS NULL OR releaseAt <= now())` 조건 적용
- [ ] Organizer는 `releaseAt` 무관하게 전체 이벤트 조회 가능

**Verification:** `./gradlew test --tests "*.CompetitionEventTest"`

**Dependencies:** B-1-2

**Files:**
- `competition/domain/CompetitionEvent.kt`
- `competition/domain/enums/EventType.kt`
- `competition/application/port/in/{Create,Update,Delete}EventUseCase.kt`
- `competition/application/port/out/CompetitionEventRepository.kt`
- `competition/application/service/CompetitionEventService.kt`
- `competition/adapter/out/persistence/entity/CompetitionEventEntity.kt`
- `competition/adapter/out/persistence/repository/CompetitionEventJpaRepository.kt`
- `competition/adapter/in/web/CompetitionEventController.kt`
- `competition/adapter/in/web/dto/request/CreateEventRequest.kt`
- `competition/adapter/in/web/dto/response/CompetitionEventResponse.kt`

---

### Checkpoint 1
- [ ] `./gradlew test` 전체 통과
- [ ] Competition → Stage → Event 생성 플로우 Swagger로 수동 검증
- [ ] 기존 wod/box/schedule 도메인 테스트 영향 없음 확인

---

## Phase 2: 참가 신청

### Task B-2-1: 개인 신청 전체 슬라이스 [M]

**Description:** 개인 참가 신청. 동일 대회 INDIVIDUAL 등록 중복 방지, 신청 기간 검증.

**Acceptance criteria:**
- [ ] `POST /api/competitions/{id}/registrations` — type=INDIVIDUAL 신청
- [ ] 동일 대회 INDIVIDUAL 등록이 이미 있으면 409
- [ ] 신청 기간 외 요청 시 400
- [ ] `GET /api/competitions/{id}/registrations/me` — 내 신청 조회
- [ ] status=PENDING으로 생성

**Verification:** `./gradlew test --tests "*.RegistrationServiceTest"`

**Dependencies:** B-1-1

**Files:**
- `competition/domain/CompetitionRegistration.kt`
- `competition/domain/enums/{RegistrationType,PaymentStatus}.kt`
- `competition/application/port/in/RegisterIndividualUseCase.kt`
- `competition/application/port/out/CompetitionRegistrationRepository.kt`
- `competition/application/service/RegistrationService.kt`
- `competition/adapter/out/persistence/entity/CompetitionRegistrationEntity.kt`
- `competition/adapter/out/persistence/repository/CompetitionRegistrationJpaRepository.kt`
- `competition/adapter/in/web/CompetitionRegistrationController.kt`
- `competition/adapter/in/web/dto/request/RegisterIndividualRequest.kt`

---

### Task B-2-2: 팀 신청 전체 슬라이스 [M]

**Description:** 팀 신청 + 팀원 등록. 한 선수 한 팀 소속 제약 검증.

**Acceptance criteria:**
- [ ] `POST /api/competitions/{id}/registrations` — type=TEAM 신청 (팀원 목록 포함)
- [ ] 팀원 중 이미 다른 팀에 소속된 선수 있으면 409
- [ ] `CompetitionTeamMember` 테이블에 팀원 일괄 저장
- [ ] 팀 대표자(LEADER)가 신청자로 자동 설정

**Verification:** `./gradlew test --tests "*.TeamRegistrationTest"`

**Dependencies:** B-2-1

**Files:**
- `competition/domain/CompetitionTeamMember.kt`
- `competition/application/port/in/RegisterTeamUseCase.kt`
- `competition/application/port/out/CompetitionTeamMemberRepository.kt`
- `competition/application/service/RegistrationService.kt` (확장)
- `competition/adapter/out/persistence/entity/CompetitionTeamMemberEntity.kt`
- `competition/adapter/out/persistence/repository/CompetitionTeamMemberJpaRepository.kt`
- `competition/adapter/in/web/dto/request/RegisterTeamRequest.kt`

---

### Task B-2-3: EventLineup 전체 슬라이스 [S]

**Description:** 팀 이벤트별 출전 멤버 설정. 팀원에 포함된 userId만 허용.

**Acceptance criteria:**
- [ ] `PUT /api/competitions/{id}/events/{eventId}/lineups/{regId}` — 출전 멤버 설정
- [ ] `participatingMemberIds` 가 팀원 목록에 없는 userId 포함 시 400
- [ ] 팀 대표자(LEADER)만 설정 가능
- [ ] 중복 호출 시 덮어쓰기 (upsert)

**Verification:** `./gradlew test --tests "*.EventLineupTest"`

**Dependencies:** B-2-2, B-1-3

**Files:**
- `competition/domain/EventLineup.kt`
- `competition/application/port/in/SetEventLineupUseCase.kt`
- `competition/application/port/out/EventLineupRepository.kt`
- `competition/application/service/RegistrationService.kt` (확장)
- `competition/adapter/out/persistence/entity/EventLineupEntity.kt`
- `competition/adapter/in/web/dto/request/SetLineupRequest.kt`

---

### Task B-2-4: 신청 관리 API (Organizer) [S]

**Description:** 주최자용 신청 목록 조회 + 결제 상태 승인/거절.

**Acceptance criteria:**
- [ ] `GET /api/competitions/{id}/registrations` — 신청 목록 (페이징, paymentStatus 필터)
- [ ] `PATCH /api/competitions/{id}/registrations/{regId}/payment` — CONFIRMED / REJECTED 변경
- [ ] 주최자 권한 없으면 403

**Verification:** `./gradlew test --tests "*.RegistrationOrganizerTest"`

**Dependencies:** B-2-1, B-0-2

**Files:**
- `competition/application/port/in/{GetRegistrations,UpdatePaymentStatus}UseCase.kt`
- `competition/application/service/RegistrationService.kt` (확장)
- `competition/adapter/in/web/dto/response/RegistrationResponse.kt`

---

### Checkpoint 2
- [ ] 개인 신청 → 주최자 승인 플로우 수동 검증
- [ ] 팀 신청 → 중복 팀 소속 제약 동작 확인
- [ ] EventLineup 설정 후 조회 확인

---

## Phase 3: 기록 제출 & 판독

### Task B-3-1: CompetitionScore 제출 전체 슬라이스 [M]

**Description:** 이벤트별 기록 제출. submissionDeadline 검증, YouTube URL 기본 형식 검증. 팀원 누구나 제출 가능.

**Acceptance criteria:**
- [ ] `POST /api/competitions/{id}/events/{eventId}/scores` — 기록 제출
- [ ] `submissionDeadline` 지난 이벤트 제출 시 400
- [ ] 등록(CONFIRMED)된 참가자만 제출 가능, 아니면 403
- [ ] 팀전 이벤트: 팀 소속 멤버면 누구나 제출 가능
- [ ] `videoUrl` 이 youtube.com / youtu.be 형식이 아니면 400
- [ ] status=SUBMITTED로 생성

**Verification:** `./gradlew test --tests "*.CompetitionScoreServiceTest"`

**Dependencies:** B-2-4, B-1-3

**Files:**
- `competition/domain/CompetitionScore.kt`
- `competition/domain/enums/ScoreStatus.kt`
- `competition/application/port/in/SubmitScoreUseCase.kt`
- `competition/application/port/out/CompetitionScoreRepository.kt`
- `competition/application/service/ScoreService.kt`
- `competition/adapter/out/persistence/entity/CompetitionScoreEntity.kt`
- `competition/adapter/out/persistence/repository/CompetitionScoreJpaRepository.kt`
- `competition/adapter/in/web/CompetitionScoreController.kt`
- `competition/adapter/in/web/dto/request/SubmitScoreRequest.kt`

---

### Task B-3-2: 기록 판독 API (Organizer) [S]

**Description:** 주최자가 기록 상태 변경. ADJUSTED 시 기록값 수정. 판독 완료 시 WebSocket 갱신 이벤트 발행.

**Acceptance criteria:**
- [ ] `GET /api/competitions/{id}/events/{eventId}/scores` — 기록 목록 (status 필터)
- [ ] `PATCH /api/competitions/{id}/scores/{scoreId}/review` — APPROVED / ADJUSTED / REJECTED
- [ ] ADJUSTED 시 `resultTimeSeconds` 등 기록값 수정 가능, `adjustedBy` + `adjustmentNote` 저장
- [ ] 판독 완료 시 Spring ApplicationEvent 발행 (Phase 4에서 WebSocket 갱신에 활용)

**Verification:** `./gradlew test --tests "*.ScoreReviewTest"`

**Dependencies:** B-3-1

**Files:**
- `competition/application/port/in/ReviewScoreUseCase.kt`
- `competition/application/service/ScoreService.kt` (확장)
- `competition/adapter/in/web/dto/request/ReviewScoreRequest.kt`
- `competition/adapter/in/web/dto/response/ScoreResponse.kt`

---

### Checkpoint 3
- [ ] 기록 제출 → 판독 전체 플로우 수동 검증
- [ ] deadline 초과 제출 거부 확인
- [ ] ADJUSTED 시 기록값 변경 확인

---

## Phase 4: 리더보드

### Task B-4-1: 이벤트별 리더보드 집계 + API [M]

**Description:** 이벤트별 순위 계산. WodType별 정렬 기준(FOR_TIME: 시간 오름차순, AMRAP: 라운드+렙 내림차순 등). gender + scaleCategory 필터.

**Acceptance criteria:**
- [ ] `GET /api/competitions/{id}/events/{eventId}/leaderboard` — 이벤트 리더보드
- [ ] `?gender=MEN&scaleCategory=RXD` 필터 동작
- [ ] 스케일 카테고리 필터 미지정 시 전체 카테고리를 반환하되, 순위(rank)는 같은 (gender, scaleCategory) 그룹 내에서만 독립 계산
- [ ] APPROVED / ADJUSTED 기록만 순위 집계 (SUBMITTED, REJECTED 제외)
- [ ] DNF는 완주자 하위에 배치
- [ ] 동점 처리: 동일 순위 부여 (dense rank 방식)

**Verification:** `./gradlew test --tests "*.LeaderboardServiceTest"`

**Dependencies:** B-3-2

**Files:**
- `competition/application/port/in/GetEventLeaderboardUseCase.kt`
- `competition/application/service/LeaderboardService.kt`
- `competition/adapter/in/web/LeaderboardController.kt`
- `competition/adapter/in/web/dto/response/EventLeaderboardResponse.kt`

---

### Task B-4-2: 종합 리더보드 집계 + API [M]

**Description:** 이벤트별 순위값 합산. INDIVIDUAL 이벤트 → 개인 종합 / TEAM 이벤트 → 팀 종합. Tie-breaker: 마지막 이벤트 성적 + 주최자 수동 override.

**Acceptance criteria:**
- [ ] `GET /api/competitions/{id}/stages/{stageId}/leaderboard` — 종합 리더보드
- [ ] `?eventType=INDIVIDUAL` or `TEAM` 파라미터로 분리 조회
- [ ] `?gender=MEN&scaleCategory=RXD` 파라미터로 특정 카테고리만 필터링 가능
- [ ] 카테고리 미지정 시 전체 카테고리 표시, 종합 순위는 scaleCategory 그룹 내에서만 독립 계산
- [ ] 순위 = 이벤트별 순위값 합산 (낮을수록 상위, 같은 scaleCategory 내에서만 비교)
- [ ] 기권 선수의 페널티 순위 = 동일 scaleCategory 이벤트 참가자 수 + 1
- [ ] Tie-breaker: 마지막 이벤트 순위 비교
- [ ] `PATCH /api/competitions/{id}/stages/{stageId}/leaderboard/{registrationId}/rank` — 주최자 수동 순위 override

**Verification:** `./gradlew test --tests "*.OverallLeaderboardTest"`

**Dependencies:** B-4-1

**Files:**
- `competition/application/port/in/GetOverallLeaderboardUseCase.kt`, `OverrideRankUseCase.kt`
- `competition/application/service/LeaderboardService.kt` (확장)
- `competition/adapter/in/web/dto/response/OverallLeaderboardResponse.kt`

---

### Task B-4-3: WebSocket 실시간 리더보드 갱신 [M]

**Description:** 기록 판독 완료 시 연결된 주최자 세션에 리더보드 갱신 메시지 전송.

**Acceptance criteria:**
- [ ] `SUBSCRIBE /topic/competitions/{id}/leaderboard` 구독 가능 (JWT 인증 필수)
- [ ] 기록 판독(APPROVED/ADJUSTED/REJECTED) 시 해당 토픽으로 갱신된 리더보드 전송
- [ ] `SUBSCRIBE /topic/competitions/{id}/events/{eventId}/leaderboard` 이벤트별 구독도 동작

**Verification:** WebSocket 클라이언트(Postman/wscat)로 구독 후 기록 판독 시 메시지 수신 확인

**Dependencies:** B-4-2, B-0-1

**Files:**
- `competition/adapter/in/websocket/LeaderboardWebSocketHandler.kt`
- `competition/application/service/LeaderboardService.kt` (ApplicationEvent 리스너 추가)

---

### Checkpoint 4
- [ ] 이벤트별 + 종합 리더보드 집계 정확도 단위 테스트
- [ ] WebSocket 구독 → 판독 → 실시간 갱신 수동 검증
- [ ] `./gradlew test` 전체 통과

---

## Phase 5: 선수 프로필

### Task B-5-1: AthleteProfile 전체 슬라이스 [M]

**Description:** OAuth 로그인 시 AthleteProfile 자동 생성. 마이페이지 수정 API. 공개 프로필 조회.

**Acceptance criteria:**
- [ ] OAuth 로그인 성공 시 AthleteProfile 자동 생성 (이미 있으면 skip)
- [ ] `PATCH /api/athletes/me` — biography, profileImageUrl 수정
- [ ] `GET /api/athletes/{userId}` — 공개 프로필 조회 (인증 없음)

**Verification:** `./gradlew test --tests "*.AthleteProfileTest"`

**Dependencies:** OAuth 로그인

**Files:**
- `competition/domain/AthleteProfile.kt`
- `competition/application/port/in/{UpdateAthleteProfile,GetAthleteProfile}UseCase.kt`
- `competition/application/port/out/AthleteProfileRepository.kt`
- `competition/application/service/AthleteProfileService.kt`
- `competition/adapter/out/persistence/entity/AthleteProfileEntity.kt`
- `competition/adapter/in/web/AthleteProfileController.kt`
- `user/adapter/out/...OAuth2LoginSuccessHandler.kt` (자동 생성 훅 추가)

---

### Task B-5-2: 대회 참가 이력 API [S]

**Description:** 선수 공개 프로필에서 대회 참가 이력 + 이벤트별 성적 조회.

**Acceptance criteria:**
- [ ] `GET /api/athletes/{userId}/competitions` — 참가 대회 목록 (종료된 대회, 최신순)
- [ ] 각 대회 내 이벤트별 기록 + 순위 포함
- [ ] 인증 없이 조회 가능

**Verification:** `./gradlew test --tests "*.AthleteHistoryTest"`

**Dependencies:** B-5-1, B-4-2

**Files:**
- `competition/application/port/in/GetAthleteCompetitionHistoryUseCase.kt`
- `competition/application/service/AthleteProfileService.kt` (확장)
- `competition/adapter/in/web/dto/response/AthleteCompetitionHistoryResponse.kt`

---

### Checkpoint 5: Backend 완료
- [ ] `./gradlew test` 전체 통과
- [ ] `./gradlew build` 성공
- [ ] Swagger UI에서 전체 API 엔드포인트 확인
- [ ] 전체 플로우 E2E 수동 검증 (대회 생성 → 신청 → 기록 → 리더보드 → 프로필)

---

## Phase 6: 웹 어드민 UI (React)

### Task W-6-1: 대회 목록 + 생성 페이지 [M]

**Description:** 주최자가 대회를 생성하고 목록을 확인하는 페이지.

**Acceptance criteria:**
- [ ] 대회 목록 페이지 (제목, 상태, 신청 기간, 참가자 수)
- [ ] 대회 생성 폼 (제목, 설명, 신청 기간, 계좌 정보)
- [ ] 생성 성공 후 상세 페이지로 이동

**Verification:** 브라우저에서 대회 생성 → 목록 확인

**Dependencies:** B-1-1

**Files:**
- `src/pages/competition/CompetitionListPage.tsx`
- `src/pages/competition/CompetitionCreatePage.tsx`
- `src/features/competition/api.ts`
- `src/features/competition/types.ts`

---

### Task W-6-2: 대회 상세 + Stage/Event 구성 UI [M]

**Description:** Stage 추가, Event 구성(WodType, 마감일, gender, scaleCategory 등) UI.

**Acceptance criteria:**
- [ ] Stage 생성/편집 (타입, 포맷, 기간)
- [ ] Event 생성/편집 (이름, eventType, gender, scaleCategories, WodType 파라미터, 마감일)
- [ ] Event 삭제 (기록 없을 때만)
- [ ] 본선 진출자 선별 UI (예선 리더보드에서 체크박스 선택)

**Verification:** 브라우저에서 Stage → Event 구성 플로우 완결

**Dependencies:** W-6-1, B-1-2, B-1-3

**Files:**
- `src/pages/competition/CompetitionDetailPage.tsx`
- `src/features/competition/StageSection.tsx`
- `src/features/competition/EventSection.tsx`

---

### Task W-6-3: 신청 관리 + 결제 승인 UI [M]

**Description:** 주최자가 신청 목록을 보고 계좌이체 확인 후 승인/거절 처리.

**Acceptance criteria:**
- [ ] 신청 목록 테이블 (이름, 타입, scaleCategory, 결제 상태, 신청일)
- [ ] paymentStatus 필터 (PENDING / CONFIRMED / REJECTED)
- [ ] 선택 행 일괄 CONFIRMED / REJECTED 처리
- [ ] 팀 신청 시 팀원 목록 펼치기

**Verification:** 브라우저에서 신청 목록 조회 → 승인 처리 확인

**Dependencies:** W-6-2, B-2-4

**Files:**
- `src/pages/competition/RegistrationManagePage.tsx`
- `src/features/competition/RegistrationTable.tsx`

---

### Task W-6-4: 기록 판독 UI [M]

**Description:** 이벤트별 제출 기록 목록. YouTube 영상 링크 클릭 → 새 탭. 기록 판독(승인/조정/거절).

**Acceptance criteria:**
- [ ] 이벤트 선택 탭 + 기록 목록 (참가자, 기록값, 영상 링크, 상태)
- [ ] SUBMITTED / UNDER_REVIEW / APPROVED / ADJUSTED / REJECTED 상태 필터
- [ ] 기록 클릭 → 판독 모달 (상태 변경 + 기록값 수정 + 메모)
- [ ] ADJUSTED 시 수정된 기록값 테이블에 즉시 반영

**Verification:** 브라우저에서 기록 제출 후 판독 플로우 확인

**Dependencies:** W-6-3, B-3-2

**Files:**
- `src/pages/competition/ScoreReviewPage.tsx`
- `src/features/competition/ScoreReviewModal.tsx`

---

### Task W-6-5: 리더보드 프로젝션 UI (WebSocket) [M]

**Description:** 오프라인 현장 프로젝션용 리더보드. WebSocket 실시간 갱신. 전체화면 지원.

**Acceptance criteria:**
- [ ] 이벤트별 / 종합 리더보드 탭
- [ ] gender + scaleCategory + eventType 필터
- [ ] WebSocket 연결 → 판독 완료 즉시 순위 갱신 (애니메이션)
- [ ] 전체화면 버튼 (F11 대신 Fullscreen API)
- [ ] 연결 끊김 시 자동 재연결

**Verification:** 브라우저에서 WebSocket 구독 후 기록 판독 시 실시간 갱신 확인

**Dependencies:** W-6-4, B-4-3

**Files:**
- `src/pages/competition/LeaderboardProjectionPage.tsx`
- `src/features/competition/LeaderboardTable.tsx`
- `src/hooks/useCompetitionWebSocket.ts`

---

### Checkpoint 6: 웹 어드민 완료
- [ ] 주최자 전체 플로우 브라우저 검증 (대회 생성 → 이벤트 → 신청 승인 → 판독 → 프로젝션)
- [ ] `pnpm build` 성공

---

## Phase 7: Flutter 앱

### Task F-7-1: Flutter 프로젝트 기반 구조 [M]

**Description:** Flutter 프로젝트 생성. Dio HTTP 클라이언트, 라우팅(go_router), 상태관리(Riverpod) 설정.

**Acceptance criteria:**
- [ ] `flutter create way-to-fit-app` 완료
- [ ] `pubspec.yaml`: dio, go_router, flutter_riverpod, stomp_dart_client 의존성 추가
- [ ] Dio baseUrl + JWT 인터셉터 설정
- [ ] `flutter run` 앱 실행 확인

**Verification:** `flutter build apk` 성공

**Dependencies:** None (API는 완성된 백엔드 사용)

**Files:**
- `way-to-fit-app/pubspec.yaml`
- `way-to-fit-app/lib/core/api/dio_client.dart`
- `way-to-fit-app/lib/core/api/auth_interceptor.dart`
- `way-to-fit-app/lib/app/router.dart`

---

### Task F-7-2: OAuth 로그인 + JWT 관리 [M]

**Description:** Google/Kakao OAuth 로그인. JWT Access/Refresh Token 저장 + 자동 갱신.

**Acceptance criteria:**
- [ ] Google OAuth 로그인 → 백엔드 JWT 발급 → SecureStorage 저장
- [ ] AT 만료 시 RT로 자동 갱신 (Dio 인터셉터)
- [ ] 로그아웃 시 토큰 삭제 + 로그인 화면 이동

**Verification:** 로그인 → 토큰 저장 → 만료 시뮬레이션 후 자동 갱신 확인

**Dependencies:** F-7-1

**Files:**
- `lib/core/auth/auth_service.dart`
- `lib/core/auth/token_storage.dart`
- `lib/features/auth/login_screen.dart`

---

### Task F-7-3: 대회 목록/상세 화면 [S]

**Acceptance criteria:**
- [ ] 홈 화면: 대회 목록 (카드형, 상태 배지, 신청 기간)
- [ ] 대회 상세: Stage 목록, Event 목록 (eventType, gender, scaleCategory, WodType)
- [ ] 신청 버튼 (신청 기간 내, CONFIRMED 아닌 경우 활성)

**Verification:** 앱에서 대회 목록 → 상세 화면 진입 확인

**Dependencies:** F-7-2

**Files:**
- `lib/features/competition/competition_list_screen.dart`
- `lib/features/competition/competition_detail_screen.dart`
- `lib/features/competition/competition_repository.dart`

---

### Task F-7-4: 참가 신청 화면 (개인/팀) [M]

**Acceptance criteria:**
- [ ] 개인 신청: gender, scaleCategory 선택 → 제출
- [ ] 팀 신청: 팀명, scaleCategory + 팀원 추가 (userId 검색 or 이메일 입력)
- [ ] 신청 완료 후 "입금 대기" 안내 메시지
- [ ] EventLineup 설정 화면 (팀 신청 완료 후, 이벤트별 출전 멤버 선택)

**Verification:** 앱에서 개인/팀 신청 → 백엔드 DB 확인

**Dependencies:** F-7-3

**Files:**
- `lib/features/registration/individual_register_screen.dart`
- `lib/features/registration/team_register_screen.dart`
- `lib/features/registration/event_lineup_screen.dart`

---

### Task F-7-5: 기록 제출 화면 [M]

**Acceptance criteria:**
- [ ] 이벤트 목록 → 이벤트 선택 → 기록 제출 폼
- [ ] WodType별 입력 필드 (FOR_TIME: 시간, AMRAP: 라운드+렙, MAX_WEIGHT: 무게)
- [ ] YouTube URL 입력 + 형식 검증
- [ ] 제출 후 상태 배지 표시 (SUBMITTED → 판독 대기 안내)
- [ ] submissionDeadline 지난 이벤트는 비활성

**Verification:** 앱에서 기록 제출 → 백엔드 DB 확인

**Dependencies:** F-7-4

**Files:**
- `lib/features/score/score_submit_screen.dart`
- `lib/features/score/score_form_widget.dart`

---

### Task F-7-6: 리더보드 화면 (폴링) [M]

**Acceptance criteria:**
- [ ] 이벤트별 리더보드 탭 + 종합 리더보드 탭
- [ ] gender / scaleCategory / eventType 필터
- [ ] 30초 폴링으로 자동 갱신
- [ ] 내 순위 하이라이트

**Verification:** 앱 리더보드 → 기록 판독 후 30초 내 갱신 확인

**Dependencies:** F-7-5

**Files:**
- `lib/features/leaderboard/leaderboard_screen.dart`
- `lib/features/leaderboard/leaderboard_repository.dart`

---

### Task F-7-7: 선수 프로필 화면 [S]

**Acceptance criteria:**
- [ ] 내 프로필 (biography, profileImageUrl 수정)
- [ ] 타인 프로필 조회 (대회 이력, 이벤트별 성적)
- [ ] 리더보드 선수명 탭 → 해당 프로필 화면 이동

**Verification:** 앱에서 프로필 수정 → 타인 프로필 조회 확인

**Dependencies:** F-7-6

**Files:**
- `lib/features/profile/my_profile_screen.dart`
- `lib/features/profile/athlete_profile_screen.dart`

---

### Checkpoint 7: 전체 완료
- [ ] Flutter 앱 전체 플로우 검증 (로그인 → 신청 → 기록 제출 → 리더보드)
- [ ] 웹 어드민 + Flutter 앱 연동 E2E 검증
- [ ] `flutter build apk` / `flutter build ios` 성공
- [ ] 스펙의 Success Criteria 9개 항목 전부 충족 확인

---

## Phase 8: 성능 최적화

### Task B-8-1: N+1 쿼리 최적화 (QueryDSL) [M]

**Description:** 리더보드 조회 시 발생하는 Score → Registration → User → TeamMember N+1 문제를 QueryDSL `fetchJoin`으로 해결.

**Acceptance criteria:**
- [ ] `CompetitionScoreCustomRepository` 구현
- [ ] 단일 쿼리로 리더보드에 필요한 모든 연관 엔티티 조회
- [ ] `LeaderboardService`가 최적화된 쿼리를 사용하도록 리팩토링

---

### Task B-8-2: 리더보드 캐싱 및 비동기 업데이트 [M]

**Description:** Spring Cache 적용 및 WebSocket 전송 로직 비동기화.

**Acceptance criteria:**
- [ ] `@Cacheable`을 이용한 리더보드 결과 캐싱 (이벤트별/종합)
- [ ] `@Async`를 이용한 리더보드 계산 및 WebSocket 전송 (트랜잭션 분리)
- [ ] 기록 판독 시 관련 캐시 무효화(`clear`) 처리

---

### Task B-8-3: 선수 이력 스냅샷 시스템 [M]

**Description:** 대회 종료 시 최종 결과를 스냅샷 테이블에 저장하여 프로필 조회 성능 개선.

**Acceptance criteria:**
- [ ] `CompetitionHistorySnapshot` 엔티티 및 저장소 구현
- [ ] `CompetitionCompletedEvent` 발행 및 스냅샷 생성 리스너 구현
- [ ] `AthleteProfileService`가 실시간 계산 대신 스냅샷을 조회하도록 수정

---

## Risks and Mitigations

| 리스크 | 영향 | 대응 |
|---|---|---|
| 리더보드 집계 로직 복잡도 (WodType별 정렬 기준) | 높음 | B-4-1에서 단위 테스트 케이스 먼저 작성 후 구현 (TDD) |
| WebSocket JWT 핸드셰이크 미구현 시 보안 구멍 | 높음 | B-0-1을 Phase 0 첫 태스크로 선행 |
| Flutter OAuth 플로우 (딥링크, 리다이렉트) | 중간 | F-7-2를 별도 스파이크로 빠르게 검증 |
| AthleteProfile 자동 생성 훅이 기존 OAuth 플로우에 영향 | 중간 | 기존 OAuth 테스트 먼저 실행해 회귀 확인 |
| 이벤트 삭제 제약 (기록 존재 시) 누락 | 낮음 | B-1-3 acceptance criteria에 명시, 테스트 필수 |

## Parallelization Opportunities

Phase 5(AthleteProfile)은 Phase 1 완료 후 Phase 2~4와 병렬 진행 가능.
Phase 6(웹 어드민)과 Phase 7(Flutter 앱)은 백엔드 완료 후 병렬 진행 가능.
