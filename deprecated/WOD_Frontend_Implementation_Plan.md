# WOD Frontend Implementation Plan

## Goal

`way-to-fit-frontend/src/features/wod`를 `WOD_Feature_Spec_v3.0.md`와 현재 백엔드 API 계약에 맞게 완성한다.

현재까지는 다음이 반영된 상태다.

- `currentBoxId` / 권한 연결
- `WodForm`의 기본 리팩토링
- `ScaleGroup` 모델 정리
- `WOD 직접 생성` 최소 연결
- `Named WOD` / 태그 입력 일부 반영
- API 응답 정규화 일부 반영

아직 스펙 대비 미완성인 기능이 많아서, 아래 순서로 단계적으로 마무리한다.

---

## Phase 1. API Contract Alignment

목표: 프런트 타입과 API 요청/응답 구조를 백엔드 controller/DTO 기준으로 완전히 맞춘다.

### Tasks

1. `wod.api.ts` 응답 정규화 범위를 확장한다.
   - 템플릿 상세 응답
   - WOD 상세 응답
   - leaderboard 응답
   - benchmark 응답

2. `wod.types.ts`를 백엔드 DTO 기준으로 재점검한다.
   - optional 여부 정리
   - `namedWodId`, `templateId`, `createdAt`, `updatedAt` 타입 일관화
   - `classInstances` 구조 재검증

3. `useWods`, `useWodTemplates`, `useLeaderboard` 훅의 query key와 인자 타입을 정리한다.
   - date/from/to 타입 혼용 제거
   - `boxId: string | null` 패턴 통일

4. `updateClassInstances` API payload 타입에서 `any`를 제거한다.

### Done Criteria

- 프런트 타입이 백엔드 DTO를 안정적으로 표현한다.
- API 호출부에 `any`가 남지 않는다.
- 타입체크가 계속 통과한다.

---

## Phase 2. WOD Create/Edit Flow Completion

목표: `/wods/new`와 이후 `/wods/:id/edit`가 스펙 수준으로 동작하도록 완성한다.

### Tasks

1. `WodCreatePage`의 배정 정보 섹션을 보강한다.
   - 수업 목록에 `routineName` 노출
   - 같은 날짜 재조회 UX 안정화
   - 선택 수업 / Part 충돌 표시 준비

2. 공개 설정 UX를 보강한다.
   - `auto`, `manual`, `draft` 설명 문구 추가
   - 박스 정책 기준 preview 표시
   - manual publish datetime validation 추가

3. `WodEditPage`를 만든다.
   - 상세 조회 후 폼 초기화
   - 수정 API 연결
   - 기록 존재 시 `wodType` UI 비활성화

4. WOD 수정 시 수업 연결 / 공개 설정 변경 경로를 분리한다.
   - 내용 수정: `PATCH /wods/{wodId}`
   - 수업 연결 수정: `PATCH /wods/{wodId}/class-instances`
   - 공개 설정 수정: `PATCH /wods/{wodId}/publish`

### Done Criteria

- 새 WOD 생성에 필요한 핵심 입력이 스펙 수준으로 동작한다.
- 수정 플로우가 생성 플로우와 분리되어 안정적으로 동작한다.
- 기록 존재 조건에서 잘못된 수정이 UI에서 차단된다.

---

## Phase 3. Template Bank Completion

목표: `/wod-templates` 화면을 실제 운영 가능한 수준으로 완성한다.

### Tasks

1. 템플릿 카드 정보를 스펙에 맞게 확장한다.
   - 최근 배정일
   - 총 배정 횟수
   - Named WOD 뱃지

2. 검색/필터를 구현한다.
   - 키워드
   - WOD 타입
   - Named WOD
   - 태그

3. `DeploySheet`를 완성한다.
   - 수업 연결 없음 옵션 명확화
   - Part 선택 UX 개선
   - `publishedAt` 직접 설정 UI
   - `draft` 저장 UI
   - `overrides` 입력 UI
   - 박스 정책 자동 공개 preview

4. 템플릿 수정/삭제 UX를 정리한다.
   - `window.confirm` 제거
   - `alert-dialog` 사용
   - 성공/실패 피드백 통일

### Done Criteria

- 템플릿 검색, 생성, 수정, 삭제, 배정이 한 화면에서 안정적으로 처리된다.
- 배정 슬라이드오버가 백엔드 `DeployTemplateRequest`를 충분히 활용한다.

---

## Phase 4. Calendar and Day Panel Completion

목표: `/wods` 캘린더 화면을 스펙의 운영 화면 수준으로 끌어올린다.

### Tasks

1. 날짜 셀 표시 규칙을 스펙에 맞게 조정한다.
   - 수업 연동 WOD 수
   - 독립 WOD 수
   - 예약 공개 아이콘

