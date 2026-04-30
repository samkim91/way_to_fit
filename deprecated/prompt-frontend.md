# 프론트엔드 AI 코드 생성 프롬프트
# 주간 정규 시간표 관리 (Weekly Schedule Template)

---

## 역할 및 기술 스택

당신은 React, shadcn/ui, Tailwind CSS, Zustand를 능숙하게 다루는 프론트엔드 개발자입니다.
CrossFit 박스 관리자용 **주간 정규 시간표(Weekly Schedule Template)** 편집 기능을 구현해야 합니다.

기술 스택:
- React (TypeScript)
- shadcn/ui + Tailwind CSS
- Zustand (상태 관리)
- React Hook Form + Zod (폼 검증)
- Zustand persist middleware (LocalStorage 연동)

---

## 1. 시간표 목록 페이지 (List Page)

### UI 컴포넌트
- shadcn `Table` 컴포넌트로 시간표 버전 목록을 렌더링하세요.
- 각 Row에는 다음 컬럼이 포함되어야 합니다:
  - 적용 시작일 (effectiveStartDate, Asia/Seoul 기준 날짜 포맷)
  - 상태 Badge (`DRAFT` / `SCHEDULED` / `ACTIVE` / `ARCHIVED`)
  - 주간 총 수업 수
  - 액션 버튼 (수정 / 삭제 — DRAFT 상태일 때만 활성화)

### 생성 UX
- 우측 상단 [새 시간표 생성] 버튼 클릭 시 shadcn `DropdownMenu`를 통해:
  - "새로 만들기" — 빈 편집 페이지로 이동
  - "기존 시간표 복사" — 복사할 버전 선택 모달 후 편집 페이지로 이동

---

## 2. 시간표 편집 페이지 (Editor Page)

### 레이아웃
- 월요일~일요일 7개 열(Column)로 구성된 주간 칸반 뷰
- 각 열의 빈 공간 클릭 또는 `+` 버튼 클릭 시 수업 추가 Dialog가 열립니다

### 상단 메타 입력
- shadcn `DatePicker` — effectiveStartDate (적용 시작일, 오늘 이후 날짜만 선택 가능)
- shadcn `NumberInput` — generateMonths (인스턴스 생성 개월 수, 1~12 범위)
- 두 입력은 나란히 배치

### 우상단 영역
- "마지막 임시저장: HH:mm" 텍스트 표시 (저장 없으면 미표시)
- 배포 확정 버튼 (아래 섹션 5 참고)

---

## 3. 수업 추가/편집 Dialog

shadcn `Dialog` 또는 `Sheet`를 사용하고, React Hook Form + Zod로 폼을 구성하세요.

### 입력 필드

| 필드 | 컴포넌트 | 비고 |
|------|----------|------|
| 요일 | 다중 선택 토글 버튼 그룹 | 한 번에 월/수/금 동시 선택 가능. 선택한 요일 수만큼 별도 ClassRoutine으로 생성됨 |
| 시작 시간 / 종료 시간 | HH:mm 텍스트 입력 또는 드롭다운 | |
| 담당 코치 | Multi-select 컴포넌트 | API에서 코치 목록 조회 후 렌더링 |
| 최대 정원 | NumberInput | "무제한" Checkbox 체크 시 disabled 처리, 서버로는 null 전송 |

### Zod 스키마 예시
```ts
const routineSchema = z.object({
  dayOfWeeks: z.array(z.enum(["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"])).min(1),
  startTime: z.string().regex(/^\d{2}:\d{2}$/),
  endTime: z.string().regex(/^\d{2}:\d{2}$/),
  coachIds: z.array(z.string().uuid()).min(1),
  capacity: z.number().int().positive().nullable(),
  isUnlimited: z.boolean(),
});
```

---

## 4. LocalStorage 임시저장 전략

### Zustand store 구성
```ts
// 편집 중인 시간표 전체 상태
interface ScheduleDraftStore {
  templateId: string | "new";
  meta: { effectiveStartDate: string | null; generateMonths: number };
  routines: ClassRoutine[];
  savedAt: string | null; // ISO Instant
  setMeta: (meta: Partial<Meta>) => void;
  addRoutine: (routine: ClassRoutine) => void;
  updateRoutine: (id: string, routine: ClassRoutine) => void;
  removeRoutine: (id: string) => void;
  clearDraft: () => void;
}
```

