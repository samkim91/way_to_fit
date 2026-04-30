# WOD & Leaderboard 기능 명세서 (v3.0)
> 대상: 코치용 웹 앱 (Coach / Head Coach / Super Admin)
> 회원 앱은 별도 문서로 분리 예정

---

## 목차
1. [아키텍처 개요](#1-아키텍처-개요)
2. [데이터 모델](#2-데이터-모델)
3. [백엔드 API 명세](#3-백엔드-api-명세)
4. [프론트엔드 화면 명세](#4-프론트엔드-화면-명세)
5. [비즈니스 규칙](#5-비즈니스-규칙)

---

## 1. 아키텍처 개요

### 핵심 개념 분리

```
┌─────────────────────────────────────────────────────┐
│  WOD 은행 (wod_templates)                           │
│  - Head Coach 이상만 접근 가능                      │
│  - 날짜 없음, 재사용 가능 콘텐츠 저장소            │
│  - Named WOD 태깅 가능 (Fran, Murph 등)            │
└──────────────────┬──────────────────────────────────┘
                   │ 복사(snapshot) 또는 직접 생성
                   ▼
┌─────────────────────────────────────────────────────┐
│  배정된 WOD (wods)                                  │
│  - 날짜 있음, 기록·리더보드의 단위                 │
│  - published_at으로 공개 시점 제어                  │
│  - template과 독립 (이후 template 수정 영향 없음)  │
└──────────────────┬──────────────────────────────────┘
                   │ N개 수업에 연결 (Part A / Part B 지원)
                   ▼
┌─────────────────────────────────────────────────────┐
│  수업 연결 (wod_class_instances)                    │
│  - wod ↔ ClassInstance N:M                         │
│  - 1개 수업에 복수 WOD 가능 (display_order로 순서) │
└─────────────────────────────────────────────────────┘
```

### Scale Group 설계 원칙

```
Rx'd 기록
  - scale_group_id = NULL로 저장
  - 리더보드 항상 최상단

Scale Group (wods.scale_groups)
  - Rx'd에서 변경된 점만 기술
  - id만 사용 (label 중복 제거), id가 곧 UI 표시 텍스트
  - 배열 index 순서 = 리더보드 우선순위
  - 기록이 있는 그룹의 id 변경 불가

예시:
  groups: [
    { "id": "scaled", "description": "30/20kg, 밴드 풀업" },
    { "id": "bg",     "description": "20/15kg, 링로우" }
  ]
  → 리더보드: Rx'd (null) > scaled > bg
```

### WOD 공개 상태 흐름

```
배정 생성
    │
    ├─ published_at = NULL        → draft (임시저장)
    ├─ published_at > NOW()       → scheduled (예약 공개 대기)
    └─ published_at <= NOW()      → published (공개됨)
                                        ↑
                             스케줄러 매 5분 감지
                             → 푸시 알림 발송
```

### Named WOD 설계 원칙

```
named_wods (플랫폼 고정 마스터)
  - Fran, Murph 등 CrossFit 공식 WOD
  - 플랫폼 전체 공용, 박스별 커스텀 없음

wod_templates / wods
  - named_wod_id 태깅으로 연결
  - 박스가 Fran을 직접 작성해도 named_wod_id만 달면
    플랫폼 전체 벤치마크 집계에 포함됨
```

---

## 2. 데이터 모델

### 2.1 `box_wod_settings` (박스별 WOD 공개 정책)

> 향후 박스 전체 설정 테이블로 흡수 예정

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `box_id` | UUID PK FK | |
| `timezone` | VARCHAR(50) | 박스 기준 타임존 (기본: `Asia/Seoul`) |
| `publish_policy` | ENUM | `manual` \| `scheduled` |
| `publish_anchor` | ENUM | `class_start` \| `fixed_time_of_day` |
| `publish_offset_minutes` | INT NULL | `class_start` 기준 N분 오프셋 (음수 = 이전) |
| `publish_fixed_time` | TIME NULL | `fixed_time_of_day` 앵커 시각 (예: `05:00`) |
| `publish_fixed_day_offset` | INT NULL | 수업일 기준 며칠 전 (`-1` = 전날, `0` = 당일) |
| `updated_at` | TIMESTAMP | |

#### 정책 예시

| 박스 정책 | `publish_anchor` | `publish_fixed_day_offset` | `publish_fixed_time` | `publish_offset_minutes` |
|---|---|---|---|---|
| 수업 시작 30분 전 | `class_start` | — | — | `-30` |
| 전날 밤 10시 | `fixed_time_of_day` | `-1` | `22:00` | — |
| 당일 오전 5시 | `fixed_time_of_day` | `0` | `05:00` | — |
| 수동 공개 | `manual` | — | — | — |

#### `published_at` 자동 계산 로직

```
policy = manual
  → published_at = NULL

policy = scheduled, anchor = class_start
  → 연결된 class_instance의 startTime + publish_offset_minutes
  → 수업 없는 독립 WOD → manual 처리

policy = scheduled, anchor = fixed_time_of_day
  → (wod.date + publish_fixed_day_offset)의 publish_fixed_time
  → box timezone 기준으로 계산 후 UTC Instant 변환
  예) date=8/20, offset=-1, time=22:00, tz=Asia/Seoul
      → 2025-08-19T13:00:00Z
```

> 설정 변경은 이후 새로 배정되는 WOD부터 적용. 기존 `published_at` 소급 적용 없음.

---

### 2.2 `wod_templates` (WOD 은행)

> **접근 권한: Head Coach 이상만**

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `box_id` | UUID FK | NULL = 플랫폼 공용 |
| `coach_id` | UUID FK | 작성 코치 (audit 목적) |
| `name` | VARCHAR(100) | 필수 |
| `description` | TEXT | 운동 내용 (Rx'd 기준 기술) |
| `image_urls` | TEXT[] | |
| `video_url` | VARCHAR(500) | |
| `wod_type` | ENUM | `for_time` \| `amrap` \| `emom` \| `max_weight` \| `custom` |
| `custom_type_label` | VARCHAR(100) | `wod_type = custom`일 때 필수 |
| `time_cap` | INT NULL | 제한 시간 초 (For Time / Custom, 0 = 제한 없음) |
| `amrap_duration` | INT NULL | AMRAP 총 시간 (초) |
| `emom_duration` | INT NULL | EMOM 총 시간 (초, NULL = Death by 등 무제한) |
| `emom_target_reps` | INT NULL | EMOM 목표 총 rep (NULL = 목표 없이 기록만) |
| `weight_unit` | ENUM NULL | `kg` \| `lb` |
| `scale_groups` | JSONB | 스케일 그룹 정의 (하단 참고) |
| `competition_mode` | ENUM | `none` \| `individual` \| `team` |
| `named_wod_id` | UUID FK NULL | Named WOD 태깅 |
| `tags` | TEXT[] | 자유 태그 |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |

#### `scale_groups` JSONB 구조

```json
{
  "groups": [
    { "id": "scaled", "description": "30/20kg, 밴드 풀업" },
    { "id": "bg",     "description": "20/15kg, 링로우" }
  ]
}
```

> - `groups` 배열이 비어 있으면 Rx'd 단일 그룹 WOD
> - 배열 index 순서 = 리더보드 우선순위 (0번이 Rx'd 다음 우선)
> - `id`는 슬러그 형태 권장 (예: `scaled`, `bg`, `masters`)
> - Rx'd 기록은 `scale_group_id = NULL`로 별도 처리 (groups에 포함하지 않음)
> - 기록이 존재하는 그룹의 `id` 변경 불가 (프론트 비활성화 처리)

---

### 2.3 `wods` (배정된 WOD)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `box_id` | UUID FK | |
| `coach_id` | UUID FK | 배정 코치 (audit 목적) |
| `template_id` | UUID FK NULL | 출처 템플릿 (직접 작성 시 NULL) |
| `date` | DATE | 필수 |
| `name` | VARCHAR(100) | 필수 |
| `description` | TEXT | |
| `image_urls` | TEXT[] | |
| `video_url` | VARCHAR(500) | |
| `wod_type` | ENUM | `for_time` \| `amrap` \| `emom` \| `max_weight` \| `custom` |
| `custom_type_label` | VARCHAR(100) | |
| `time_cap` | INT NULL | |
| `amrap_duration` | INT NULL | |
| `emom_duration` | INT NULL | NULL = Death by 등 무제한 EMOM |
| `emom_target_reps` | INT NULL | |
| `weight_unit` | ENUM NULL | |
| `scale_groups` | JSONB | wod_templates와 동일 구조 (snapshot) |
| `competition_mode` | ENUM | `none` \| `individual` \| `team` |
| `named_wod_id` | UUID FK NULL | |
| `published_at` | TIMESTAMP NULL | NULL=draft, 미래=scheduled, 과거=published |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |

> `wod_type` 변경 규칙:
> - 연결된 `wod_records`가 1개라도 존재하면 `wod_type` 변경 **완전 차단** (`409 Conflict`)
> - 프론트엔드: 기록이 있으면 타입 선택 UI 자체를 비활성화

---

### 2.4 `wod_class_instances` (WOD ↔ 수업 연결)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `wod_id` | UUID FK | |
| `class_instance_id` | UUID FK | |
| `display_order` | INT | 같은 수업 내 WOD 표시 순서 (Part A=1, Part B=2) |
| **PK** | `(wod_id, class_instance_id)` | |

> - UNIQUE 제약 없음 — 1개 수업에 복수 WOD 가능 (Part A / Part B)
> - 1개 WOD가 여러 수업에 연결 가능 (오전반 / 오후반 동일 WOD)
> - `display_order`는 수업 내 WOD 순서 보장용, 박스마다 다를 수 있음
> - ClassInstance 취소 시 WOD·기록 유지
> - WOD 삭제 시 cascade 삭제

---

### 2.5 `wod_records` (개인 기록)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `wod_id` | UUID FK | |
| `member_id` | UUID FK | |
| `box_id` | UUID FK | |
| `team_id` | UUID FK NULL | 팀전 소속 팀 |
| `scale_group_id` | VARCHAR(50) NULL | NULL = Rx'd, 그 외 = scale group id |
| `result_time_seconds` | INT NULL | For Time 결과 |
| `result_rounds` | INT NULL | AMRAP 라운드 수 |
| `result_reps` | INT NULL | AMRAP 추가 rep / EMOM 누적 rep |
| `result_weight` | DECIMAL(6,2) NULL | Max Weight |
| `result_custom` | TEXT NULL | Custom 자유 텍스트 |
| `result_status` | ENUM | `completed` \| `dnf` |
| `memo` | TEXT NULL | |
| `video_url` | VARCHAR(500) NULL | |
| `is_pr` | BOOLEAN | |
| `recorded_by` | UUID FK | 입력 주체 (본인 or 코치) |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |

#### WOD 타입별 사용 필드

| WOD 타입 | 사용 필드 | 비고 |
|---|---|---|
| For Time | `result_time_seconds` | DNF 시 `result_status = dnf` |
| AMRAP | `result_rounds`, `result_reps` | |
| EMOM | `result_reps` | 누적 rep, `emom_target_reps`와 비교해 완수 여부 UI 표시 |
| Max Weight | `result_weight` | |
| Custom | `result_custom` | |

#### EMOM 기록 상세

```
wod.emom_duration    = 600 (10분)
wod.emom_target_reps = 150 (분당 15회 목표)

기록 예시:
  result_reps = 150  → UI: ✅ 완수 (150 / 150)
  result_reps = 127  → UI: ⚠️ 미완수 (127 / 150)
  result_status = dnf, result_reps = 마지막 완수까지 누적값

Death by 패턴 (emom_duration = NULL):
  MVP: Custom 타입으로 우회
  추후: result_reps = 실패 직전 분 수로 확장 예정
```

---

### 2.6 `wod_teams` (팀전 팀)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `wod_id` | UUID FK | |
| `box_id` | UUID FK | |
| `name` | VARCHAR(100) | 팀명 (기본값: 팀원 이름 이어붙임, 수정 가능) |
| `gender_category` | ENUM | `men` \| `women` \| `mixed` (팀원 구성 기반 자동 계산, 저장) |
| `scale_group_id` | VARCHAR(50) NULL | NULL = Rx'd |
| `result_time_seconds` | INT NULL | |
| `result_rounds` | INT NULL | |
| `result_reps` | INT NULL | |
| `result_weight` | DECIMAL(6,2) NULL | |
| `result_custom` | TEXT NULL | |
| `result_status` | ENUM | `completed` \| `dnf` |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |

#### `gender_category` 자동 계산 규칙

```
팀원 전원 male   → men
팀원 전원 female → women
그 외 혼성       → mixed
```

> - 팀원 추가/삭제 시 서버에서 자동 재계산하여 저장
> - 팀원의 성별 정보는 `members` 테이블의 `gender` 필드 참조

---

### 2.7 `wod_team_members` (팀 구성원)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `team_id` | UUID FK | |
| `member_id` | UUID FK | |
| `role` | ENUM | `leader` \| `member` |
| **PK** | `(team_id, member_id)` | |

---

### 2.8 `named_wods` (벤치마크 WOD 마스터)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `name` | VARCHAR(100) | 예: "Fran", "Murph" |
| `category` | ENUM | `the_girls` \| `hero` \| `other` |
| `description` | TEXT | 공식 내용 |
| `default_wod_type` | ENUM | 기본 측정 타입 |

---

### 2.9 `movements` / `member_1rm_records` (1RM 기록)

**`movements`**

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `box_id` | UUID FK NULL | NULL = 플랫폼 기본 종목 |
| `name` | VARCHAR(100) | |
| `category` | ENUM | `barbell` \| `gymnastic` \| `cardio` \| `other` |
| `is_1rm_trackable` | BOOLEAN | |

**`member_1rm_records`**

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `member_id` | UUID FK | |
| `movement_id` | UUID FK | |
| `weight` | DECIMAL(6,2) | |
| `weight_unit` | ENUM | `kg` \| `lb` |
| `recorded_date` | DATE | |
| `wod_record_id` | UUID FK NULL | WOD 기록에서 자동 연동 시 |
| `created_at` | TIMESTAMP | |

---

## 3. 백엔드 API 명세

### API Prefix 규칙
- **코치/어드민**: `/api/v1/admin/boxes/:boxId/...`
- **회원 (추후)**: `/api/v1/boxes/:boxId/...`

---

### 3.1 박스 WOD 설정

#### `GET /api/v1/admin/boxes/:boxId/wod-settings`
**권한**: Head Coach 이상

**Response**
```json
{
  "timezone": "Asia/Seoul",
  "publish_policy": "scheduled",
  "publish_anchor": "fixed_time_of_day",
  "publish_fixed_time": "22:00",
  "publish_fixed_day_offset": -1
}
```

#### `PUT /api/v1/admin/boxes/:boxId/wod-settings`
**권한**: Head Coach 이상

```json
{
  "timezone": "Asia/Seoul",
  "publish_policy": "scheduled",
  "publish_anchor": "fixed_time_of_day",
  "publish_fixed_time": "22:00",
  "publish_fixed_day_offset": -1
}
```

---

### 3.2 WOD 은행 (Templates)

> **모든 엔드포인트: Head Coach 이상**

#### `POST /api/v1/admin/boxes/:boxId/wod-templates`

```json
{
  "name": "Fran",
  "description": "21-15-9\nThrusters (43/29kg)\nPull-ups",
  "wod_type": "for_time",
  "time_cap": 0,
  "scale_groups": {
    "groups": [
      { "id": "scaled", "description": "30/20kg, 밴드 풀업" },
      { "id": "bg",     "description": "20/15kg, 링로우" }
    ]
  },
  "competition_mode": "individual",
  "named_wod_id": "uuid-fran",
  "tags": ["전신", "benchmark"]
}
```

#### `GET /api/v1/admin/boxes/:boxId/wod-templates`

**Query Params**

| 파라미터 | 설명 |
|---|---|
| `q` | 이름 / 태그 검색 |
| `wod_type` | 타입 필터 |
| `named_wod_id` | Named WOD 필터 |
| `tags` | 태그 필터 (복수) |
| `page` / `limit` | 페이지네이션 |

#### `GET /api/v1/admin/boxes/:boxId/wod-templates/:templateId`

#### `PATCH /api/v1/admin/boxes/:boxId/wod-templates/:templateId`
원본 수정. 기배정 wod 영향 없음.

#### `DELETE /api/v1/admin/boxes/:boxId/wod-templates/:templateId`
기배정 wod 유지. `wods.template_id → NULL` 처리.

#### `POST /api/v1/admin/boxes/:boxId/wod-templates/:templateId/deploy`
**템플릿 → WOD 배정**

```json
{
  "date": "2025-08-20",
  "class_instance_ids": [
    { "class_instance_id": "uuid1", "display_order": 1 },
    { "class_instance_id": "uuid2", "display_order": 1 }
  ],
  "published_at": "auto",
  "overrides": {
    "name": "250820 Fran",
    "time_cap": 600
  }
}
```

**`published_at` 처리**

| 값 | 처리 |
|---|---|
| `"auto"` | `box_wod_settings` 정책 기반 자동 계산 |
| ISO 8601 | 해당 시각으로 직접 설정 |
| `null` | draft (임시저장) |

**서버 처리**
1. template 필드 + overrides 병합
2. `published_at` 계산
3. `wods` 레코드 생성 (snapshot)
4. `wod_class_instances` 생성 (`display_order` 포함)

**Response**
```json
{
  "wod": {
    "id": "uuid",
    "date": "2025-08-20",
    "name": "250820 Fran",
    "published_at": "2025-08-19T13:00:00Z",
    "published_at_local": "2025-08-19T22:00:00+09:00",
    "publish_status": "scheduled"
  },
  "class_instances_linked": 2
}
```

---

### 3.3 WOD CRUD

#### `POST /api/v1/admin/boxes/:boxId/wods`
직접 생성 (Head Coach 이상)

```json
{
  "date": "2025-08-20",
  "class_instance_ids": [
    { "class_instance_id": "uuid1", "display_order": 2 }
  ],
  "name": "250820 Part B",
  "description": "12분 AMRAP\n...",
  "wod_type": "amrap",
  "amrap_duration": 720,
  "scale_groups": {
    "groups": [
      { "id": "scaled", "description": "무게 조절" }
    ]
  },
  "competition_mode": "none",
  "published_at": "auto"
}
```

#### `GET /api/v1/admin/boxes/:boxId/wods`

**Query Params**

| 파라미터 | 설명 |
|---|---|
| `date` | 특정 날짜 |
| `from` / `to` | 날짜 범위 |
| `publish_status` | `draft` \| `scheduled` \| `published` |
| `competition_mode` | `none` \| `individual` \| `team` |
| `named_wod_id` | Named WOD 필터 |

**Response**
```json
{
  "data": [
    {
      "date": "2025-08-20",
      "wods": [
        {
          "id": "uuid",
          "name": "250820 Part A - Back Squat",
          "wod_type": "max_weight",
          "competition_mode": "none",
          "template_id": "uuid-template",
          "named_wod_id": null,
          "publish_status": "scheduled",
          "published_at": "2025-08-19T13:00:00Z",
          "record_count": 0,
          "class_instances": [
            {
              "id": "uuid1",
              "start_time": "2025-08-20T07:00:00Z",
              "routine_name": "아침 6시반",
              "display_order": 1
            }
          ]
        },
        {
          "id": "uuid2",
          "name": "250820 Part B - Fran",
          "wod_type": "for_time",
          "publish_status": "scheduled",
          "published_at": "2025-08-19T13:00:00Z",
          "record_count": 0,
          "class_instances": [
            {
              "id": "uuid1",
              "start_time": "2025-08-20T07:00:00Z",
              "routine_name": "아침 6시반",
              "display_order": 2
            }
          ]
        }
      ]
    }
  ]
}
```

#### `GET /api/v1/admin/boxes/:boxId/wods/:wodId`

#### `PATCH /api/v1/admin/boxes/:boxId/wods/:wodId`
**제약**: `wod_records`가 1개라도 존재하면 `wod_type` 변경 → `409 Conflict`

```json
// 409 응답 예시
{
  "error": "WOD_TYPE_CHANGE_BLOCKED",
  "message": "기록이 존재하는 WOD의 타입은 변경할 수 없습니다. 새 WOD를 생성해 주세요.",
  "record_count": 12
}
```

#### `DELETE /api/v1/admin/boxes/:boxId/wods/:wodId`
Head Coach 이상. 기록 존재 시 soft delete.

#### `PATCH /api/v1/admin/boxes/:boxId/wods/:wodId/class-instances`
수업 연결 수정

```json
{
  "add": [{ "class_instance_id": "uuid3", "display_order": 3 }],
  "remove": ["uuid1"]
}
```

#### `PATCH /api/v1/admin/boxes/:boxId/wods/:wodId/publish`
공개 시각 수정 (Coach 이상)

```json
{ "published_at": "2025-08-19T13:00:00Z" }
// 또는
{ "published_at": null }
```

---

### 3.4 WOD 기록

#### `POST /api/v1/admin/boxes/:boxId/wods/:wodId/records`
Coach: 타인 기록 입력 가능 / Member: 본인만

**For Time**
```json
{
  "member_id": "uuid",
  "scale_group_id": null,
  "result_time_seconds": 480,
  "result_status": "completed",
  "memo": "힘들었음"
}
```

**AMRAP**
```json
{
  "member_id": "uuid",
  "scale_group_id": "scaled",
  "result_rounds": 8,
  "result_reps": 12,
  "result_status": "completed"
}
```

**EMOM**
```json
{
  "member_id": "uuid",
  "scale_group_id": null,
  "result_reps": 143,
  "result_status": "completed"
}
```

**Max Weight**
```json
{
  "member_id": "uuid",
  "scale_group_id": null,
  "result_weight": 102.5,
  "result_status": "completed"
}
```

**Custom**
```json
{
  "member_id": "uuid",
  "scale_group_id": "scaled",
  "result_custom": "5라운드 + 버피 12개",
  "result_status": "completed"
}
```

**Response**
```json
{
  "record": { ... },
  "is_pr": true,
  "previous_best": { "result_time_seconds": 510 }
}
```

#### `PATCH /api/v1/admin/boxes/:boxId/wods/:wodId/records/:recordId`
Coach 이상

#### `DELETE /api/v1/admin/boxes/:boxId/wods/:wodId/records/:recordId`
Coach 이상 또는 당일 본인

---

### 3.5 팀 기록

#### `POST /api/v1/admin/boxes/:boxId/wods/:wodId/teams`
`competition_mode = team`인 WOD만 (Coach 이상)

```json
{
  "name": null,
  "scale_group_id": null,
  "members": [
    { "member_id": "uuid1", "role": "leader" },
    { "member_id": "uuid2", "role": "member" },
    { "member_id": "uuid3", "role": "member" }
  ],
  "result_time_seconds": 890,
  "result_status": "completed"
}
```

**팀명 자동 생성**
- `name = null` 또는 빈 문자열이면 서버에서 자동 생성
- 생성 규칙: leader 이름 우선, 이후 member 순으로 `" · "` 구분자로 이어붙임
- 예: `"홍길동 · 김철수 · 이영희"`
- 이후 `PATCH`로 팀명 자유 수정 가능

**`gender_category` 자동 계산 및 저장**
- 팀원 전원 male → `men`
- 팀원 전원 female → `women`
- 혼성 → `mixed`
- 팀원 추가/삭제(PATCH) 시 서버에서 재계산

**Validation**
- `leader` 정확히 1명
- 동일 WOD 내 타 팀 중복 소속 → `409 Conflict`
- 팀 전체 단일 scale group

**Response**
```json
{
  "team": {
    "id": "uuid",
    "name": "홍길동 · 김철수 · 이영희",
    "gender_category": "mixed",
    "scale_group_id": null,
    "members": [
      { "member_id": "uuid1", "name": "홍길동", "gender": "male", "role": "leader" },
      { "member_id": "uuid2", "name": "김철수", "gender": "male", "role": "member" },
      { "member_id": "uuid3", "name": "이영희", "gender": "female", "role": "member" }
    ],
    "result_time_seconds": 890,
    "result_status": "completed"
  }
}
```

#### `PATCH /api/v1/admin/boxes/:boxId/wods/:wodId/teams/:teamId`
Coach 이상. 팀명 수정, 팀원 변경, 기록 수정 모두 가능.
팀원 변경 시 `gender_category` 자동 재계산.

#### `DELETE /api/v1/admin/boxes/:boxId/wods/:wodId/teams/:teamId`
Coach 이상

---

### 3.6 리더보드

#### `GET /api/v1/admin/boxes/:boxId/wods/:wodId/leaderboard`

**Query Params**

| 파라미터 | 설명 |
|---|---|
| `gender` | `male` \| `female` \| `all` (개인전 성별 필터) |
| `scale_group_id` | `null`(Rx'd) \| `scaled` \| `bg` \| `all` |
| `mode` | `individual` \| `team` |
| `gender_category` | `men` \| `women` \| `mixed` \| `all` (팀전 구성 필터) |

**정렬 로직**
```
1. scale_group_id IS NULL 최상단 (Rx'd)
2. scale_groups.groups 배열 index 순 (0번 → 1번 → ...)
3. WOD 타입별:
   for_time    → result_time_seconds ASC
   amrap       → (result_rounds DESC, result_reps DESC)
   emom        → result_reps DESC
   max_weight  → result_weight DESC
   custom      → 순위 없음 (created_at ASC)
4. DNF → 해당 그룹 내 최하단
```

**Response (개인전)**
```json
{
  "wod": { ... },
  "mode": "individual",
  "leaderboard": [
    {
      "rank": 1,
      "member": { "id": "uuid", "name": "홍길동", "gender": "male", "avatar_url": "..." },
      "scale_group_id": null,
      "scale_group_label": "Rx'd",
      "result_display": "8:00",
      "result_time_seconds": 480,
      "is_pr": true
    },
    {
      "rank": 1,
      "member": { "id": "uuid2", "name": "박민수", "gender": "male", "avatar_url": "..." },
      "scale_group_id": "scaled",
      "scale_group_label": "scaled",
      "result_display": "7:45",
      "result_time_seconds": 465,
      "is_pr": false
    }
  ],
  "total_count": 24
}
```

**Response (팀전)**
```json
{
  "wod": { ... },
  "mode": "team",
  "leaderboard": [
    {
      "rank": 1,
      "team": {
        "id": "uuid",
        "name": "홍길동 · 김철수 · 이영희",
        "gender_category": "mixed",
        "gender_composition": "2M/1W",
        "members": [
          { "name": "홍길동", "gender": "male", "role": "leader" },
          { "name": "김철수", "gender": "male", "role": "member" },
          { "name": "이영희", "gender": "female", "role": "member" }
        ]
      },
      "scale_group_id": null,
      "scale_group_label": "Rx'd",
      "result_display": "14:50",
      "result_time_seconds": 890
    }
  ],
  "total_count": 6
}
```

> `gender_composition`: 팀원 성별 인원 수 표시용 계산값 (예: `"2M/1W"`, `"3M"`, `"2W"`). DB 저장 안 함, API 응답에서 계산.

---

### 3.7 Named WOD 벤치마크

#### `GET /api/v1/named-wods`

#### `GET /api/v1/admin/boxes/:boxId/members/:memberId/benchmark`

```json
{
  "benchmark": [
    {
      "named_wod": { "id": "uuid", "name": "Fran", "category": "the_girls" },
      "pr": { "result_display": "5:48", "date": "2025-08-20", "scale_group_id": null },
      "records": [
        { "wod_id": "uuid", "date": "2025-01-10", "result_display": "6:23", "scale_group_id": null },
        { "wod_id": "uuid2", "date": "2025-08-20", "result_display": "5:48", "scale_group_id": null, "is_pr": true }
      ]
    }
  ]
}
```

---

### 3.8 스케줄러

**`WodPublishScheduler`** — 매 5분 실행

```
1. published_at <= NOW() AND published_at > NOW() - interval '5 minutes'
   인 wods 조회 (방금 공개된 것들)
2. 연결된 class_instance 예약자 대상 푸시 알림 발송
3. WebSocket / SSE 이벤트 발행 (실시간 반영)
```

> 공개 여부는 `published_at <= NOW()` 실시간 판단. 별도 상태 컬럼 없음.

---

## 4. 프론트엔드 화면 명세

> UX 원칙: 복잡한 구조는 뒤에 숨기고 자주 쓰는 흐름은 최소 클릭으로 완료.
> 코치 핵심 태스크: 캘린더 → 날짜 선택 → 은행에서 가져오기 → 수업 연결 → 배정 완료.

---

### 4.1 WOD 캘린더 화면
`/wods`

**레이아웃**: 월간 캘린더 (좌 70%) + 선택 날짜 사이드패널 (우 30%)

#### 캘린더 날짜 셀

```
  20
  🔵🔵  ← 파랑 dot: 수업 연동 WOD 수
  ⚫    ← 회색 dot: 독립 WOD 수
  ⏰    ← 예약 공개 대기 WOD 있을 때
```

#### 선택 날짜 사이드패널

```
[ 2025년 8월 20일 수요일 ]

 ┌─ 아침 6시반 클래스 ─────────────────────────┐
 │ Part A  백스쿼트 1RM       [Max Weight] 🟢  │
 │ Part B  250820 Fran        [For Time]   🟢  │
 │ 참여 18명  Rx'd 8 · scaled 7 · bg 3         │
 │ 🏆 PR 3명                                   │
 │                       [리더보드] [편집]      │
 └─────────────────────────────────────────────┘

 ┌─ 독립 WOD ──────────────────────────────────┐
 │ 오픈짐 WOD                [AMRAP]      📝   │
 │                       [리더보드] [편집]      │
 └─────────────────────────────────────────────┘

 [+ 이 날에 WOD 추가]
```

> 수업 내 Part A / Part B는 `display_order` 순으로 들여쓰기 표시.

#### 공개 상태 배지

| 상태 | 배지 | 조건 |
|---|---|---|
| 공개됨 | 🟢 | `published_at <= NOW()` |
| 예약 공개 | ⏰ N월 N일 N시 예정 | `published_at > NOW()` |
| 임시저장 | 📝 | `published_at IS NULL` |

#### 빠른 WOD 추가 플로우

`[+ 이 날에 WOD 추가]` 클릭 → 인라인 선택:

```
 ┌──────────────────────────────────────────┐
 │  어떻게 추가할까요?                       │
 │  ┌────────────────┐  ┌────────────────┐  │
 │  │ 🗂 WOD 은행    │  │ ✏️ 직접 작성   │  │
 │  │   에서 가져오기 │  │               │  │
 │  └────────────────┘  └────────────────┘  │
 └──────────────────────────────────────────┘
```

---

### 4.2 WOD 은행 화면
`/wod-templates` (Head Coach 이상)

#### 검색 & 필터 사이드바

| 필터 | UI |
|---|---|
| 키워드 | Text Input (이름, 태그 동시 검색) |
| WOD 타입 | 체크박스 |
| Named WOD | 드롭다운 (The Girls / Hero / Other) |
| 태그 | 태그 버튼 토글 |

#### 템플릿 카드

```
┌──────────────────────────────────────────────┐
│  Fran                   [For Time]  [Named]  │
│  21-15-9 Thrusters / Pull-ups                │
│  태그: #benchmark #전신                       │
│  최근 배정: 2025.06.15   총 배정 8회          │
│                                              │
│        [배정하기]   [편집]   [복제]           │
└──────────────────────────────────────────────┘
```

#### 배정 슬라이드오버

`[배정하기]` 클릭 → 우측 슬라이드오버:

```
┌──────────────────────────────────────────────┐
│  Fran 배정                             [×]   │
├──────────────────────────────────────────────┤
│  날짜   [ 2025-08-20              📅 ]       │
│                                              │
│  수업 연결                       (선택)       │
│  ────────────────────────────────────────    │
│  ☑ 아침 6시반  07:00   Part [ 1 ▾ ]         │
│  ☐ 오후 5시    17:00   Part [ 1 ▾ ]         │
│  ☐ 저녁 7시    19:00   ⚠️ Part 1 이미 배정   │
│  ○ 수업 연결 안 함                            │
│                                              │
│  공개 시점                                   │
│  ● 박스 정책 자동 적용                        │
│    → 전날 (8/19) 22:00 공개 예정             │
│  ○ 직접 설정  [ 2025-08-19 ] [ 22:00 ]      │
│  ○ 임시저장                                  │
│                                              │
│  ▶ 내용 수정하기          ← 기본 접힘        │
│                                              │
│               [취소]   [배정 완료]            │
└──────────────────────────────────────────────┘
```

> - 수업별로 Part 번호(display_order) 선택 드롭다운 표시
> - 해당 Part가 이미 배정된 경우 `⚠️` 표시 + 선택 가능 (덮어쓰기 아닌 추가)
> - "내용 수정하기" 기본 접힘 → 펼치면 name, description 등 override 가능
> - "박스 정책 자동 적용" 선택 시 계산된 예정 시각 미리보기 표시

---

### 4.3 WOD 작성/수정 폼
`/wods/new` | `/wods/:wodId/edit`
`/wod-templates/new` | `/wod-templates/:id/edit`
(날짜·수업·공개 섹션은 WOD 작성 시만 표시, 동일 컴포넌트 재사용)

#### 섹션 1. 기본 정보

| 필드 | UI | 비고 |
|---|---|---|
| 날짜 | Date Picker | WOD만 |
| 수업 연결 | 체크박스 + Part 번호 선택 | WOD만, 이미 같은 Part 배정된 수업 표시 |
| WOD 이름 | Text Input | 필수 |
| 설명 | Rich Text Editor | Rx'd 기준으로 기술 |
| 영상 링크 | URL Input | 선택 |

> **미래 수업 인스턴스 UX**: 날짜 선택 시 해당 날짜의 ClassInstance를 API로 조회. 인스턴스가 없는 날짜는 "해당 날짜에 수업이 없습니다. 독립 WOD로 등록됩니다." 안내 표시.

#### 섹션 2. 측정 타입

```
[ For Time ⏱ ]  [ AMRAP 🔄 ]  [ EMOM ⚡ ]  [ Max Weight 🏋 ]  [ Custom ✏️ ]

── For Time ────────────────────────────────────
  Time Cap   [ 20 ] 분   □ 제한 없음

── AMRAP ───────────────────────────────────────
  총 시간    [ 12 ] 분

── EMOM ────────────────────────────────────────
  총 시간          [ 10 ] 분   □ 무제한 (Death by 등)
  목표 총 Reps     [ 150  ]    선택, 미입력 시 순수 기록만

── Max Weight ──────────────────────────────────
  단위   ● kg   ○ lb

── Custom ──────────────────────────────────────
  타입 레이블   [ Chipper              ]
```

> 기록이 존재하는 WOD 수정 시 타입 선택 UI 전체 비활성화 + 안내:
> "기록이 있는 WOD의 타입은 변경할 수 없습니다."

#### 섹션 3. 스케일 그룹

```
  Rx'd는 WOD 설명 기준 (scale group 별도 등록 불필요)

  ┌── scaled ───────────────┐  ┌── bg ───────────────────┐
  │ id: scaled              │  │ id: bg                  │
  │ 설명: [30/20kg, 밴드...]│  │ 설명: [20/15kg, 링로우] │
  │                    [×]  │  │                    [×]  │
  └─────────────────────────┘  └─────────────────────────┘
  [+ 그룹 추가]

  ※ 위에서부터 순서대로 리더보드 Rx'd 다음 우선순위
  ※ 카드 드래그로 순서 변경 가능
  ※ 기록이 있는 그룹의 id 변경 불가 (자물쇠 아이콘 표시)
```

#### 섹션 4. 경쟁 모드

```
  ○ 없음 (일반 기록)
  ● 개인전
  ○ 팀전
```

#### 섹션 5. 공개 설정 (WOD만)

```
  ● 박스 정책 자동 적용
      → 계산된 공개 예정: 2025-08-19 22:00 (Asia/Seoul)
  ○ 직접 설정
      [ 2025-08-19 ] [ 22:00 ]
  ○ 임시저장
```

#### 섹션 6. Named WOD & 태그 (템플릿 작성 시)

```
  Named WOD   [ Fran ▾ ]        (선택)
  태그         [ #benchmark × ]  [ #전신 × ]  [ + 추가 ]
```

#### 하단 고정 액션바

```
  (WOD)       [임시저장]   [저장 및 공개]
  (템플릿)                  [저장]
```

---

### 4.4 WOD 상세 화면
`/wods/:wodId`

```
250820 Part B - Fran                          [편집] [삭제]
For Time · 개인전
2025년 8월 20일  ·  아침 6시반 클래스 (Part 2)
출처: Fran 템플릿  ·  Named WOD: Fran (The Girls)
🟢 공개됨  (2025-08-19 22:00 공개)          [공개 설정 변경]
```

**탭**

| 탭 | 내용 |
|---|---|
| 📊 리더보드 | 순위표 |
| 📝 기록 관리 | 전체 기록 목록 + 수정 |
| 📈 통계 | 참여율, 평균, 완수율 |

---

### 4.5 리더보드 화면

#### 컨트롤바 (개인전)

```
성별: [전체] [남성] [여성]
그룹: [전체] [Rx'd] [scaled] [bg]
모드: [개인전 ●] [팀전 ○]                    [+ 기록 입력]
```

#### 개인전 테이블

```
#    이름           그룹       기록
──────────────────────────────────────────────
1    홍길동 (남)   Rx'd       8:00   🏆 PR   ✏️
2    김철수 (남)   Rx'd       9:32            ✏️
── scaled ────────────────────────────────────
1    박민수 (남)   scaled     7:45            ✏️
── bg ────────────────────────────────────────
1    윤도현 (남)   bg         6:10            ✏️
```

#### 컨트롤바 (팀전)

```
구성: [전체] [Men 🔵] [Women 🔴] [Mixed 🟣]
그룹: [전체] [Rx'd] [scaled] [bg]
모드: [개인전 ○] [팀전 ●]                    [+ 팀 기록 입력]
```

#### 팀전 테이블

```
#   팀 이름                    구성      그룹      기록
──────────────────────────────────────────────────────────────────
1   홍길동 · 김철수 · 이영희   2M/1W    Rx'd     14:50    ✏️
    👑홍길동(남) · 김철수(남) · 이영희(여)        ← 행 클릭 시 펼침
2   박민수 · 최지수            2M       Rx'd     15:20    ✏️
── scaled ────────────────────────────────────────────────────────
1   윤도현 · 강지혜            1M/1W    scaled   13:10    ✏️
```

> - `[전체]` 탭: 모든 팀 통합 순위 (gender_category와 무관하게 WOD 타입별 점수 기준)
> - `[Men]` / `[Women]` / `[Mixed]` 탭: 해당 카테고리 팀만 표시, 카테고리 내 독립 순위 재계산
> - 각 팀 행 클릭 시 팀원 목록 인라인 펼침 (성별 아이콘 + 역할 표시)

---

### 4.6 기록 입력 모달

#### 개인 기록

```
┌──────────────────────────────────────────────┐
│  기록 입력                              [×]  │
├──────────────────────────────────────────────┤
│  회원   [ 홍길동 검색...            ▾ ]      │
│  그룹   [Rx'd]  [scaled]  [bg]              │
│                                              │
│  ── For Time ─────────────────────────────  │
│  기록   [ 08 ] 분   [ 00 ] 초               │
│         □ DNF                               │
│                                              │
│  메모   [                           ]        │
│  영상   [                           ]        │
│                          [취소]  [저장]       │
└──────────────────────────────────────────────┘
```

> WOD 타입에 따라 기록 입력 영역 동적 렌더링

#### 팀 기록

```
┌──────────────────────────────────────────────┐
│  팀 기록 입력                           [×]  │
├──────────────────────────────────────────────┤
│  팀원 구성                                   │
│  [ 회원 검색 및 추가...               + ]    │
│  👑 홍길동 (남, 리더)                  [×]   │
│     김철수  (남)        [리더 지정]    [×]    │
│     이영희  (여)        [리더 지정]    [×]    │
│                                              │
│  구성: Mixed (2M/1W)   ← 자동 표시           │
│                                              │
│  팀 이름  [ 홍길동 · 김철수 · 이영희  ]      │
│           ↑ 팀원 추가 시 자동 생성, 수정 가능 │
│  그룹     [Rx'd]  [scaled]  [bg]            │
│                                              │
│  ── For Time ─────────────────────────────  │
│  기록   [ 14 ] 분   [ 50 ] 초               │
│         □ DNF                               │
│                          [취소]  [저장]       │
└──────────────────────────────────────────────┘
```

> - 팀원 추가/삭제 시 팀명 자동 갱신 (단, 코치가 직접 수정한 이후에는 자동 갱신 중단)
> - `구성` 표시는 팀원 성별 기반 실시간 계산 (저장 시 서버에서 최종 확정)

---

### 4.7 박스 WOD 설정 화면
`/settings/wod` (Head Coach 이상)

```
WOD 공개 정책
──────────────────────────────────────────────

박스 타임존   [ Asia/Seoul ▾ ]

● 박스 정책으로 자동 공개

  공개 기준
  ● 수업 시작 시각 기준
      수업 시작 [ 30 ] 분 전 공개

  ○ 특정 시각 기준
      수업일로부터  [ -1 ] 일   [ 22:00 ] 에 공개

○ 수동 공개 (코치가 직접 설정)

──────────────────────────────────────────────
※ 정책 변경은 이후 새로 배정되는 WOD부터 적용됩니다.

                                        [저장]
```

---

### 4.8 회원 기록 & 벤치마크
`/members/:memberId/records`

**탭**

| 탭 | 내용 |
|---|---|
| 1RM 기록 | 종목별 히스토리 그래프 + 테이블 |
| Named WOD | Fran, Murph 등 벤치마크 이력 비교 |
| 전체 WOD 기록 | 날짜별 타임라인 |

---

## 5. 비즈니스 규칙

### 5.1 공개 상태 판단

```sql
-- 공개 여부 (DB 컬럼 없음, 실시간 계산)
published_at IS NOT NULL AND published_at <= NOW()

-- publish_status (API 응답에서 계산)
CASE
  WHEN published_at IS NULL        THEN 'draft'
  WHEN published_at > NOW()        THEN 'scheduled'
  ELSE                                  'published'
END
```

### 5.2 리더보드 정렬

```
1. scale_group_id IS NULL (Rx'd) 최상단
2. scale_groups.groups 배열 index 순
3. WOD 타입별:
   for_time    → result_time_seconds ASC
   amrap       → (result_rounds DESC, result_reps DESC)
   emom        → result_reps DESC
   max_weight  → result_weight DESC
   custom      → 순위 없음 (created_at ASC)
4. DNF → 해당 그룹 내 최하단
```

### 5.3 PR 감지

| WOD 타입 | 조건 |
|---|---|
| For Time | `new_time < prev_best` (DNF 제외) |
| AMRAP | `(rounds, reps) > prev_best` (tuple 비교) |
| EMOM | `new_reps > prev_best` |
| Max Weight | `new_weight > prev_best` |
| Custom | 없음 |

> - 동일 `member_id + wod_id` 내 이전 기록 비교
> - Named WOD 통산 PR은 `named_wod_id` 기준으로 별도 집계

### 5.4 Template → WOD Snapshot 규칙

- deploy 시 template 전 필드 복사 + overrides 병합
- 이후 template 수정은 기배정 wod 무영향
- template 삭제 시 `wods.template_id → NULL` (wod 유지)

### 5.5 wod_type 변경 차단

- `wod_records` 1개라도 존재 → `wod_type` 변경 `409 Conflict`
- 프론트: 기록이 있으면 타입 선택 UI 비활성화

### 5.6 Scale Group id 변경 차단

- 해당 group id를 참조하는 `wod_records`가 존재하면 id 변경 불가
- 프론트: 기록이 있는 그룹 카드에 자물쇠 아이콘 표시

### 5.7 ClassInstance 연결 규칙

- 1개 수업에 복수 WOD 가능 (Part A / Part B)
- `display_order`로 수업 내 순서 보장
- ClassInstance 취소 시 WOD·기록 유지
- WOD 삭제 시 `wod_class_instances` cascade 삭제

### 5.8 Competition Mode 규칙

| mode | 기록 대상 | 팀 생성 |
|---|---|---|
| `none` | `wod_records` | 불가 |
| `individual` | `wod_records` | 불가 |
| `team` | `wod_teams` | 가능 |

### 5.9 팀명 자동 생성 규칙

- 팀 생성 시 `name = null`이면 서버에서 자동 생성
- 생성 형식: leader 이름 우선 + member 순으로 `" · "` 구분자로 이어붙임
- 예: `"홍길동 · 김철수 · 이영희"`
- 코치가 직접 수정한 이후에는 팀원 변경 시 자동 갱신 안 함 (프론트 플래그로 추적)

### 5.10 `gender_category` 자동 계산 규칙

- 팀원 전원 male → `men`
- 팀원 전원 female → `women`
- 혼성 → `mixed`
- 팀원 추가/삭제(PATCH) 시 서버에서 재계산하여 저장
- `gender_composition` (예: `"2M/1W"`)은 DB 저장 안 함, API 응답에서 실시간 계산

### 5.11 권한 매트릭스

| 기능 | Coach | Head Coach | Super Admin |
|---|---|---|---|
| WOD 은행 접근/생성/수정/삭제 | ❌ | ✅ | ✅ |
| WOD 조회/수정 | ✅ | ✅ | ✅ |
| WOD 등록 | ❌ | ✅ | ✅ |
| WOD 삭제 | ❌ | ✅ | ✅ |
| 공개 설정 변경 | ✅ | ✅ | ✅ |
| 박스 WOD 설정 변경 | ❌ | ✅ | ✅ |
| 타인 기록 입력/수정 | ✅ | ✅ | ✅ |
| 기록 삭제 | ✅ | ✅ | ✅ |
| 팀 생성/수정 | ✅ | ✅ | ✅ |
| 팀 삭제 | ✅ | ✅ | ✅ |
| 1RM 전체 입력 | ✅ | ✅ | ✅ |

---

## 6. 추후 고려사항

- **회원 앱**: 동일 API (`/api/v1/...`), 공개된 WOD만 노출 (`published_at <= NOW()`)
- **팀전 회원 앱**: 팀 참여 요청 → 리더 승인 플로우 별도 설계
- **Death by EMOM**: MVP는 Custom 우회. 추후 `emom_duration = NULL` + `result_reps = 실패 직전 분` 확장
- **PR 푸시 알림**: FCM / APNs
- **실시간 리더보드**: WebSocket 또는 SSE 방식 결정
- **리더보드 소셜**: `wod_reactions`, `wod_comments` 별도 설계
- **WOD 은행 마켓플레이스**: 박스 간 템플릿 공유 가능성 검토
