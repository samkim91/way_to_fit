# WOD & Leaderboard Frontend 구현 프롬프트

> 이 프롬프트는 AI 코드 생성 도구에 전달하여 WOD(Workout of the Day) & Leaderboard 프론트엔드를 구현하기 위한 self-contained 프롬프트입니다.

---

## 1. 프로젝트 컨텍스트

### 기술 스택
- **Framework**: React 19 + TypeScript 6.0
- **Build**: Vite 8.0
- **Package Manager**: pnpm
- **상태 관리**: Zustand 5.0 (persist middleware)
- **서버 상태**: TanStack React Query 5.97
- **폼**: React Hook Form 7.72 + Zod 4.3
- **UI**: shadcn/ui 4.2 (Radix UI + Tailwind CSS 4.2)
- **라우팅**: React Router DOM 7.14
- **HTTP**: Axios 1.15
- **아이콘**: Lucide React 1.8
- **날짜**: dayjs 1.11
- **테이블**: TanStack React Table 8.21

### 소스 루트
```
way-to-fit-frontend/src/
```

### Feature 모듈 구조
```
src/features/wod/
├── api/
│   └── wod.api.ts
├── components/
│   └── (UI 컴포넌트들)
├── hooks/
│   └── (React Query 훅들)
├── pages/
│   └── (페이지 컴포넌트들)
├── store/
│   └── wod.store.ts
├── types/
│   └── wod.types.ts
└── utils/
    └── wod.utils.ts
```

### Path Alias
`@/` → `./src/` (vite.config.ts에서 설정)

---

## 2. 기존 코드 패턴 레퍼런스

### 2.1 API 클라이언트 패턴

```typescript
// src/features/schedules/api/schedule.api.ts
import apiClient from '@/api/client';
import type { IScheduleTemplate } from '../types/schedule.types';

export const schedulesApi = {
  getTemplates: async (boxId: string, statuses?: ScheduleStatus[]) => {
    const response = await apiClient.get('/v1/admin/schedule-templates', {
      params: { boxId, statuses },
    });
    return response.data?.data as IScheduleTemplate[];
  },

  createTemplate: async (payload: ICreateTemplateRequest) => {
    const response = await apiClient.post('/v1/admin/schedule-templates', payload);
    return response.data?.data as string;
  },

  updateTemplate: async (id: string, payload: IUpdateTemplateRequest) => {
    const response = await apiClient.put(`/v1/admin/schedule-templates/${id}`, payload);
    return response.data;
  },

  deleteTemplate: async (id: string) => {
    const response = await apiClient.delete(`/v1/admin/schedule-templates/${id}`);
    return response.data;
  },
};
```

**API Client** (`src/api/client.ts`):
- Axios 인스턴스, Base URL: `VITE_API_BASE_URL`
- Request: `Authorization: Bearer {token}` 자동 주입
- Response: 401 시 RT reissue 자동 시도
- 모든 응답은 `{ code, message, data }` 구조

### 2.2 TypeScript 타입 패턴

```typescript
// src/features/schedules/types/schedule.types.ts
export type ScheduleStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED';

export interface IScheduleTemplate {
  id: string;
  boxId: string;
  effectiveStartDate: string;
  generateMonths: number;
  status: ScheduleStatus;
  createdAt: string;
  routinesCount: number;
}

export interface ICreateTemplateRequest {
  boxId: string;
  effectiveStartDate: string;
  generateMonths: number;
  routines: IRoutineRequest[];
}
```

### 2.3 Zustand 스토어 패턴

```typescript
// src/features/members/store/members.store.ts
import { create } from 'zustand';

interface MemberState {
  search: string;
  filters: string[];
  page: number;
  setSearch: (search: string) => void;
  setFilters: (filters: string[] | ((prev: string[]) => string[])) => void;
  resetFilters: () => void;
}

export const useMemberStore = create<MemberState>((set) => ({
  search: '',
  filters: [],
  page: 0,
  setSearch: (search) => set({ search }),
  setFilters: (filters) =>
    set((state) => ({
      filters: typeof filters === 'function' ? filters(state.filters) : filters,
    })),
  resetFilters: () => set({ search: '', filters: [], page: 0 }),
}));
```

### 2.4 React Query 패턴

```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

function MyPage() {
  const { currentBoxId } = useAuthStore();
  const queryClient = useQueryClient();

  const { data: items, isLoading } = useQuery({
    queryKey: ['feature-items', currentBoxId],
    queryFn: () => featureApi.getItems(currentBoxId!),
    enabled: !!currentBoxId,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => featureApi.deleteItem(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['feature-items'] });
    },
  });
}
```

