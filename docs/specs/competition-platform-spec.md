# Spec: CrossFit Competition Platform v1.0

## Objective

구글 폼·엑셀로 대회를 운영하는 주최자에게 직관적인 운영 엔진을 제공하고, 참가자와 관중에게는 실시간 리더보드와 선수 프로필을 즐길 수 있는 미디어 경험을 제공한다.

**MVP 타겟:** 로컬 100인 규모 대회. 계좌이체 + 주최자 수동 승인.

### 사용자 역할

| 역할 | 플랫폼 | 주요 기능 |
|---|---|---|
| Organizer (주최자) | 웹 어드민 | 대회 생성, 신청 승인, 기록 판독, 리더보드 프로젝션 |
| Participant (참가자) | Flutter 앱 | 참가 신청, 기록 제출, 리더보드 조회 |
| Spectator (관중) | Flutter 앱 | 리더보드 조회, 선수 프로필 조회 |

### 성공 기준

- 주최자가 대회 개설 → 이벤트 구성 → 신청 관리 → 기록 판독 → 리더보드 프로젝션을 어드민 내에서 완결할 수 있다
- 참가자가 앱에서 신청 → 기록 제출 → 실시간 순위 확인을 완결할 수 있다
- 오프라인 현장에서 WebSocket 리더보드가 주최자 어드민에 실시간으로 반영된다
- 관중(Spectator)용 Flutter 앱 리더보드는 REST API 기반으로 최소 30초~1분 간격의 폴링(Polling)을 통해 최신 상태를 유지한다.

---

## Tech Stack

### Backend
- Kotlin 2.3.10 + Spring Boot 3.4.3
- Architecture: Hexagonal (Domain-first, bounded context per domain)
- DB: PostgreSQL (JPA + QueryDSL)
- Auth: OAuth2 + JWT (기존 인프라 그대로)
- WebSocket: Spring WebSocket + STOMP (`spring-boot-starter-websocket` 추가)

### Admin Web (기존)
- React 18 + TypeScript + Vite
- 기존 코드베이스에 competition 기능 추가

### Participant/Spectator App (신규)
- Flutter (iOS + Android)
- REST API + WebSocket 클라이언트

---

## Commands

```bash
# Backend
./gradlew bootRun          # 실행 (local 프로파일)
./gradlew build            # 빌드
./gradlew test             # 전체 테스트
./gradlew clean build      # 클린 빌드
docker-compose up -d       # PostgreSQL 실행

# Admin Web
pnpm dev                   # 개발 서버
pnpm build                 # 프로덕션 빌드
pnpm lint                  # 린트

# Flutter App
flutter run                # 개발 실행
flutter build apk          # Android 빌드
flutter build ios          # iOS 빌드
flutter test               # 테스트
```

---

## Project Structure

### Backend — competition 도메인 추가

```
com.waytofit/
└── competition/
    ├── domain/
    │   ├── Competition.kt
    │   ├── CompetitionStage.kt
    │   ├── CompetitionEvent.kt
    │   ├── CompetitionRegistration.kt
    │   ├── CompetitionTeamMember.kt
    │   ├── EventLineup.kt
    │   ├── CompetitionScore.kt
    │   ├── AthleteProfile.kt
    └── enums/
        ├── CompetitionStatus.kt   # DRAFT|PUBLISHED|REGISTRATION_OPEN|REGISTRATION_CLOSED|IN_PROGRESS|COMPLETED
        ├── StageType.kt           # QUALIFIER|FINAL
    │       ├── StageFormat.kt         # ONLINE|OFFLINE|HYBRID
    │       ├── EventType.kt           # INDIVIDUAL|TEAM
    │       ├── RegistrationType.kt    # INDIVIDUAL|TEAM
    │       ├── PaymentStatus.kt       # PENDING|CONFIRMED|REJECTED
    │       └── ScoreStatus.kt         # SUBMITTED|UNDER_REVIEW|APPROVED|ADJUSTED|REJECTED
    ├── application/
    │   ├── service/
    │   │   ├── CompetitionService.kt
    │   │   ├── RegistrationService.kt
    │   │   ├── ScoreService.kt
    │   │   └── LeaderboardService.kt
    │   └── port/
    │       ├── in/                    # UseCase 인터페이스
    │       └── out/                   # Repository 인터페이스
    └── adapter/
        ├── in/
        │   ├── web/                   # REST Controllers + DTOs
        │   └── websocket/             # WebSocket Handler (STOMP)
        └── out/persistence/           # JPA Entities + Repositories
```

### Flutter App 구조

