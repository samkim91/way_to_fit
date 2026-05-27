# CrossFit Competition Platform — v1.0

## Problem Statement

> 구글 폼과 엑셀로 대회를 힘겹게 운영하는 주최자에게 직관적인 운영 엔진을 제공하고, 참가자와 관중(Spectator)에게는 실시간 순위와 선수 프로필을 즐길 수 있는 미디어 경험을 제공하려면 어떻게 해야 할까?

**MVP 타겟:** 로컬 100인 규모 대회. 계좌이체 + 주최자 수동 승인으로 충분한 규모.

---

## 플랫폼 구조

| 역할 | 플랫폼 | 범위 |
|---|---|---|
| Organizer (주최자/코치) | 웹 어드민 (기존 React 앱) | 대회 운영 전체 |
| Participant (참가자) | Flutter 앱 (신규, MVP 포함) | 신청, 기록 제출, 리더보드 |
| Spectator (관중) | Flutter 앱 (신규, MVP 포함) | 리더보드, 선수 프로필 |

---

## Recommended Direction

기존 `way-to-fit` 코드베이스에 `competition` 독립 도메인 추가. 기존 박스 관리 기능은 유지하되 Competition은 박스 의존 없이 설계. **이벤트 단위로 개인전/팀전을 설정**하는 구조.

```
Competition
├── Stage (QUALIFIER → FINAL, 독립 리더보드)
│   └── Event (이벤트 1, 2, 3...)
│       ├── eventType: INDIVIDUAL | TEAM
│       ├── gender: MEN | WOMEN | MIXED
│       ├── scaleCategories: [RXD, SCALED, ...]
│       ├── WodType + timeCap 등 파라미터   ← 기존 WodType 재활용
│       ├── releaseAt (null이면 대회 OPEN 시 일괄 공개, 값 있으면 순차 공개)
│       └── submissionDeadline (이벤트별 개별 마감)
│
├── Registration (대회별 참가 신청)
│   ├── Individual: userId, gender, scaleCategory
│   ├── Team: teamName + List<{userId, gender}>, scaleCategory
│   │         팀원 전원 신청 시 확정 / 이후 수정은 주최자 권한
│   │         성별 구성 검증은 주최자 육안 확인 후 승인 (v2: 강제 검증)
│   └── paymentStatus: PENDING → CONFIRMED (주최자 수동 승인)
│       계좌이체 + 주최자 수동 승인 (v2: PG 연동)
│
├── EventLineup (팀 이벤트별 출전 멤버)
│   ├── eventId + registrationId(팀) 연결
│   └── participatingMemberIds: 팀원 중 출전 멤버 선택
│       (팀 등록 멤버 전원이 아닌, 이벤트마다 다르게 구성 가능)
│
├── Score (기록 제출)
│   ├── eventId + registrationId 연결
│   ├── 기록값 + YouTube URL
│   ├── submittedBy: 팀원 누구나 제출 가능
│   └── status: SUBMITTED → UNDER_REVIEW → APPROVED | ADJUSTED | REJECTED
│
├── Leaderboard (실시간 집계)
│   ├── 이벤트별: eventType + gender + scaleCategory 조합으로 분리
│   ├── 종합 (개인): INDIVIDUAL 이벤트 순위값 합산
│   ├── 종합 (팀): TEAM 이벤트 순위값 합산
│   ├── 점수: 낮을수록 좋음 (CrossFit 표준)
│   ├── Tie-breaker: 마지막 이벤트 성적 우수자 자동 적용 + 주최자 수동 override 가능
│   └── WebSocket 실시간 — 주최자 어드민 내 프로젝션 탭 (인증 세션 한정)
│       공개 리더보드(Spectator용)는 REST API + 폴링
│
└── AthleteProfile (선수 프로필)
    ├── User 기반, OAuth 로그인 시 자동 생성
    ├── biography, profileImageUrl 등 마이페이지에서 수정
    ├── 대회 참가 이력: List<{competitionId, division, finalRank}>
    └── 이벤트별 성적 조회
```

### 기존 코드 재활용 목록

| 재활용 대상 | 위치 | 용도 |
|---|---|---|
| `User` + OAuth | `user` 도메인 | 로그인 + AthleteProfile 기반 |
| `WodType` enum | `wod/domain/enums` | Event WOD 타입 |
| `GenderCategory` | `wod/domain/enums` | 이벤트 성별 구분 |
| `ResultStatus` (COMPLETED/DNF) | `wod/domain/enums` | 기록 완주 상태 |
| `WeightUnit` (KG/LB) | `wod/domain/enums` | 무게 단위 |
| `ScaleGroup` 개념 | `wod/domain` | ScaleCategory로 재정의 |

