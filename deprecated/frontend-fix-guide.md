# 프론트엔드 수정 가이드 — 주간 정규 시간표

스펙(`prompt-frontend.md`, `weekly_schedule_spec_v2.html`)과 현재 구현을 대조하여 발견한 문제점 및 수정 방향을 정리합니다.

---

## 수정 항목 목록

| # | 파일 | 심각도 | 제목 |
|---|------|--------|------|
| 1 | `schedule.api.ts` | 🔴 높음 | 백엔드에 없는 API 엔드포인트 호출 |
| 2 | `ScheduleEditorPage.tsx` | 🔴 높음 | 배포 전 템플릿 저장 흐름 없음 |
| 3 | `ScheduleCalendarPage.tsx` | 🔴 높음 | 캘린더 API 파라미터 불일치 |
| 4 | `ScheduleCalendarPage.tsx` | 🔴 높음 | `instance.startTime` / `endTime` 존재하지 않는 필드 참조 |
| 5 | `scheduleDraft.store.ts` | 🟡 중간 | Zustand persist key 고정으로 draft 데이터 충돌 |
| 6 | `ScheduleEditorPage.tsx` | 🟡 중간 | LocalStorage 복원에 `window.confirm` 사용 |
| 7 | `ScheduleListPage.tsx` | 🟡 중간 | 삭제 버튼에 onClick 핸들러 없음 |
| 8 | `ScheduleListPage.tsx` | 🟡 중간 | DropdownMenu 미구현 ("기존 복사" 옵션 없음) |

---

## 상세 수정 내용

---

### 1. `schedule.api.ts` — 백엔드에 없는 엔드포인트 제거 및 재설계

**현재 문제**

아래 4개 함수가 호출하는 엔드포인트가 백엔드에 존재하지 않습니다.

```ts
// 존재하지 않는 엔드포인트들
getTemplate: GET /v1/admin/schedule-templates/{id}         // 단건 조회 없음
getRoutines: GET /v1/admin/schedule-templates/{id}/routines // 별도 라우트 없음
createRoutine: POST /v1/admin/schedule-templates/{id}/routines
deleteRoutine: DELETE /v1/admin/schedule-templates/{id}/routines/{routineId}
```

**백엔드 실제 API 구조**

백엔드는 템플릿 + 루틴을 단일 요청으로 처리합니다.

```
POST   /api/v1/admin/schedule-templates          → 템플릿 + 루틴 한 번에 생성 (DRAFT)
GET    /api/v1/admin/schedule-templates           → 목록 (boxId, statuses? 쿼리파람)
PUT    /api/v1/admin/schedule-templates/{id}      → 템플릿 + 루틴 전체 교체 수정 (DRAFT만)
DELETE /api/v1/admin/schedule-templates/{id}      → 삭제 (DRAFT만)
POST   /api/v1/admin/schedule-templates/{id}/deploy → 배포 확정
GET    /api/v1/admin/class-instances              → from, to (Instant) 쿼리파람
PATCH  /api/v1/admin/class-instances/{id}/cancel  → 휴강 토글
```

**수정 방향**

`schedule.api.ts`를 아래와 같이 재작성합니다.

```ts
export const schedulesApi = {
  getTemplates: (boxId: string, statuses?: ScheduleStatus[]) =>
    apiClient.get('/v1/admin/schedule-templates', { params: { boxId, statuses } })
      .then(r => r.data.data as IScheduleTemplate[]),

  // 신규 생성: 템플릿 + 루틴 한 번에
  createTemplate: (payload: ICreateTemplateRequest) =>
    apiClient.post('/v1/admin/schedule-templates', payload)
      .then(r => r.data.data as string), // 반환값: UUID

  // 수정: 템플릿 + 루틴 전체 교체
  updateTemplate: (id: string, payload: IUpdateTemplateRequest) =>
    apiClient.put(`/v1/admin/schedule-templates/${id}`, payload),

  deleteTemplate: (id: string) =>
    apiClient.delete(`/v1/admin/schedule-templates/${id}`),

  deployTemplate: (id: string) =>
    apiClient.post(`/v1/admin/schedule-templates/${id}/deploy`),

  getClassInstances: (boxId: string, from: string, to: string) =>
    apiClient.get('/v1/admin/class-instances', { params: { boxId, from, to } })
      .then(r => r.data.data as IClassInstance[]),

  cancelClassInstance: (id: string) =>
    apiClient.patch(`/v1/admin/class-instances/${id}/cancel`)
      .then(r => r.data.data as IClassInstance),
};
```

