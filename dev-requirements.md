# Way to Fit — 개발 요구사항 (회원 앱 대응)

> 작성일: 2026-04-25  
> 대상: 백엔드 개발자, 코치 웹 프론트엔드 개발자  
> 배경: 회원용 Flutter 앱 출시에 필요한 서버/관리 기능 목록

---

## 목차

1. [백엔드 신규 개발](#1-백엔드-신규-개발)
2. [코치 웹 신규 개발](#2-코치-웹-신규-개발)

---

## 1. 백엔드 신규 개발

### 1.1 회원용 WOD API

> 기존 `/api/v1/admin/...` 경로는 코치 전용이다.  
> 회원 앱은 별도 `/api/v1/boxes/{boxId}/...` 경로로 제공.  
> **공통 조건**: `published_at <= NOW()` 인 WOD만 반환 (draft/scheduled 노출 금지).

#### 신규 엔드포인트

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/v1/boxes/{boxId}/wods` | WOD 목록 (날짜 범위) | ACTIVE 회원 |
| GET | `/api/v1/boxes/{boxId}/wods/{wodId}` | WOD 상세 | ACTIVE 회원 |
| GET | `/api/v1/boxes/{boxId}/wods/{wodId}/leaderboard` | 리더보드 | ACTIVE 회원 |

#### `GET /api/v1/boxes/{boxId}/wods` Query Params

| 파라미터 | 설명 |
|---|---|
| `from` | 시작 날짜 (ISO 8601 date) |
| `to` | 종료 날짜 |

#### 응답 형식

기존 Admin WOD API와 동일한 구조이나, 다음 필드 제거:
- `template_id` (회원에게 불필요)
- `coach_id`

공개 상태 필드는 제거하고, 항상 `published` 상태만 반환.

---

### 1.2 회원용 WOD 기록 API

> 회원은 **본인 기록만** 입력/수정/삭제 가능. 타인 기록 조작 불가.

#### 신규 엔드포인트

| Method | Path | 설명 |
|---|---|---|
| POST | `/api/v1/boxes/{boxId}/wods/{wodId}/records` | 개인 기록 입력 |
| PATCH | `/api/v1/boxes/{boxId}/wods/{wodId}/records/{recordId}` | 개인 기록 수정 |
| DELETE | `/api/v1/boxes/{boxId}/wods/{wodId}/records/{recordId}` | 개인 기록 삭제 |
| POST | `/api/v1/boxes/{boxId}/wods/{wodId}/teams` | 팀 기록 입력 |
| PATCH | `/api/v1/boxes/{boxId}/wods/{wodId}/teams/{teamId}` | 팀 기록 수정 |
| DELETE | `/api/v1/boxes/{boxId}/wods/{wodId}/teams/{teamId}` | 팀 기록 삭제 |

#### 권한 제약

- `POST records`: `member_id`는 요청 주체 자신의 ID만 허용 (코치처럼 타인 지정 불가)
- `PATCH / DELETE records`: 본인 기록(`member_id = 요청자`)만 수정/삭제 가능
- `POST teams`: 팀원 목록에 반드시 요청자 본인 포함 필수
- `PATCH / DELETE teams`: 팀원 중 요청자가 포함된 팀만 조작 가능
- 기존 Admin API 로직(서버 검증, 팀명 자동 생성, gender_category 계산 등) 재사용

#### PR 달성 시 FCM 발송

기록 저장 완료 후, PR(`is_pr = true`)이면 FCM 발송:

```json
{
  "title": "🏆 새 PR 달성!",
  "body": "Fran 8:30 — 이전 기록 9:15",
  "data": {
    "type": "PR_ACHIEVED",
    "wodId": "uuid",
    "boxId": "uuid"
  }
}
```

---

### 1.3 회원용 내 기록 조회 API

| Method | Path | 설명 |
|---|---|---|
| GET | `/api/v1/boxes/{boxId}/members/me/records` | 내 전체 기록 (타임라인) |
| GET | `/api/v1/boxes/{boxId}/members/me/benchmark` | 내 Named WOD 벤치마크 이력 |
| GET | `/api/v1/boxes/{boxId}/members/me/1rm` | 내 1RM 기록 목록 |

#### `GET .../me/records` Query Params

| 파라미터 | 설명 |
|---|---|
| `from` / `to` | 날짜 범위 (선택) |
| `page` / `size` | 페이지네이션 |

---

### 1.4 회원용 수업 인스턴스 API

| Method | Path | 설명 |
|---|---|---|
| GET | `/api/v1/boxes/{boxId}/class-instances` | 주간 수업 목록 |

#### `GET .../class-instances` Query Params

| 파라미터 | 설명 |
|---|---|
| `from` | 시작 날짜 |
| `to` | 종료 날짜 |

#### 응답 포함 정보

- `id`, `classDate`, `startTime`, `endTime`, `capacity`, `coachName`, `isCancelled`
- 연결된 WOD 목록 (`published` 상태인 것만): `[{ wodId, wodName, wodType, displayOrder }]`

---

### 1.5 FCM 토큰 관리 API

| Method | Path | 설명 |
|---|---|---|
| PATCH | `/api/v1/members/me/fcm-token` | FCM 토큰 등록 / 갱신 |
| DELETE | `/api/v1/members/me/fcm-token` | 로그아웃 시 토큰 삭제 |

#### Request Body (PATCH)

```json
{ "fcmToken": "fCM_TOKEN_STRING", "platform": "ios" }
```

`platform`: `"ios"` | `"android"`

#### DB 변경

`user_fcm_tokens` 테이블 신규 생성:

| 컬럼 | 타입 | 설명 |
|---|---|---|
| `user_id` | UUID FK | |
| `fcm_token` | VARCHAR(512) | |
| `platform` | ENUM | `ios` \| `android` |
| `updated_at` | TIMESTAMP | 갱신 시각 |
| PK | `(user_id, platform)` | 기기 플랫폼당 1개 |

---

### 1.6 FCM 알림 스케줄러 확장

#### 기존 WOD 공개 스케줄러 (`WodPublishScheduler`) 수정

현재: 공개 감지 후 로직 미구현 상태  
변경: 공개된 WOD에 연결된 수업 예약자 또는 박스 전체 ACTIVE 회원에게 FCM 발송

```json
{
  "title": "오늘의 WOD가 공개되었습니다!",
  "body": "250425 Fran 외 1개 — 확인해보세요",
  "data": {
    "type": "WOD_PUBLISHED",
    "wodDate": "2026-04-25",
    "boxId": "uuid"
  }
}
```

#### 신규: 회원권 만료 알림 스케줄러

매일 자정 실행. 만료 D-14, D-3 회원에게 FCM 발송.

```
1. 만료일이 오늘 + 14일인 ACTIVE 회원 조회
2. 만료일이 오늘 + 3일인 ACTIVE 회원 조회
3. 각 대상에 FCM 발송
```

```json
{
  "title": "⚠️ 회원권 만료 안내",
  "body": "회원권이 14일 후 만료됩니다. 코치에게 갱신 문의하세요.",
  "data": {
    "type": "MEMBERSHIP_EXPIRING",
    "daysLeft": 14,
    "boxId": "uuid"
  }
}
```

---

### 1.7 미션 & 성취 시스템

#### 1.7.1 데이터 모델 (신규 테이블)

**`missions`** (미션 정의)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `box_id` | UUID FK NULL | NULL = 플랫폼 기본 미션 |
| `category` | ENUM | `attendance` \| `wod_type` \| `scale` \| `pr` \| `benchmark` \| `team` \| `leaderboard` \| `social` |
| `difficulty` | ENUM | `easy` \| `medium` \| `hard` \| `elite` |
| `name` | VARCHAR(100) | 미션명 |
| `description` | TEXT | 조건 설명 |
| `condition_type` | ENUM | 아래 참고 |
| `condition_value` | INT | 목표 수치 (횟수, 일수 등) |
| `condition_meta` | JSONB NULL | 추가 조건 (WOD 타입, Named WOD 등) |
| `badge_icon_url` | VARCHAR(500) | 뱃지 이미지 URL |
| `badge_color` | VARCHAR(7) | 뱃지 배경 Hex 색상 |
| `is_active` | BOOLEAN | 비활성화 시 목록에서 숨김 |
| `display_order` | INT | 목록 정렬 순서 |
| `created_at` | TIMESTAMP | |

**`condition_type` ENUM 값**

| 값 | 조건 설명 |
|---|---|
| `total_records` | 누적 기록 제출 N회 |
| `consecutive_days` | N일 연속 기록 제출 |
| `records_in_month` | 한 달 내 N회 기록 |
| `records_by_wod_type` | 특정 WOD 타입 N회 (`condition_meta.wod_type`) |
| `rxd_records` | Rx'd 기록 N회 |
| `pr_count` | PR 달성 N회 |
| `named_wod_complete` | Named WOD 완수 (`condition_meta.named_wod_id`) |
| `named_wod_category_complete` | Named WOD 카테고리 전체 완수 (`condition_meta.category`) |
| `team_participate` | 팀전 WOD N회 참여 |
| `team_leader` | 팀전 리더로 N회 기록 |
| `leaderboard_top3` | 리더보드 Top 3 입성 N회 |
| `leaderboard_first` | 리더보드 1위 N회 |
| `share_card_created` | 공유 카드 생성 N회 |
| `scale_upgrade` | 동일 WOD 스케일 업 N회 |

**`member_mission_progress`** (개인별 진행도)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| `member_id` | UUID FK | |
| `mission_id` | UUID FK | |
| `box_id` | UUID FK | |
| `current_value` | INT | 현재 진행 수치 |
| `is_achieved` | BOOLEAN | 달성 여부 |
| `achieved_at` | TIMESTAMP NULL | 달성 시각 |
| `updated_at` | TIMESTAMP | |
| PK | `(member_id, mission_id)` | |

#### 1.7.2 미션 진행도 업데이트 트리거

미션 progress는 이벤트 발생 시 즉시 업데이트한다 (배치 아닌 실시간).

| 이벤트 | 관련 condition_type |
|---|---|
| WOD 기록 저장 | `total_records`, `consecutive_days`, `records_in_month`, `records_by_wod_type`, `rxd_records`, `scale_upgrade`, `named_wod_complete`, `named_wod_category_complete` |
| PR 달성 (`is_pr = true`) | `pr_count` |
| 팀 기록 저장 | `team_participate`, `team_leader` |
| 리더보드 순위 계산 | `leaderboard_top3`, `leaderboard_first` |
| 공유 카드 생성 API 호출 | `share_card_created` |

**달성 처리 플로우:**
```
1. 이벤트 발생
2. 관련 mission_id 목록 조회 (condition_type 기준)
3. 각 미션의 progress current_value 증가
4. current_value >= condition_value 이면 is_achieved = true, achieved_at = NOW()
5. 달성된 미션이 있으면 FCM 발송
```

#### 1.7.3 미션 API

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/v1/boxes/{boxId}/missions` | 전체 미션 목록 (필터 지원) | ACTIVE 회원 |
| GET | `/api/v1/boxes/{boxId}/missions/me` | 내 진행도 포함 미션 목록 | ACTIVE 회원 |
| GET | `/api/v1/boxes/{boxId}/missions/me/achievements` | 달성한 뱃지 목록 | ACTIVE 회원 |
| POST | `/api/v1/boxes/{boxId}/missions/me/share-card-created` | 공유 카드 생성 이벤트 기록 | ACTIVE 회원 |

**`GET .../missions` Query Params**

| 파라미터 | 설명 |
|---|---|
| `category` | 카테고리 필터 (복수) |
| `difficulty` | 난이도 필터 (복수) |

**`GET .../missions/me` Response 예시**

```json
{
  "data": [
    {
      "id": "uuid",
      "name": "한 주의 전사",
      "category": "attendance",
      "difficulty": "medium",
      "description": "7일 연속 기록 제출",
      "conditionValue": 7,
      "badgeIconUrl": "...",
      "badgeColor": "#FF6B2B",
      "progress": {
        "currentValue": 5,
        "isAchieved": false,
        "achievedAt": null
      }
    }
  ]
}
```

#### 1.7.4 미션 FCM 알림

```json
{
  "title": "🏅 미션 달성!",
  "body": "\"PR 사냥꾼\" 달성 — PR 5회 완료!",
  "data": {
    "type": "MISSION_ACHIEVED",
    "missionId": "uuid",
    "boxId": "uuid"
  }
}
```

---

### 1.8 알림 설정 저장 API

회원이 알림 카테고리별 on/off 설정을 서버에 저장.

| Method | Path | 설명 |
|---|---|---|
| GET | `/api/v1/members/me/notification-settings` | 알림 설정 조회 |
| PUT | `/api/v1/members/me/notification-settings` | 알림 설정 저장 |

**Request/Response Body**

```json
{
  "wodPublished": true,
  "prAchieved": true,
  "membershipExpiring": true,
  "missionAchieved": true
}
```

FCM 발송 전 해당 회원의 설정을 확인하여 비활성화된 카테고리는 발송 생략.

---

## 2. 코치 웹 신규 개발

### 2.1 미션 관리 페이지

**경로**: `/settings/missions` (Head Coach 이상)

#### 기능

1. **플랫폼 기본 미션 목록 조회**
   - 전체 미션 테이블 (카테고리, 난이도, 조건, 활성화 상태)
   - 박스 단위로 미션 활성화/비활성화 토글

2. **박스 전용 미션 추가 (선택 기능)**
   - 박스 고유 미션 생성 폼 (이름, 카테고리, 난이도, 조건 타입, 목표 수치)
   - 뱃지 색상 선택

3. **미션별 달성 현황 조회**
   - 미션별 달성 회원 수 / 진행 중 회원 수 확인

#### 화면 구성 (와이어프레임)

```
미션 관리
─────────────────────────────────────────

[플랫폼 기본 미션]  [박스 전용 미션]

카테고리: [전체 ▾]   난이도: [전체 ▾]

 이름              카테고리  난이도  달성자  활성화
 첫 발걸음         출석      하      42명    [ON ●]
 3일의 시작        출석      하      38명    [ON ●]
 한 주의 전사      출석      중      21명    [ON ●]
 이번 달의 투지    출석      중      15명    [OFF ○]
 ...

[+ 박스 전용 미션 추가]
```

---

### 2.2 회원 미션 달성 현황 (회원 상세 페이지 확장)

**경로**: `/members/:memberId` 내 탭 추가

기존 회원 상세에 **"미션 & 성취"** 탭 추가:
- 달성한 뱃지 목록 (달성일 포함)
- 진행 중인 미션 목록 (진행도 표시)

```
[기본정보]  [WOD 기록]  [미션 & 성취]   ← 탭 추가

── 달성한 뱃지 ──────────────────────
🏅 첫 발걸음      2026.01.05 달성
🏅 3일의 시작     2026.01.07 달성
🏅 첫 번째 PR     2026.02.12 달성

── 진행 중인 미션 ───────────────────
한 주의 전사  ████████░░  5/7일
기록 파괴자   ██░░░░░░░░  3/20회
```

---

### 2.3 WOD 공개 시 FCM 발송 대상 설정 (WOD 설정 페이지 확장)

**경로**: `/settings/wod` 내 항목 추가

기존 WOD 공개 정책 설정 하단에:

```
WOD 공개 알림 발송 대상
● 박스 전체 ACTIVE 회원
○ 해당 수업 예약자만  (수업 예약 기능 추후 추가)
```

---

### 우선순위 요약

| 우선순위 | 항목 | 비고 |
|---|---|---|
| P0 | 회원용 WOD 조회 API | 앱 핵심 기능 |
| P0 | 회원용 기록 입력 API (개인 + 팀) | 앱 핵심 기능 |
| P0 | 회원용 리더보드 API | 앱 핵심 기능 |
| P0 | FCM 토큰 관리 API | 알림 전제 조건 |
| P0 | WOD 공개 FCM 발송 (스케줄러 확장) | 리텐션 핵심 |
| P1 | 회원용 수업 인스턴스 API | |
| P1 | 회원용 내 기록 조회 API | |
| P1 | PR 달성 FCM 발송 | |
| P1 | 회원권 만료 알림 스케줄러 | |
| P1 | 알림 설정 저장 API | |
| P2 | 미션 시스템 전체 (DB + API + 진행도 업데이트 + FCM) | |
| P2 | 코치 웹 — 미션 관리 페이지 | |
| P2 | 코치 웹 — 회원 상세 미션 탭 | |
| P2 | 회원용 벤치마크 / 1RM API | |