### 핵심 설계 결정

- 한 선수가 같은 대회에 **개인 등록 1개 + 팀 등록 1개** 동시 가능 (이벤트별 eventType으로 자연 처리)
- 한 선수는 **하나의 팀에만 소속** 가능 (동일 대회 내)
- **팀 이벤트별 출전 멤버 구성 가능** — EventLineup으로 관리 (팀 전원 참가 강제 아님)
- **선수 프로필은 biography/profileImageUrl 중심으로 유지** — 별도 소속 도메인 의존 없음
- **Organizer 권한**: `CompetitionOrganizer` 별도 테이블 (`competitionId + userId`) — 대회별 권한 관리, 향후 공동 주최 확장 가능
- **프로젝션 페이지는 주최자 어드민 내 탭** → WebSocket 세션이 주최자 인원 수로 제한되어 부하 예측 가능
- **Competition Score는 기존 WodRecord와 완전 분리** — 영상 검증·조정 이력·이벤트 연계 구조가 달라 확장이 아닌 별도 도메인
- **이벤트 순서(order) 변경 불가** — 기록(Score)이 1건이라도 존재하면 변경 차단 (순위 일관성 보장)
- **DRAFT 대회는 공개 비노출** — `OPEN` 이상부터 공개 목록 노출. Organizer는 자신의 DRAFT 대회 조회 가능
- **이벤트 순차 공개** — `releaseAt`으로 자동 공개 시점 지정. `null`이면 대회 OPEN 시 일괄 공개

---

## Key Assumptions to Validate

- [ ] **수동 승인의 한계치** — 100~200명 무통장 입금 확인 + 수동 등록 처리가 구글 폼+엑셀보다 확실히 편하다고 느끼는가? → 초기 1~2개 대회 주최자 피드백으로 검증
- [ ] **채점 룰 유연성** — 국내 유명 대회 3~5개 룰북이 우리 스키마로 90% 이상 수용 가능한가? → 구현 전 시뮬레이션
- [ ] **YouTube URL UX** — 비공개 설정 실수, 링크 오류 등 없이 매끄럽게 작동하는가?
- [ ] **타이브레이커 자동 룰 충분성** — 마지막 이벤트 성적 기준이 실제 대회에서 90% 이상 수용 가능한가?
- [x] ~~종합 순위 계산 범위~~ — INDIVIDUAL 이벤트 → 개인 종합 / TEAM 이벤트 → 팀 종합으로 결정됨

---

## MVP Scope

### 주최자 (Organizer) — 웹 어드민

- 대회 생성/편집, Stage 구성 (QUALIFIER/FINAL, ONLINE/OFFLINE/HYBRID), Event 구성
- 신청 승인 (계좌이체 확인 후 PENDING → CONFIRMED)
- 기록 판독 (APPROVED / ADJUSTED / REJECTED)
- 리더보드 WebSocket 프로젝션 (어드민 내 탭)
- 본선 진출자 수동 선별 UI
- 타이브레이커 수동 override

### 참가자 (Participant) — Flutter 앱

- 개인/팀 신청 (동일 대회 중복 참가 가능, 한 팀만)
- 팀 이벤트별 EventLineup 설정
- 이벤트별 기록 제출 (YouTube URL + 기록값, 팀원 누구나)

### 관중 (Spectator) — Flutter 앱

- 메인 랜딩 대회 목록 공개
- 실시간 공개 리더보드 (REST API + 폴링)
- 선수 프로필 조회 (참가 이력, 이벤트별 성적)

---

## Not Doing (and Why)

| 항목 | 이유 |
|---|---|
| Super Admin 승인 UI | MVP는 CompetitionOrganizer 테이블 직접 제어, v2 |
| 팀 성별 구성 강제 검증 | 주최자 육안 승인으로 충분, v2 |
| 대기자 명단 | v2 결정 |
| PG 결제 연동 | MVP+3개월 결정 |
| 알림/이메일 시스템 | 주최자 수동 처리 가능 규모 |
| 복잡한 타이브레이커 자동화 | 마지막 이벤트 룰 + 수동 override로 대체 |
| 자동 본선 진출 선별 | 주최자 판단 영역 |
| 플랫폼 자체 영상 업로드 | 스토리지/트래픽 비용, YouTube 링크로 대체 |
| 관중용 WebSocket 리더보드 | 부하 이슈, 폴링으로 대체 |

---

## v2 Backlog

- Super Admin 승인 UI
- PG 결제 연동 + 자동 등록 승인
- 팀 성별 구성 강제 검증
- 대기자 명단
- 알림/이메일 (신청 완료, 승인, 기록 검토 결과)
- 복잡한 타이브레이커 자동화 옵션
- 자동 본선 진출 선별