**Request/Response 타입 추가 (`schedule.types.ts`)**

```ts
interface IRoutineRequest {
  dayOfWeek: DayOfWeek;
  startTime: string; // ISO Instant (ex. "2026-06-01T06:00:00Z")
  endTime: string;   // ISO Instant
  capacity: number | null;
  coachIds: string[];
}

interface ICreateTemplateRequest {
  effectiveStartDate: string; // ISO Instant
  generateMonths: number;
  routines: IRoutineRequest[];
}

interface IUpdateTemplateRequest extends ICreateTemplateRequest {}
```

---

### 2. `ScheduleEditorPage.tsx` — 배포 전 템플릿 저장 흐름 추가

**현재 문제**

편집 페이지에서 저장(Draft 생성/수정) API 호출이 없습니다. 배포 버튼을 눌러도 routines가 서버에 반영되지 않습니다. `templateId === 'new'`인 상태에서 deploy를 호출하면 `/schedule-templates/new/deploy` 요청이 발생합니다.

**수정 방향**

배포 흐름을 2단계로 분리합니다.

```
[현재] 배포 버튼 클릭 → deploy API 바로 호출

[수정] 배포 버튼 클릭
        ↓
  templateId === 'new'?
  ├── YES → POST /schedule-templates (create) → 반환된 id로 templateId 갱신
  └── NO  → PUT /schedule-templates/{id} (update, 루틴 포함)
        ↓
  POST /schedule-templates/{id}/deploy
        ↓
  localStorage.removeItem(`schedule-draft:${templateId}`)
  navigate('/schedules')
```

**routines를 서버 포맷으로 변환하는 헬퍼**

```ts
// store의 IClassRoutine을 서버 요청 형식으로 변환
function toRoutineRequest(r: IClassRoutine, effectiveStartDate: string): IRoutineRequest {
  // startTime/endTime은 HH:mm 형식 → effectiveStartDate 날짜 기준 Instant로 변환
  const baseDate = dayjs(effectiveStartDate).format('YYYY-MM-DD');
  return {
    dayOfWeek: r.dayOfWeek,
    startTime: dayjs(`${baseDate}T${r.startTime}:00`).toISOString(),
    endTime:   dayjs(`${baseDate}T${r.endTime}:00`).toISOString(),
    capacity:  r.capacity,
    coachIds:  r.coaches.map(c => c.id),
  };
}
```

**수정 후 deploy 핸들러 예시**

```ts
const handleDeploy = async () => {
  const payload = {
    effectiveStartDate: meta.effectiveStartDate!,
    generateMonths: meta.generateMonths,
    routines: routines.map(r => toRoutineRequest(r, meta.effectiveStartDate!)),
  };

  let resolvedId = templateId;
  if (templateId === 'new') {
    resolvedId = await schedulesApi.createTemplate(payload); // UUID 반환
    setTemplateId(resolvedId);
    localStorage.removeItem('schedule-draft:new');
  } else {
    await schedulesApi.updateTemplate(templateId!, payload);
  }

  await schedulesApi.deployTemplate(resolvedId);
  localStorage.removeItem(`schedule-draft:${resolvedId}`);
  queryClient.invalidateQueries({ queryKey: ['schedule-templates'] });
  navigate('/schedules');
};
```

---

### 3. `ScheduleCalendarPage.tsx` — API 파라미터 수정

**현재 문제**

```ts
// 현재: year, month 전달 → 백엔드가 받지 못함
schedulesApi.getClassInstances({ boxId, year, month })
```