2. `WodDayPanel`을 그룹형 구조로 바꾼다.
   - 수업별 그룹핑
   - 독립 WOD 그룹 분리
   - `display_order` 순서 표시
   - Part A / Part B 표시

3. `WodDayCard` 액션을 실제 라우트와 연결한다.
   - 리더보드 이동
   - 편집 이동
   - 영상 링크 열기

4. 카드 메타데이터를 확장한다.
   - publish 상태 설명
   - 수업명 / 루틴명
   - 참여자 수 / PR 수치 준비

### Done Criteria

- 캘린더와 우측 패널만으로 날짜별 WOD 운영이 가능하다.
- 같은 날짜의 복수 WOD와 수업별 파트 구성이 읽기 쉽게 드러난다.

---

## Phase 5. WOD Detail and Leaderboard

목표: 조회와 기록 관리 흐름을 연결한다.

### Tasks

1. `WodDetailPage`를 만든다.
   - 기본 정보
   - 출처 템플릿
   - Named WOD
   - 공개 상태

2. `WodLeaderboardPage`를 백엔드 응답 기준으로 보강한다.
   - Rx'd + scale group 필터
   - team / individual 모드 전환
   - 팀 구성 표시
   - PR 표시

3. `LeaderboardTable`을 그룹 섹션형으로 개선한다.
   - Rx'd 먼저
   - scale group별 묶음 표시
   - 팀 행 확장 구조 준비

4. 상세 화면과 리더보드 화면 라우트를 앱에 연결한다.

### Done Criteria

- 날짜 패널에서 상세/리더보드로 이동이 가능하다.
- 리더보드가 백엔드 정렬 규칙과 scale group 구조를 자연스럽게 반영한다.

---

## Phase 6. Record and Team Entry

목표: 코치가 실제 기록을 입력하고 관리할 수 있게 한다.

### Tasks

1. 개인 기록 입력 모달을 만든다.
   - 회원 검색
   - scale group 선택
   - WOD type별 결과 입력 UI
   - DNF
   - memo / video

2. 팀 기록 입력 모달을 만든다.
   - 팀원 추가/삭제
   - 리더 지정
   - 팀 이름 자동 생성
   - gender composition 표시

3. 기록 수정/삭제 액션을 연결한다.
   - record/team별 edit/delete

4. WOD 타입별 결과 입력 validation을 추가한다.

### Done Criteria

- 개인전/팀전 기록 입력이 실제 API에 맞게 동작한다.
- 결과 필드가 WOD 타입별로 올바르게 제한된다.

---

## Phase 7. Settings and Policy

목표: 박스별 WOD 공개 정책을 관리 가능하게 한다.

### Tasks

1. `/settings/wod` 페이지를 만든다.
   - timezone
   - publish policy
   - publish anchor
   - offset / fixed time

2. 정책 저장 API 연결

3. WOD 생성 / 템플릿 배정 화면에서 정책 preview 재사용

### Done Criteria

- Head Coach 이상이 박스 WOD 정책을 설정할 수 있다.
- 생성/배정 화면이 같은 정책 해석을 재사용한다.

---

## Phase 8. UX and Code Quality

목표: 기능 구현 후 남는 품질 이슈를 정리한다.

### Tasks

1. `alert`, `window.confirm`, `console.error` 중심 UX를 정리한다.
   - `alert-dialog`
   - 일관된 성공/실패 피드백

2. React Query 훅을 역할별로 나눈다.
   - query / mutation 분리
   - invalidate 범위 정리

3. store와 local state 경계를 정리한다.
   - store에 둘 상태와 페이지 state 분리

4. 불필요한 타입 단언 제거
   - `as`
   - 느슨한 string enum 처리

5. 테스트/검증 루틴 정리
   - `tsc --noEmit`
   - 주요 화면 smoke check

### Done Criteria

- 구현 이후 유지보수가 쉬운 상태가 된다.
- 화면별 에러 처리와 사용자 피드백이 통일된다.

---

## Suggested Execution Order

1. Phase 1
2. Phase 2
3. Phase 3
4. Phase 4
5. Phase 5
6. Phase 6
7. Phase 7
8. Phase 8

이 순서를 권장하는 이유는 다음과 같다.

- API 계약과 데이터 모델이 먼저 안정되어야 이후 화면 구현이 덜 흔들린다.
- WOD 생성/템플릿 배정이 핵심 업무 흐름이라 먼저 완성해야 한다.
- 리더보드와 기록 입력은 상세/조회 구조가 잡힌 뒤 구현하는 편이 안전하다.

---

## Immediate Next Step

가장 먼저 할 일:

1. `DeploySheet`를 백엔드 `DeployTemplateRequest` 기준으로 완성한다.
2. `WodEditPage`와 `WodDetailPage`를 추가한다.
3. `WodCreatePage`의 공개 정책 preview를 붙인다.