### 2.5 페이지 컴포넌트 패턴

```typescript
import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Plus, Trash2 } from 'lucide-react';
import { useAuthStore } from '@/store/auth.store';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { AlertDialog, AlertDialogContent } from '@/components/ui/alert-dialog';
import type { ColumnDef } from '@tanstack/react-table';

export function FeatureListPage() {
  const { currentBoxId } = useAuthStore();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: items, isLoading } = useQuery({
    queryKey: ['items', currentBoxId],
    queryFn: () => api.getItems(currentBoxId!),
    enabled: !!currentBoxId,
  });

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold">Feature Title</h1>
        <Button onClick={() => navigate('/feature/new')}>
          <Plus className="mr-2 h-4 w-4" /> New
        </Button>
      </div>
      <DataTable columns={columns} data={items || []} isLoading={isLoading} />
    </div>
  );
}
```

### 2.6 폼 검증 패턴

```typescript
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';

const schema = z.object({
  name: z.string().min(1, '필수 입력입니다'),
  status: z.enum(['ACTIVE', 'DRAFT']),
});

export function MyForm() {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {},
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input {...register('name')} />
      {errors.name && <span>{errors.name.message}</span>}
    </form>
  );
}
```

### 2.7 사용 가능한 shadcn/ui 컴포넌트

```
button, badge, alert-dialog, dialog, input, label, checkbox,
select, toggle, toggle-group, card, table, popover, tabs,
calendar, dropdown-menu, separator, sheet, tooltip, textarea
```

Import 패턴: `import { Button } from '@/components/ui/button';`

### 2.8 공통 컴포넌트

- `RootLayout.tsx` — 메인 레이아웃
- `Header.tsx` — 상단 네비게이션
- `Sidebar.tsx` — 사이드 네비게이션
- `DataTable.tsx` — 재사용 테이블 (페이지네이션, 행 클릭)
- `SingleBoxSelector.tsx` — 박스 선택기
- `RoleGuard.tsx` — 역할 기반 접근 제어

### 2.9 라우팅 패턴

```typescript
// src/App.tsx
<Routes>
  <Route path="/login" element={<LoginPage />} />
  <Route element={<RootLayout />}>
    <Route path="/" element={<DashboardPage />} />
    <Route path="/members" element={<MemberListPage />} />
    <Route path="/members/:memberId" element={<MemberDetailPage />} />
    {/* WOD 라우트 추가 위치 */}
  </Route>
</Routes>
```

### 2.10 인증 스토어

```typescript
import { useAuthStore } from '@/store/auth.store';

const { user, currentBoxId, currentRole } = useAuthStore();
// currentRole: 'SUPER_ADMIN' | 'OWNER' | 'HEAD_COACH' | 'COACH' | 'MEMBER' | 'JUDGE'
```

### 2.11 유틸리티

```typescript
import { formatDate, formatDateTime, fromNow } from '@/utils/date';
import dayjs from 'dayjs';

formatDate('2026-04-13T00:00:00Z');     // "2026.04.13"
formatDateTime('2026-04-13T10:30:00Z'); // "2026.04.13 10:30"
```

---

## 3. 화면 명세

### 3.1 WOD 캘린더 화면
**라우트**: `/wods`

**레이아웃**: 월간 캘린더 (좌 70%) + 선택 날짜 사이드패널 (우 30%)

#### 캘린더 날짜 셀

```
  20
  파랑 dot: 수업 연동 WOD 수
  회색 dot: 독립 WOD 수
  시계 아이콘: 예약 공개 대기 WOD 있을 때
```

#### 선택 날짜 사이드패널

```
[ 2025년 8월 20일 수요일 ]

 ┌─ 아침 6시반 클래스 ─────────────────────────┐
 │ Part A  백스쿼트 1RM       [Max Weight] 공개됨  │
 │ Part B  250820 Fran        [For Time]   공개됨  │
 │ 참여 18명  Rx'd 8 · scaled 7 · bg 3         │
 │ PR 3명                                       │
 │                       [리더보드] [편집]      │
 └─────────────────────────────────────────────┘

 ┌─ 독립 WOD ──────────────────────────────────┐
 │ 오픈짐 WOD                [AMRAP]      임시저장│
 │                       [리더보드] [편집]      │
 └─────────────────────────────────────────────┘

 [+ 이 날에 WOD 추가]
```