백엔드 컨트롤러는 `from: Instant, to: Instant` 파라미터를 기대합니다.

**수정 방향**

```ts
// month의 시작일과 종료일을 ISO Instant로 계산하여 전달
const from = currentDate.startOf('month').toISOString();
const to   = currentDate.endOf('month').toISOString();

const { data: instances } = useQuery({
  queryKey: ['class-instances', currentBoxId, from, to],
  queryFn: () => schedulesApi.getClassInstances(currentBoxId!, from, to),
  enabled: !!currentBoxId,
});
```

---

### 4. `ScheduleCalendarPage.tsx` — `IClassInstance` 타입에 없는 필드 참조 수정

**현재 문제**

`IClassInstance`에는 `startTime`, `endTime`, `coaches`, `capacity` 필드가 없습니다.

```ts
// 이 코드들 모두 런타임 오류 또는 undefined
.sort((a, b) => a.startTime.localeCompare(b.startTime))
instance.startTime, instance.endTime
instance.coaches?.[0]?.name
instance.capacity
```

**`IClassInstance` 타입 현황**

```ts
interface IClassInstance {
  id: string;
  routineId: string;
  classDate: string; // ISO Instant
  isCancelled: boolean;
  // startTime, endTime, coaches, capacity 없음
}
```

**수정 방향 (두 가지 옵션)**

**Option A** — 백엔드에서 ClassInstance 응답에 routine 정보 포함 요청 (권장)
- BE에게 `ClassInstanceResponseDto`에 `startTime`, `endTime`, `capacity`, `coaches` 필드 추가 요청
- 타입 정의 확장:

```ts
interface IClassInstance {
  id: string;
  routineId: string;
  classDate: string;
  isCancelled: boolean;
  // 추가 요청 필드
  startTime: string;  // ISO Instant
  endTime: string;    // ISO Instant
  capacity: number | null;
  coaches: ICoach[];
}
```

**Option B** — 프론트엔드에서 routineId로 로컬 매핑
- 별도로 routines를 조회해 `routineId`로 조인하여 표시 (복잡도 증가, 비권장)

**임시 처리 (Option A 대기 중)**

```ts
// 오류 방지를 위해 optional chaining 및 fallback 처리
.sort((a, b) => (a as any).startTime?.localeCompare((b as any).startTime) ?? 0)

// InstanceBlock 내에서
{(instance as any).startTime ?? dayjs(instance.classDate).format('HH:mm')}
```

---

### 5. `scheduleDraft.store.ts` — persist key를 templateId 기반으로 동적 설정

**현재 문제**

```ts
// 현재: 모든 템플릿이 동일한 LS 키를 사용
name: 'schedule-draft:current'
```

`useScheduleAutoSave`는 올바르게 `schedule-draft:{templateId}`에 저장하지만, Zustand persist는 항상 `schedule-draft:current`에 씁니다. 두 개의 저장소가 충돌합니다.

**수정 방향**

Zustand persist를 제거하고 수동 저장 방식으로 일원화합니다.

```ts
// persist 미들웨어 제거, 순수 store로 변경
export const useScheduleDraftStore = create<ScheduleDraftStore>()((set, get) => ({
  templateId: 'new',
  meta: { effectiveStartDate: null, generateMonths: 3 },
  routines: [],
  savedAt: null,
  // ... actions
}));
```

`useScheduleAutoSave`가 이미 올바른 키(`schedule-draft:{templateId}`)로 저장하므로, persist 미들웨어만 제거하면 충돌이 해소됩니다.

---

### 6. `ScheduleEditorPage.tsx` — LocalStorage 복원에 shadcn Toast 사용

**현재 문제**

```ts
// 현재: 브라우저 기본 confirm 다이얼로그
if (window.confirm(`${...}에 임시저장된 내용이 있습니다. 불러올까요?`))
```

**수정 방향**

shadcn `toast`와 action 버튼을 사용합니다.

