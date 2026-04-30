# WOD Frontend Implementation Status

`WOD_Frontend_Implementation_Plan.md` 기준으로 현재 구현 상태를 정리한 문서다.

기준 시점: 2026-04-17

---

## Summary

- 핵심 운영 기능 구현: 대부분 완료
- 남은 큰 범위: `Phase 7`, `Phase 8`
- 남은 중간 범위: 템플릿 검색/필터 UI 고도화

---

## Phase Status

### Phase 1. API Contract Alignment

상태: 완료

- [x] `wod.api.ts` 응답 정규화 확장
- [x] `wod.types.ts` DTO 재정렬
- [x] leaderboard 응답 정규화
- [x] records 조회/정규화 추가
- [x] `updateClassInstances`의 `any` 제거
- [x] 타입체크 통과 유지

메모:
- backend leaderboard에 `recordId`를 추가해 프런트가 휴리스틱 없이 개인 기록을 식별한다.
- backend `GET /wods/{wodId}/records` 조회 API도 추가되어 개인 기록 수정/삭제 경로가 닫혔다.

### Phase 2. WOD Create/Edit Flow Completion

상태: 완료

- [x] `WodCreatePage` 생성 플로우 연결
- [x] 수업 연결 및 Part 선택 반영
- [x] 공개 설정 `auto / manual / draft` 반영
- [x] 박스 정책 기준 preview 반영
- [x] `WodEditPage` 구현
- [x] 수정 API 분리
- [x] 기록 존재 시 `wodType` 수정 제한

메모:
- 내용 수정, 수업 연결 수정, 공개 설정 수정을 각각 다른 API로 분리했다.

### Phase 3. Template Bank Completion

상태: 부분 완료

- [x] 템플릿 카드 메타데이터 확장
- [x] 배정 횟수 / 최근 배정일 / Named WOD 표시
- [x] `DeploySheet` 핵심 배정 플로우 구현
- [x] `publishedAt` 직접 설정 UI 반영
- [x] `draft` 저장 UI 반영
- [x] `overrides` 입력 UI 반영
- [x] `window.confirm` 제거
- [x] 삭제 `alert-dialog` 적용
- [ ] WOD 타입 필터 UI
- [ ] Named WOD 필터 UI
- [ ] 태그 필터 UI
- [ ] 검색/필터 상태와 API 인자 범위 최종 정리

메모:
- 현재 템플릿 페이지는 키워드 검색은 동작하지만, 스펙 수준의 다중 필터 UI는 아직 없다.

### Phase 4. Calendar and Day Panel Completion

상태: 대부분 완료

- [x] 날짜 셀에 수업 연동 / 독립 WOD 수 표시
- [x] 예약 공개 상태 표현
- [x] `WodDayPanel` 수업별 그룹핑
- [x] 독립 WOD 그룹 분리
- [x] `displayOrder` 순서 표시
- [x] `WodDayCard` 라우트 액션 연결
- [x] 편집 / 리더보드 / 영상 링크 연결
- [~] 참여자 수 / PR 수치 운영 메타데이터 강화

메모:
- 운영 화면으로 쓸 수 있는 수준까지는 올라왔지만, 카드 수준 통계 정보는 더 보강할 수 있다.

### Phase 5. WOD Detail and Leaderboard

상태: 완료

- [x] `WodDetailPage` 구현
- [x] 기본 정보 / 템플릿 출처 / Named WOD / 공개 상태 표시
- [x] `WodLeaderboardPage` 보강
- [x] Rx'd + scale group 구조 반영
- [x] 팀/개인 모드 전환 반영
- [x] 팀 구성 표시
- [x] PR / DNF 표시
- [x] 앱 라우트 연결

메모:
- 상세 화면에서도 기록과 팀을 관리할 수 있도록 확장되었다.

### Phase 6. Record and Team Entry

상태: 완료

- [x] 개인 기록 입력 모달
- [x] 회원 검색
- [x] scale group 선택
- [x] WOD 타입별 결과 입력 UI
- [x] DNF / memo / video 반영
- [x] 팀 기록 입력 모달
- [x] 팀원 추가/삭제
- [x] 리더 지정
- [x] 팀 이름 자동 생성
- [x] record/team edit/delete 연결
- [x] 상세 화면과 리더보드 양쪽에서 CRUD 가능
- [x] WOD 타입별 validation 반영

메모:
- `gender composition`은 멤버 API 응답 한계가 있으면 일부 제약이 있을 수 있어 실제 표시 수준은 재확인 필요하다.

### Phase 7. Settings and Policy

상태: 미완료

- [ ] `/settings/wod` 페이지 구현
- [ ] timezone 설정 UI
- [ ] publish policy / anchor / offset / fixed time 설정 UI
- [ ] 정책 저장 API 연결
- [ ] 라우트 및 권한 연결
- [ ] 생성/배정 화면 preview와 설정 화면 로직 재사용 정리

메모:
- API 타입과 호출 함수는 일부 준비돼 있지만, 실제 페이지와 라우트는 아직 없다.

### Phase 8. UX and Code Quality

상태: 미완료

- [ ] `alert` 중심 UX 제거
- [ ] `console.error` 중심 에러 처리 제거
- [ ] 성공/실패 피드백 통일
- [ ] React Query 훅 `query / mutation` 분리
- [ ] invalidate 유틸 또는 공통 훅 정리
- [ ] store와 local state 경계 재정리
- [ ] 불필요한 타입 단언 제거
- [x] `tsc --noEmit` 검증 루틴 유지
- [ ] 주요 화면 smoke check 문서화

메모:
- 현재 기능은 동작하지만, UX 피드백과 훅 구조는 아직 정리 여지가 크다.

---

## Remaining TODO

우선순위 순서:

1. `Phase 7` 완료
2. 템플릿 검색/필터 UI 완성
3. `Phase 8` UX/코드 품질 정리

세부 작업:

- [ ] `WodSettingsPage` 생성
- [ ] `/settings/wod` 라우트 연결
- [ ] Head Coach 이상 권한 가드 연결
- [ ] settings 저장 후 create/deploy preview 로직과 공통화
- [ ] 템플릿 필터 UI 추가
- [ ] 토스트 또는 공통 피드백 체계 적용
- [ ] `useWods`, `useWodTemplates`, `useLeaderboard` 경계 재정리
- [ ] 화면별 공통 invalidate 로직 추출

---

## Recommended Next Step

가장 자연스러운 다음 작업:

1. `Phase 7`의 `/settings/wod` 페이지를 구현한다.
2. 그 다음 템플릿 필터 UI를 마무리한다.
3. 마지막으로 `Phase 8` 품질 정리를 진행한다.