> - 수업 내 Part A/B는 `display_order` 순으로 들여쓰기
> - 공개 상태 배지: 공개됨(green), 예약 공개(시계), 임시저장(pencil)

#### 빠른 WOD 추가 플로우

`[+ 이 날에 WOD 추가]` 클릭 → 인라인 선택:
- "WOD 은행에서 가져오기" → 은행 검색 모달 → 배정 슬라이드오버
- "직접 작성" → WOD 작성 폼으로 이동

### 3.2 WOD 은행 화면
**라우트**: `/wod-templates` (Head Coach 이상)

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
│        [배정하기]   [편집]   [복제]           │
└──────────────────────────────────────────────┘
```

#### 배정 슬라이드오버

`[배정하기]` 클릭 → 우측 슬라이드오버 (Sheet 컴포넌트):

```
┌──────────────────────────────────────────────┐
│  Fran 배정                             [×]   │
├──────────────────────────────────────────────┤
│  날짜   [ 2025-08-20              📅 ]       │
│                                              │
│  수업 연결                       (선택)       │
│  ☑ 아침 6시반  07:00   Part [ 1 ▾ ]         │
│  ☐ 오후 5시    17:00   Part [ 1 ▾ ]         │
│  ☐ 저녁 7시    19:00   경고: Part 1 이미 배정  │
│  ○ 수업 연결 안 함                            │
│                                              │
│  공개 시점                                   │
│  ● 박스 정책 자동 적용                        │
│    → 전날 (8/19) 22:00 공개 예정             │
│  ○ 직접 설정  [ 2025-08-19 ] [ 22:00 ]      │
│  ○ 임시저장                                  │
│                                              │
│  ▶ 내용 수정하기          ← 기본 접힘        │
│               [취소]   [배정 완료]            │
└──────────────────────────────────────────────┘
```

> - 날짜 선택 시 해당 날짜의 ClassInstance API 조회
> - Part 번호 이미 배정 시 경고 표시
> - "내용 수정하기" 펼치면 name, description 등 override 가능
> - "박스 정책 자동 적용" 시 계산된 예정 시각 미리보기

### 3.3 WOD 작성/수정 폼
**라우트**: `/wods/new` | `/wods/:wodId/edit` | `/wod-templates/new` | `/wod-templates/:id/edit`
(날짜·수업·공개 섹션은 WOD 작성 시만 표시, 동일 컴포넌트 재사용)

#### 섹션 1. 기본 정보

| 필드 | UI | 비고 |
|---|---|---|
| 날짜 | Date Picker (Calendar) | WOD만 |
| 수업 연결 | 체크박스 + Part 번호 Select | WOD만 |
| WOD 이름 | Input | 필수 |
| 설명 | Textarea | Rx'd 기준 |
| 영상 링크 | Input (URL) | 선택 |

> 날짜 선택 시 ClassInstance 조회. 수업 없으면 "독립 WOD로 등록됩니다" 안내

#### 섹션 2. 측정 타입

```
[ For Time ] [ AMRAP ] [ EMOM ] [ Max Weight ] [ Custom ]

For Time:     Time Cap [ 20 ] 분   체크박스: 제한 없음
AMRAP:        총 시간  [ 12 ] 분
EMOM:         총 시간  [ 10 ] 분   체크박스: 무제한 (Death by)
              목표 Reps [ 150 ]    선택
Max Weight:   단위 ● kg ○ lb
Custom:       타입 레이블 Input
```

> 기록 존재 시 타입 선택 UI 전체 비활성화 + "기록이 있는 WOD의 타입은 변경할 수 없습니다" 안내

#### 섹션 3. 스케일 그룹

```
Rx'd는 WOD 설명 기준 (별도 등록 불필요)

┌── scaled ───────────────┐  ┌── bg ─────────────────┐
│ id: scaled              │  │ id: bg                │
│ 설명: [30/20kg, 밴드...]│  │ 설명: [20/15kg, 링로우]│
│                    [×]  │  │                  [×]  │
└─────────────────────────┘  └───────────────────────┘
[+ 그룹 추가]