```
way-to-fit-app/
└── lib/
    ├── core/
    │   ├── api/           # Dio 클라이언트, interceptor
    │   ├── auth/          # OAuth + JWT 토큰 관리
    │   └── websocket/     # STOMP 클라이언트
    └── features/
        ├── competition/   # 대회 목록, 상세
        ├── registration/  # 참가 신청
        ├── score/         # 기록 제출
        ├── leaderboard/   # 리더보드 (실시간)
        └── profile/       # 선수 프로필
```

---

## Domain Model

### Competition (대회)

```kotlin
data class Competition(
    val id: UUID? = null,
    val organizerId: UUID,
    val name: String,
    val description: String? = null,
    val bannerImageUrl: String? = null,
    val status: CompetitionStatus,
    val startAt: Instant,                // 대회 시작일시
    val endAt: Instant,                  // 대회 종료일시
    val registrationStartAt: Instant,    // 신청 시작 시점 (UTC)
    val registrationEndAt: Instant,      // 신청 마감 시점 (UTC)
    val bankInfo: BankInfo? = null,
    val audit: AuditInfo = AuditInfo.empty(),
)

data class BankInfo(
    val bankName: String,
    val accountNumber: String,
    val accountHolder: String,
    val entryFee: Int,
)
```

### CompetitionStage (예선/본선)

```kotlin
data class CompetitionStage(
    val id: UUID? = null,
    val competitionId: UUID,
    val type: StageType,         // QUALIFIER | FINAL
    val format: StageFormat,     // ONLINE | OFFLINE | HYBRID
    val name: String,            // 주최자 커스텀 이름 ("예선", "본선" 등)
    val startAt: Instant,
    val endAt: Instant,
    val audit: AuditInfo = AuditInfo.empty(),
)
```

### CompetitionEvent (이벤트)

```kotlin
data class CompetitionEvent(
    val id: UUID? = null,
    val stageId: UUID,
    val competitionId: UUID,
    val order: Int,
    val name: String,                     // "Event 1", "21.1" 등
    val eventType: EventType,             // INDIVIDUAL | TEAM
    val gender: GenderCategory,           // MEN | WOMEN | MIXED
    val scaleCategories: List<String>,    // ["RXD", "SCALED"]
    val wodType: WodType,                 // 기존 enum 재활용
    val timeCap: Int? = null,
    val amrapDuration: Int? = null,
    val emomDuration: Int? = null,
    val weightUnit: WeightUnit? = null,   // 기존 enum 재활용
    val description: String? = null,
    val releaseAt: Instant? = null,       // null이면 대회 PUBLISHED/OPEN 시점에 일괄 공개
    val submissionDeadline: Instant,      // 온라인 제출 마감 시점 (UTC)
    val audit: AuditInfo = AuditInfo.empty(),
)
```

**이벤트 공개 조건 (Participant/Spectator 조회 시 적용)**

```
competition.status IN (PUBLISHED, REGISTRATION_OPEN, REGISTRATION_CLOSED, IN_PROGRESS, COMPLETED)
AND (event.releaseAt IS NULL OR event.releaseAt <= now())
```

- `releaseAt = null` → 대회 PUBLISHED/OPEN 시점에 일괄 공개
- `releaseAt = 특정 시점` → 해당 시각에 자동 공개 (순차 공개)
- Organizer는 `releaseAt` 무관하게 항상 전체 이벤트 조회 가능

---

### CompetitionRegistration (참가 신청)

```kotlin
data class CompetitionRegistration(
    val id: UUID? = null,
    val competitionId: UUID,
    val registrationType: RegistrationType,  // INDIVIDUAL | TEAM
    val userId: UUID,                        // 개인: 본인 / 팀: 대표자
    val scaleCategory: String,
    val gender: GenderCategory,
    val teamName: String? = null,
    val paymentStatus: PaymentStatus,
    val paymentNote: String? = null,         // 입금자명, 메모
    val audit: AuditInfo = AuditInfo.empty(),
)

// 팀 등록 시 팀원 목록 (별도 테이블)
data class CompetitionTeamMember(
    val id: UUID? = null,
    val registrationId: UUID,
    val userId: UUID,
    val gender: GenderCategory,
    val role: TeamMemberRole,   // LEADER | MEMBER (기존 enum 재활용)
)
```

**제약:**
- 한 선수는 동일 대회에서 INDIVIDUAL 등록 1개 + TEAM 등록 1개까지만 허용
- 한 선수는 하나의 팀에만 소속 가능 (동일 대회 내)

### EventLineup (이벤트별 출전 멤버)

```kotlin
data class EventLineup(
    val id: UUID? = null,
    val eventId: UUID,
    val registrationId: UUID,         // TEAM 등록 ID
    val participatingMemberIds: List<UUID>,  // 출전 멤버 userId (팀원 중 선택)
    val audit: AuditInfo = AuditInfo.empty(),
)
```