```tsx
import { useToast } from '@/hooks/use-toast';

const { toast } = useToast();

useEffect(() => {
  const key = `schedule-draft:${templateId}`;
  const savedData = localStorage.getItem(key);
  if (!savedData) return;

  const { savedAt: lsSavedAt, meta: lsMeta, routines: lsRoutines } = JSON.parse(savedData);

  toast({
    title: `${dayjs(lsSavedAt).format('HH:mm')}에 임시저장된 내용이 있습니다.`,
    description: '불러올까요?',
    action: (
      <div className="flex gap-2">
        <ToastAction altText="불러오기" onClick={() => {
          setMeta(lsMeta);
          setRoutines(lsRoutines);
          setSavedAt(lsSavedAt);
        }}>
          불러오기
        </ToastAction>
        <ToastAction altText="무시" onClick={() => {
          localStorage.removeItem(key);
        }}>
          무시
        </ToastAction>
      </div>
    ),
  });
}, [templateId]);
```

---

### 7. `ScheduleListPage.tsx` — 삭제 버튼 onClick 핸들러 추가

**현재 문제**

```tsx
// 현재: onClick 없음
<Button variant="ghost" size="sm" disabled={!isEditable}>삭제</Button>
```

**수정 방향**

```tsx
import { useMutation, useQueryClient } from '@tanstack/react-query';

const queryClient = useQueryClient();

const deleteMutation = useMutation({
  mutationFn: (id: string) => schedulesApi.deleteTemplate(id),
  onSuccess: () => queryClient.invalidateQueries({ queryKey: ['schedule-templates'] }),
});

// 버튼
<Button
  variant="ghost"
  size="sm"
  className="text-destructive hover:text-destructive hover:bg-destructive/10"
  disabled={!isEditable}
  onClick={() => {
    if (window.confirm('이 시간표를 삭제할까요?')) {
      deleteMutation.mutate(row.original.id);
    }
  }}
>
  삭제
</Button>
```

> 필요시 `window.confirm` 대신 shadcn `AlertDialog`로 교체하세요.

---

### 8. `ScheduleListPage.tsx` — DropdownMenu로 생성 UX 개선

**현재 문제**

단일 버튼으로 항상 새 페이지로 이동합니다. "기존 시간표 복사" 기능이 없습니다.

**수정 방향**

```tsx
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

// 복사 시 기존 템플릿 선택 모달 상태
const [isCopyModalOpen, setIsCopyModalOpen] = useState(false);

<DropdownMenu>
  <DropdownMenuTrigger asChild>
    <Button>
      <Plus className="mr-2 h-4 w-4" />
      새 시간표 생성
    </Button>
  </DropdownMenuTrigger>
  <DropdownMenuContent align="end">
    <DropdownMenuItem onClick={() => navigate('/schedules/editor/new')}>
      새로 만들기
    </DropdownMenuItem>
    <DropdownMenuItem onClick={() => setIsCopyModalOpen(true)}>
      기존 시간표 복사
    </DropdownMenuItem>
  </DropdownMenuContent>
</DropdownMenu>
```

**"기존 시간표 복사" 흐름**

1. 모달에서 기존 템플릿 선택 (`templates` 목록 활용)
2. 선택 시 해당 템플릿의 routines를 draft store에 세팅 후 `/schedules/editor/new`로 이동
3. 에디터 진입 시 선택한 템플릿의 데이터가 채워진 상태로 시작

---

## 수정 우선순위 권장 순서

1. **[1] `schedule.api.ts` 재설계** — 연동 전체의 기반
2. **[3] 캘린더 API 파라미터 수정** — 독립적, 단순
3. **[2] 에디터 저장/배포 흐름** — api.ts 완료 후 진행
4. **[5] persist key 수정** — 데이터 충돌 방지
5. **[4] ClassInstance 타입/필드** — BE 협의 후 진행
6. **[7] 삭제 버튼**, **[8] DropdownMenu** — UI 완성도
7. **[6] Toast 복원 UX** — 마지막 polish