※ 카드 드래그로 순서 변경 가능
※ 기록이 있는 그룹의 id 변경 불가 (자물쇠 아이콘)
```

#### 섹션 4. 경쟁 모드

```
○ 없음 (일반 기록)  ● 개인전  ○ 팀전
```

#### 섹션 5. 공개 설정 (WOD만)

```
● 박스 정책 자동 적용 → 계산된 시각 미리보기
○ 직접 설정 → DatePicker + TimePicker
○ 임시저장
```

#### 섹션 6. Named WOD & 태그 (템플릿만)

```
Named WOD   [ Select ▾ ]
태그         [ Badge × ] [ + 추가 ]
```

#### 하단 고정 액션바

```
(WOD)       [임시저장]   [저장 및 공개]
(템플릿)                  [저장]
```

### 3.4 WOD 상세 화면
**라우트**: `/wods/:wodId`

```
250820 Part B - Fran                          [편집] [삭제]
For Time · 개인전
2025년 8월 20일 · 아침 6시반 클래스 (Part 2)
출처: Fran 템플릿 · Named WOD: Fran (The Girls)
공개됨 (2025-08-19 22:00 공개)                [공개 설정 변경]
```

**탭 (Tabs 컴포넌트)**:

| 탭 | 내용 |
|---|---|
| 리더보드 | 순위표 (3.5 참고) |
| 기록 관리 | 전체 기록 목록 + 수정 |
| 통계 | 참여율, 평균, 완수율 |

### 3.5 리더보드 화면

#### 컨트롤바 (개인전)

```
성별: [전체] [남성] [여성]           ← ToggleGroup
그룹: [전체] [Rx'd] [scaled] [bg]   ← ToggleGroup
모드: [개인전 ●] [팀전 ○]           ← ToggleGroup
                                    [+ 기록 입력]  ← Button
```

#### 개인전 테이블

```
#    이름           그룹       기록
────────────────────────────────────────────
1    홍길동 (남)   Rx'd       8:00   PR 뱃지   편집버튼
2    김철수 (남)   Rx'd       9:32              편집버튼
── scaled ────────────────────────────────
1    박민수 (남)   scaled     7:45              편집버튼
```

#### 컨트롤바 (팀전)

```
구성: [전체] [Men] [Women] [Mixed]
그룹: [전체] [Rx'd] [scaled] [bg]
모드: [개인전 ○] [팀전 ●]           [+ 팀 기록 입력]
```

#### 팀전 테이블

```
#   팀 이름                    구성      그룹      기록
──────────────────────────────────────────────────────
1   홍길동 · 김철수 · 이영희   2M/1W    Rx'd     14:50    편집
    행 클릭 → 팀원 목록 인라인 펼침
```

> - `[전체]` 탭: 모든 팀 통합 순위
> - `[Men]`/`[Women]`/`[Mixed]` 탭: 카테고리 내 독립 순위

### 3.6 기록 입력 모달

#### 개인 기록 (Dialog 컴포넌트)

```
┌──────────────────────────────────────────────┐
│  기록 입력                              [×]  │
├──────────────────────────────────────────────┤
│  회원   [ 검색 Select...             ▾ ]     │
│  그룹   [Rx'd]  [scaled]  [bg]   ← Toggle   │
│                                              │
│  ── For Time ─────────────────────────────  │
│  기록   [ 08 ] 분   [ 00 ] 초               │
│         체크박스 DNF                         │
│                                              │
│  메모   [ Textarea                  ]        │
│  영상   [ Input URL                 ]        │
│                          [취소]  [저장]       │
└──────────────────────────────────────────────┘
```

> WOD 타입에 따라 기록 입력 영역 동적 렌더링:
> - For Time: 분/초 Input
> - AMRAP: 라운드 Input + 추가 rep Input
> - EMOM: 누적 rep Input (목표 대비 진행률 표시)
> - Max Weight: 중량 Input
> - Custom: 자유 텍스트 Textarea

#### 팀 기록 (Dialog 컴포넌트)

```
┌──────────────────────────────────────────────┐
│  팀 기록 입력                           [×]  │
├──────────────────────────────────────────────┤
│  팀원 구성                                   │
│  [ 회원 검색 및 추가...               + ]    │
│  리더 홍길동 (남)                      [×]   │
│     김철수  (남)        [리더 지정]    [×]    │
│     이영희  (여)        [리더 지정]    [×]    │
│                                              │
│  구성: Mixed (2M/1W)   ← 자동 계산 표시      │
│                                              │
│  팀 이름  [ 홍길동 · 김철수 · 이영희  ]      │
│           ↑ 팀원 추가 시 자동 생성, 수정 가능 │
│  그룹     [Rx'd]  [scaled]  [bg]             │
│                                              │
│  (WOD 타입별 기록 입력 영역)                  │
│                          [취소]  [저장]       │
└──────────────────────────────────────────────┘
```

> - 팀원 추가/삭제 시 팀명 자동 갱신 (코치 직접 수정 후 자동 갱신 중단)
> - 구성 표시는 실시간 계산

### 3.7 박스 WOD 설정 화면
**라우트**: `/settings/wod` (Head Coach 이상)

```
WOD 공개 정책
──────────────────────────────────────

박스 타임존   [ Select: Asia/Seoul ▾ ]

● 박스 정책으로 자동 공개

  공개 기준
  ● 수업 시작 시각 기준
      수업 시작 [ Input: 30 ] 분 전 공개

  ○ 특정 시각 기준
      수업일로부터 [ Input: -1 ] 일  [ Input: 22:00 ] 에 공개

○ 수동 공개 (코치가 직접 설정)

※ 정책 변경은 이후 새로 배정되는 WOD부터 적용됩니다.

                                        [저장]
```

### 3.8 회원 기록 & 벤치마크
**라우트**: `/members/:memberId/records`

**탭 (Tabs)**:

| 탭 | 내용 |
|---|---|
| 1RM 기록 | 종목별 히스토리 그래프 + 테이블 |
| Named WOD | Fran, Murph 등 벤치마크 이력 비교 |
| 전체 WOD 기록 | 날짜별 타임라인 |

---

## 4. TypeScript 타입 정의

아래 타입을 `src/features/wod/types/wod.types.ts`에 정의하세요.

### 4.1 Enum 타입

```typescript
export type WodType = 'for_time' | 'amrap' | 'emom' | 'max_weight' | 'custom';
export type PublishPolicy = 'manual' | 'scheduled';
export type PublishAnchor = 'class_start' | 'fixed_time_of_day';
export type PublishStatus = 'draft' | 'scheduled' | 'published';
export type CompetitionMode = 'none' | 'individual' | 'team';
export type ResultStatus = 'completed' | 'dnf';
export type WeightUnit = 'kg' | 'lb';
export type GenderCategory = 'men' | 'women' | 'mixed';
export type TeamMemberRole = 'leader' | 'member';
export type NamedWodCategory = 'the_girls' | 'hero' | 'other';
export type Gender = 'male' | 'female';
```

### 4.2 도메인 타입

```typescript
export interface IScaleGroup {
  id: string;
  description: string;
}

export interface IScaleGroups {
  groups: IScaleGroup[];
}

export interface IWodSettings {
  timezone: string;
  publishPolicy: PublishPolicy;
  publishAnchor: PublishAnchor;
  publishOffsetMinutes?: number;
  publishFixedTime?: string;
  publishFixedDayOffset?: number;
}

export interface IWodTemplate {
  id: string;
  boxId: string;
  coachId: string;
  name: string;
  description?: string;
  imageUrls?: string[];
  videoUrl?: string;
  wodType: WodType;
  customTypeLabel?: string;
  timeCap?: number;
  amrapDuration?: number;
  emomDuration?: number;
  emomTargetReps?: number;
  weightUnit?: WeightUnit;
  scaleGroups: IScaleGroups;
  competitionMode: CompetitionMode;
  namedWodId?: string;
  tags?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface IWod {
  id: string;
  boxId: string;
  coachId: string;
  templateId?: string;
  date: string;
  name: string;
  description?: string;
  imageUrls?: string[];
  videoUrl?: string;
  wodType: WodType;
  customTypeLabel?: string;
  timeCap?: number;
  amrapDuration?: number;
  emomDuration?: number;
  emomTargetReps?: number;
  weightUnit?: WeightUnit;
  scaleGroups: IScaleGroups;
  competitionMode: CompetitionMode;
  namedWodId?: string;
  publishedAt?: string;
  publishStatus: PublishStatus;
  recordCount: number;
  classInstances: IWodClassInstance[];
  createdAt: string;
  updatedAt: string;
}

export interface IWodClassInstance {
  id: string;
  startTime: string;
  routineName: string;
  displayOrder: number;
}

export interface IWodsByDate {
  date: string;
  wods: IWod[];
}

export interface IWodRecord {
  id: string;
  wodId: string;
  memberId: string;
  memberName: string;
  memberGender: Gender;
  scaleGroupId?: string;
  resultTimeSeconds?: number;
  resultRounds?: number;
  resultReps?: number;
  resultWeight?: number;
  resultCustom?: string;
  resultStatus: ResultStatus;
  resultDisplay: string;
  memo?: string;
  videoUrl?: string;
  isPr: boolean;
  recordedBy: string;
  createdAt: string;
}

export interface IWodTeam {
  id: string;
  wodId: string;
  name: string;
  genderCategory: GenderCategory;
  genderComposition: string;
  scaleGroupId?: string;
  members: IWodTeamMember[];
  resultTimeSeconds?: number;
  resultRounds?: number;
  resultReps?: number;
  resultWeight?: number;
  resultCustom?: string;
  resultStatus: ResultStatus;
  resultDisplay: string;
}

export interface IWodTeamMember {
  memberId: string;
  name: string;
  gender: Gender;
  role: TeamMemberRole;
}

export interface ILeaderboardEntry {
  rank: number;
  member?: { id: string; name: string; gender: Gender; avatarUrl?: string };
  team?: IWodTeam;
  scaleGroupId?: string;
  scaleGroupLabel: string;
  resultDisplay: string;
  resultTimeSeconds?: number;
  resultRounds?: number;
  resultReps?: number;
  resultWeight?: number;
  isPr?: boolean;
}

export interface ILeaderboardResponse {
  wod: IWod;
  mode: 'individual' | 'team';
  leaderboard: ILeaderboardEntry[];
  totalCount: number;
}

export interface INamedWod {
  id: string;
  name: string;
  category: NamedWodCategory;
  description: string;
  defaultWodType: WodType;
}

export interface IBenchmarkEntry {
  namedWod: INamedWod;
  pr?: { resultDisplay: string; date: string; scaleGroupId?: string };
  records: Array<{
    wodId: string;
    date: string;
    resultDisplay: string;
    scaleGroupId?: string;
    isPr?: boolean;
  }>;
}
```

### 4.3 Request 타입

```typescript
export interface ICreateWodTemplateRequest {
  name: string;
  description?: string;
  imageUrls?: string[];
  videoUrl?: string;
  wodType: WodType;
  customTypeLabel?: string;
  timeCap?: number;
  amrapDuration?: number;
  emomDuration?: number;
  emomTargetReps?: number;
  weightUnit?: WeightUnit;
  scaleGroups: IScaleGroups;
  competitionMode: CompetitionMode;
  namedWodId?: string;
  tags?: string[];
}

export interface IDeployTemplateRequest {
  date: string;
  classInstanceIds: Array<{ classInstanceId: string; displayOrder: number }>;
  publishedAt: 'auto' | string | null;
  overrides?: Partial<ICreateWodTemplateRequest>;
}

export interface ICreateWodRequest extends ICreateWodTemplateRequest {
  date: string;
  classInstanceIds: Array<{ classInstanceId: string; displayOrder: number }>;
  publishedAt: 'auto' | string | null;
}

export interface ICreateWodRecordRequest {
  memberId: string;
  scaleGroupId?: string;
  resultTimeSeconds?: number;
  resultRounds?: number;
  resultReps?: number;
  resultWeight?: number;
  resultCustom?: string;
  resultStatus: ResultStatus;
  memo?: string;
  videoUrl?: string;
}

export interface ICreateWodTeamRequest {
  name?: string;
  scaleGroupId?: string;
  members: Array<{ memberId: string; role: TeamMemberRole }>;
  resultTimeSeconds?: number;
  resultRounds?: number;
  resultReps?: number;
  resultWeight?: number;
  resultCustom?: string;
  resultStatus: ResultStatus;
}

export interface IUpdateWodSettingsRequest {
  timezone: string;
  publishPolicy: PublishPolicy;
  publishAnchor?: PublishAnchor;
  publishOffsetMinutes?: number;
  publishFixedTime?: string;
  publishFixedDayOffset?: number;
}
```

---

## 5. 구현 단계

### Phase 1: 기초 인프라

**생성 파일:**
```
src/features/wod/
├── types/
│   └── wod.types.ts              # 4장의 모든 타입
├── api/
│   └── wod.api.ts                # API 클라이언트 (모든 엔드포인트)
├── store/
│   └── wod.store.ts              # 캘린더 선택 날짜, 필터 상태 등
└── utils/
    └── wod.utils.ts              # 결과 포매팅, 시간 변환 유틸
```

**wod.api.ts 구현할 메서드:**
```typescript
export const wodApi = {
  // Settings
  getSettings: (boxId: string) => ...,
  updateSettings: (boxId: string, payload: IUpdateWodSettingsRequest) => ...,

  // Templates
  getTemplates: (boxId: string, params?: { q?, wodType?, namedWodId?, tags?, page?, limit? }) => ...,
  getTemplate: (boxId: string, templateId: string) => ...,
  createTemplate: (boxId: string, payload: ICreateWodTemplateRequest) => ...,
  updateTemplate: (boxId: string, templateId: string, payload: Partial<ICreateWodTemplateRequest>) => ...,
  deleteTemplate: (boxId: string, templateId: string) => ...,
  deployTemplate: (boxId: string, templateId: string, payload: IDeployTemplateRequest) => ...,

  // WODs
  getWods: (boxId: string, params?: { date?, from?, to?, publishStatus?, competitionMode?, namedWodId? }) => ...,
  getWod: (boxId: string, wodId: string) => ...,
  createWod: (boxId: string, payload: ICreateWodRequest) => ...,
  updateWod: (boxId: string, wodId: string, payload: Partial<ICreateWodRequest>) => ...,
  deleteWod: (boxId: string, wodId: string) => ...,
  updateClassInstances: (boxId: string, wodId: string, payload: { add?, remove? }) => ...,
  updatePublish: (boxId: string, wodId: string, publishedAt: string | null) => ...,

  // Records
  createRecord: (boxId: string, wodId: string, payload: ICreateWodRecordRequest) => ...,
  updateRecord: (boxId: string, wodId: string, recordId: string, payload: Partial<ICreateWodRecordRequest>) => ...,
  deleteRecord: (boxId: string, wodId: string, recordId: string) => ...,

  // Teams
  createTeam: (boxId: string, wodId: string, payload: ICreateWodTeamRequest) => ...,
  updateTeam: (boxId: string, wodId: string, teamId: string, payload: Partial<ICreateWodTeamRequest>) => ...,
  deleteTeam: (boxId: string, wodId: string, teamId: string) => ...,

  // Leaderboard
  getLeaderboard: (boxId: string, wodId: string, params?: { gender?, scaleGroupId?, mode?, genderCategory? }) => ...,

  // Named WODs & Benchmark
  getNamedWods: () => ...,
  getMemberBenchmark: (boxId: string, memberId: string) => ...,
};
```

**wod.store.ts:**
```typescript
interface WodState {
  selectedDate: string;          // 캘린더 선택 날짜 (YYYY-MM-DD)
  calendarMonth: string;         // 현재 보고 있는 월 (YYYY-MM)
  templateSearch: string;        // 은행 검색어
  templateFilters: { wodType?: WodType[]; namedWodId?: string; tags?: string[] };
  setSelectedDate: (date: string) => void;
  setCalendarMonth: (month: string) => void;
  setTemplateSearch: (search: string) => void;
  setTemplateFilters: (filters: ...) => void;
  resetTemplateFilters: () => void;
}
```

**wod.utils.ts:**
```typescript
// 결과 포매팅
export function formatResult(wod: IWod, record: IWodRecord): string
// 예: For Time 480초 → "8:00", Max Weight 102.5 → "102.5kg"

// 공개 상태 계산
export function getPublishStatus(publishedAt?: string): PublishStatus

// gender_composition 계산
export function calcGenderComposition(members: IWodTeamMember[]): string
// 예: "2M/1W"
```

### Phase 2: WOD 캘린더 화면

**생성 파일:**
```
src/features/wod/
├── pages/
│   └── WodCalendarPage.tsx
└── components/
    ├── WodCalendar.tsx           # 월간 캘린더 (셀 dot, 클릭 이벤트)
    ├── WodDayPanel.tsx           # 선택 날짜 사이드패널
    ├── WodDayCard.tsx            # 수업별 또는 독립 WOD 카드
    ├── PublishBadge.tsx          # 공개 상태 배지 (green/clock/pencil)
    └── WodAddDialog.tsx          # "WOD 은행에서 가져오기" / "직접 작성" 선택
```

**라우트 추가** (App.tsx):
```typescript
<Route path="/wods" element={<WodCalendarPage />} />
```

### Phase 3: WOD 은행 + 작성/수정 폼

**생성 파일:**
```
src/features/wod/
├── pages/
│   ├── WodTemplateListPage.tsx
│   ├── WodTemplateFormPage.tsx   # new + edit 공용
│   └── WodFormPage.tsx           # WOD 직접 작성/수정 (new + edit 공용)
└── components/
    ├── WodTemplateCard.tsx       # 은행 카드
    ├── WodTemplateFilter.tsx     # 검색 & 필터 사이드바
    ├── DeploySheet.tsx           # 배정 슬라이드오버 (Sheet)
    ├── WodForm.tsx               # 공통 WOD 폼 (템플릿/WOD 공용 재사용)
    ├── WodTypeSelector.tsx       # 측정 타입 선택 + 타입별 필드
    ├── ScaleGroupEditor.tsx      # 스케일 그룹 카드 편집기 (드래그 순서)
    ├── CompetitionModeSelector.tsx
    ├── PublishSettingSelector.tsx # 공개 설정 (자동/직접/임시저장)
    └── NamedWodTagEditor.tsx     # Named WOD Select + 태그 편집
```

**라우트 추가:**
```typescript
<Route path="/wod-templates" element={<WodTemplateListPage />} />
<Route path="/wod-templates/new" element={<WodTemplateFormPage />} />
<Route path="/wod-templates/:templateId/edit" element={<WodTemplateFormPage />} />
<Route path="/wods/new" element={<WodFormPage />} />
<Route path="/wods/:wodId/edit" element={<WodFormPage />} />
```

### Phase 4: WOD 상세 + 리더보드 + 기록 입력

**생성 파일:**
```
src/features/wod/
├── pages/
│   └── WodDetailPage.tsx
└── components/
    ├── LeaderboardTab.tsx        # 리더보드 탭 내용
    ├── LeaderboardControls.tsx   # 필터 컨트롤바
    ├── IndividualLeaderboard.tsx # 개인전 테이블
    ├── TeamLeaderboard.tsx       # 팀전 테이블
    ├── RecordManagementTab.tsx   # 기록 관리 탭
    ├── StatsTab.tsx              # 통계 탭
    ├── RecordInputDialog.tsx     # 개인 기록 입력 모달
    ├── TeamRecordInputDialog.tsx # 팀 기록 입력 모달
    ├── RecordResultInput.tsx     # WOD 타입별 결과 입력 (동적 렌더링)
    └── MemberSearchSelect.tsx    # 회원 검색 Select
```

**라우트 추가:**
```typescript
<Route path="/wods/:wodId" element={<WodDetailPage />} />
```

### Phase 5: 설정 + 벤치마크

**생성 파일:**
```
src/features/wod/
├── pages/
│   └── WodSettingsPage.tsx
└── components/
    ├── WodSettingsForm.tsx        # 공개 정책 설정 폼
    ├── MemberBenchmarkTab.tsx     # 회원 상세 > Named WOD 벤치마크 탭
    ├── Member1rmTab.tsx           # 회원 상세 > 1RM 기록 탭
    └── MemberWodHistoryTab.tsx    # 회원 상세 > 전체 WOD 기록 타임라인
```

**라우트 추가:**
```typescript
<Route path="/settings/wod" element={<WodSettingsPage />} />
<Route path="/members/:memberId/records" element={<MemberRecordsPage />} />
```

---

## 6. 주요 구현 포인트

### 6.1 캘린더 월간 데이터 로딩
- 월 변경 시 `GET /wods?from=YYYY-MM-01&to=YYYY-MM-31` 호출
- React Query `queryKey: ['wods', boxId, calendarMonth]`

### 6.2 WOD 타입별 동적 렌더링
- `RecordResultInput` 컴포넌트는 `wodType` prop에 따라 다른 입력 필드 렌더링
- switch/case 또는 맵 패턴 활용

### 6.3 팀 기록 UX
- 팀원 추가 시 팀명 자동 생성 (`leader · member1 · member2`)
- 코치가 팀명을 직접 수정하면 `isNameManuallyEdited` 로컬 플래그 true
- 이후 팀원 변경 시 자동 갱신 중단

### 6.4 역할 기반 접근 제어
- `RoleGuard` 컴포넌트로 Head Coach 이상만 접근 가능한 화면 보호
- 조건부 렌더링: `currentRole`에 따라 버튼/메뉴 표시/숨김

### 6.5 낙관적 업데이트 (선택)
- 기록 삭제 등 빈번한 작업은 `useMutation`의 `onMutate`로 낙관적 업데이트 고려

### 6.6 shadcn 컴포넌트 활용 가이드
| UI 요소 | shadcn 컴포넌트 |
|---|---|
| 캘린더 | `Calendar` |
| 배정 슬라이드오버 | `Sheet` |
| 기록 입력 | `Dialog` |
| 필터 토글 | `ToggleGroup` |
| 상태 배지 | `Badge` |
| 탭 네비게이션 | `Tabs` |
| 확인 다이얼로그 | `AlertDialog` |
| 드롭다운 | `Select` 또는 `DropdownMenu` |
| 폼 입력 | `Input`, `Textarea`, `Checkbox` |
| 날짜 선택 | `Calendar` + `Popover` |