**제약:** `participatingMemberIds`는 해당 Registration의 팀원(CompetitionTeamMember)에 포함된 userId여야 함.

### CompetitionScore (기록)

```kotlin
data class CompetitionScore(
    val id: UUID? = null,
    val eventId: UUID,
    val registrationId: UUID,
    val competitionId: UUID,
    val submittedBy: UUID,
    val resultTimeSeconds: Int? = null,
    val resultRounds: Int? = null,
    val resultReps: Int? = null,
    val resultWeight: BigDecimal? = null,
    val resultCustom: String? = null,
    val resultStatus: ResultStatus,          // 기존 enum 재활용 (COMPLETED | DNF)
    val videoUrl: String? = null,            // YouTube URL
    val status: ScoreStatus,
    val adjustedBy: UUID? = null,
    val adjustmentNote: String? = null,
    val audit: AuditInfo = AuditInfo.empty(),
)
```

### Leaderboard (계산 기준)

- **이벤트별 리더보드**: `eventId + gender + scaleCategory` 조합별 순위
- **종합 리더보드**: Registration 단위로 INDIVIDUAL 이벤트 순위합 / TEAM 이벤트 순위합 분리 집계
- **점수**: 순위값 합산, 낮을수록 좋음 (CrossFit 표준)
- **Tie-breaker**: 마지막 이벤트 성적 우수자 자동 적용 + 주최자 수동 override

### CompetitionStatus 공개 범위

| Status | 공개 목록 노출 | 상세 조회 | 비고 |
|---|---|---|---|
| DRAFT | ✗ (Organizer만) | ✗ | 작성 중 |
| PUBLISHED | ✓ | ✓ | 정보 공개 (신청 전) |
| REGISTRATION_OPEN | ✓ | ✓ | 신청 가능 |
| REGISTRATION_CLOSED | ✓ | ✓ | 신청 마감, 대회 준비 중 |
| IN_PROGRESS | ✓ | ✓ | 대회 진행 중 |
| COMPLETED | ✓ | ✓ | 대회 종료 |

- 공개 목록 API(`GET /api/competitions`)는 `status != DRAFT` 조건으로 필터링
- Organizer는 자신이 주최하는 DRAFT 대회도 조회 가능

---

### AthleteProfile (선수 프로필)

```kotlin
data class AthleteProfile(
    val id: UUID? = null,
    val userId: UUID,
    val boxId: UUID? = null,          // nullable FK → Box
    val biography: String? = null,
    val profileImageUrl: String? = null,
    val audit: AuditInfo = AuditInfo.empty(),
)
```

OAuth 로그인 시 자동 생성 (기본값만). 마이페이지에서 boxId, biography 등 수정.

---

## API Endpoints

### Competition

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/api/competitions` | 대회 목록 (공개, 페이징) | 없음 |
| POST | `/api/competitions` | 대회 생성 | Organizer |
| GET | `/api/competitions/{id}` | 대회 상세 | 없음 |
| PATCH | `/api/competitions/{id}` | 대회 수정 | Organizer |

### Stage

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/competitions/{id}/stages` | 스테이지 생성 | Organizer |
| PATCH | `/api/competitions/{id}/stages/{stageId}` | 스테이지 수정 | Organizer |
| POST | `/api/competitions/{id}/stages/{stageId}/finalists` | 본선 진출자 선별 | Organizer |

### Event

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/competitions/{id}/stages/{stageId}/events` | 이벤트 생성 | Organizer |
| PATCH | `/api/competitions/{id}/events/{eventId}` | 이벤트 수정 | Organizer |
| DELETE | `/api/competitions/{id}/events/{eventId}` | 이벤트 삭제 | Organizer |

### Registration

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/competitions/{id}/registrations` | 참가 신청 | User |
| GET | `/api/competitions/{id}/registrations` | 신청 목록 | Organizer |
| GET | `/api/competitions/{id}/registrations/me` | 내 신청 조회 | User |
| PATCH | `/api/competitions/{id}/registrations/{regId}/payment` | 결제 상태 변경 | Organizer |

### EventLineup

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| PUT | `/api/competitions/{id}/events/{eventId}/lineups/{regId}` | 출전 멤버 설정 | User (팀 대표) |

### Score

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/api/competitions/{id}/events/{eventId}/scores` | 기록 제출 | User |
| GET | `/api/competitions/{id}/events/{eventId}/scores` | 기록 목록 | Organizer |
| PATCH | `/api/competitions/{id}/scores/{scoreId}/review` | 기록 판독 | Organizer |

### Leaderboard

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/api/competitions/{id}/stages/{stageId}/leaderboard` | 종합 리더보드 | 없음 |
| GET | `/api/competitions/{id}/events/{eventId}/leaderboard` | 이벤트별 리더보드 | 없음 |