### LocalStorage 키 전략
- 기존 템플릿 편집: `schedule-draft:{templateId}`
- 신규 작성: `schedule-draft:new`
- Zustand persist middleware의 `name` 옵션을 동적으로 templateId 기준으로 설정
- 템플릿별 키가 분리되어 동시에 여러 draft를 작성해도 데이터가 섞이지 않습니다

### 자동 저장
- routines 또는 meta 변경 시 debounce 1초 후 LocalStorage 저장
- savedAt을 현재 Instant로 업데이트하여 "마지막 임시저장: HH:mm"에 표시

### 페이지 진입 시 복원 로직
1. 해당 키의 LocalStorage 데이터 존재 여부 확인
2. 존재하면 shadcn `Toast`: "HH:mm에 임시저장된 내용이 있습니다. 불러올까요?" [불러오기] [무시]
3. [무시] 선택 시 해당 키 즉시 삭제 (다음 진입 시 재표시 없음)

### 서버 저장 성공 후 정리
- deploy API 성공 응답 수신 시 해당 키 즉시 삭제
- 신규 작성(`schedule-draft:new`) 후 서버에서 templateId가 발급되면:
  - `schedule-draft:new` 키 삭제
  - 이후 해당 templateId 키로 신규 draft가 생성될 수 있도록 상태 갱신

---

## 5. 배포 확정 버튼 UX

effectiveStartDate 값에 따라 버튼 문구와 안내 텍스트를 동적으로 변경하세요.

| 상황 | 버튼 문구 | 버튼 하단 안내 텍스트 |
|------|-----------|----------------------|
| 날짜 미선택 | `배포 확정` (disabled) | 적용 시작일을 선택해주세요 |
| 오늘 날짜 선택 | `지금 바로 적용하기` | 저장 즉시 현재 시간표가 교체됩니다 |
| 미래 날짜 선택 | `N월 N일부터 적용 예정으로 저장` | 해당 날짜 자정에 자동으로 교체됩니다 |

### 오늘 날짜 선택 시 확인 모달
- 버튼 클릭 시 shadcn `AlertDialog`로 아래 내용 표시:

```
지금 바로 시간표를 교체할까요?

현재 운영 중인 시간표가 즉시 이 시간표로 교체되며,
기존에 예약된 수업이 취소될 수 있습니다.

취소된 예약은 회원에게 자동으로 알림이 발송됩니다.

[취소]  [교체하기]
```

- [취소]: 모달만 닫힘, API 호출 없음
- [교체하기]: `POST /api/v1/schedule-templates/{id}/deploy` 호출

### 미래 날짜 선택 시
- 확인 모달 없이 바로 `POST /api/v1/schedule-templates/{id}/deploy` 호출

---

## 6. 월간 캘린더 페이지 (Monthly View)

### UI 컴포넌트
- FullCalendar 라이브러리 또는 shadcn 캘린더 커스텀으로 월간 뷰 구성

### 상태 표시
- 정상 수업: 기본 색상 블록
- 휴강 처리된 수업: `text-decoration: line-through` + 회색/빨간 테마

### 휴강 액션
- 수업 블록 클릭 → shadcn `Popover` 표시
- Popover 내 "휴강 처리" 토글 버튼
- 토글 즉시 `PATCH /api/v1/class-instances/{id}/cancel` 호출
- 성공 시 캘린더 상태 즉시 갱신 (optimistic update 권장)

---

## 7. API 연동 타입 정의

```ts
// 시간표 버전
interface ScheduleTemplate {
  id: string;
  boxId: string;
  effectiveStartDate: string; // ISO Instant
  generateMonths: number;
  status: "DRAFT" | "SCHEDULED" | "ACTIVE" | "ARCHIVED";
  createdAt: string;
  updatedAt: string;
}

// 주간 수업 규칙
interface ClassRoutine {
  id: string;
  templateId: string;
  dayOfWeek: "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";
  startTime: string; // ISO Instant
  endTime: string;   // ISO Instant
  capacity: number | null; // null = 무제한
  coaches: Coach[];
}

// 실제 수업 인스턴스
interface ClassInstance {
  id: string;
  routineId: string;
  classDate: string; // ISO Instant
  isCancelled: boolean;
}
```