쿼리 파라미터: `?gender=MEN&scaleCategory=RXD&eventType=INDIVIDUAL`

### WebSocket (STOMP)

| Destination | 설명 | 인증 |
|---|---|---|
| `/topic/competitions/{id}/leaderboard` | 종합 리더보드 실시간 | JWT |
| `/topic/competitions/{id}/events/{eventId}/leaderboard` | 이벤트 리더보드 실시간 | JWT |

### AthleteProfile

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/api/athletes/{userId}` | 선수 프로필 조회 | 없음 |
| PATCH | `/api/athletes/me` | 내 프로필 수정 | User |
| GET | `/api/athletes/{userId}/competitions` | 대회 참가 이력 | 없음 |

---

## Code Style

기존 헥사고날 패턴 및 프로젝트 관례를 따름.

- **날짜/시간:** 
    - 정확한 시점(Audit, 마감, 결제, 토큰 등) -> `Instant` 사용
    - 대회 일시(startAt, endAt), 이벤트 일시(releaseAt, submissionDeadline) -> `Instant` 사용
    - 달력상의 생일 등 순수 날짜 -> `LocalDate` 사용
- **Package:** `com.waytofit.competition` 하위에 도메인별로 구성

---

## Testing Strategy

- **단위 테스트:** 도메인 로직 (비즈니스 규칙, 계산 로직) → `domain/` 대상
- **통합 테스트:** UseCase + Repository 레이어 → `@SpringBootTest` + Testcontainers PostgreSQL
- **컨트롤러 테스트:** `@WebMvcTest` + MockMvc
- 테스트 위치: `src/test/kotlin/com/waytofit/competition/`
- 커버리지 목표: 도메인 레이어 80% 이상

---

## Boundaries

**Always:**
- 도메인 레이어는 Spring/JPA 등 외부 프레임워크에 의존하지 않음
- 프로젝트 관례에 맞는 날짜/시간 타입 사용 (LocalDate, LocalTime, Instant 혼용)
- API 응답은 기존 `ApiResponse` 공통 포맷 사용
- 기록 score는 기존 `WodRecord`와 별도 도메인으로 분리 유지
- DRAFT 대회는 공개 목록 API(`GET /api/competitions`)에서 제외
- 이벤트 조회 시 Participant/Spectator는 `releaseAt` 공개 조건 필터링 적용

**Ask first:**
- DB 스키마 변경 (기존 테이블 영향 시)
- 신규 의존성 추가 (build.gradle.kts)
- WebSocket 인프라 설정 변경
- Organizer 권한 부여 로직 변경 (현재 DB 직접 제어)

**Never:**
- 기존 `WodRecord`, `Wod`, `Box` 도메인 직접 수정
- CompetitionScore와 WodRecord 혼용
- 인증 없는 WebSocket 구독 허용

---

## Success Criteria

- [ ] 주최자가 대회 생성 → Stage → Event 순서로 설정 완료 가능
- [ ] 참가자가 개인/팀 신청 후 주최자가 결제 확인 후 승인 처리 가능
- [ ] 팀 이벤트별 EventLineup 설정 가능
- [ ] 참가자/팀이 YouTube URL + 기록값 제출 가능
- [ ] 주최자가 APPROVED / ADJUSTED / REJECTED 처리 가능
- [ ] 이벤트별 리더보드 + 종합 리더보드 (개인/팀 분리) 정상 집계
- [ ] WebSocket 리더보드가 기록 판독 즉시 반영
- [ ] Flutter 앱에서 위 참가자 기능 전체 동작
- [ ] AthleteProfile에서 대회 참가 이력 + 이벤트 성적 조회 가능
- [ ] 관중(Spectator)용 Flutter 앱 리더보드는 REST API 기반으로 최소 30초~1분 간격의 폴링(Polling)을 통해 최신 상태를 유지한다.

---

## Architecture Decisions (확정)

| 항목 | 결정 | 이유 |
|---|---|---|
| Organizer 권한 | `CompetitionOrganizer` 별도 테이블 (`competitionId + userId`) | 대회별 권한 관리, 향후 공동 주최 확장 가능 |
| 리더보드 캐싱 | 매 요청마다 실시간 집계 (캐시 없음) | 100인 규모에서 충분, 단순함 우선 |
| Flutter OAuth | 웹과 동일한 provider (Google, Kakao 등) | 기존 백엔드 OAuth 인프라 재활용 |
| 이벤트 순서 변경 | 기록(Score)이 1건이라도 존재하면 `order` 변경 불가 (400 반환) | 순위 일관성 보장 |
