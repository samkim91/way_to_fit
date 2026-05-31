# Flutter 앱 디자인 GAP 분석

> 기준 디자인: `docs/design-handoff/project/app-screens.jsx`
> 분석일: 2026-05-30
>
> **전제 조건:**
> - `box`(소속) 개념은 제외 (모델에서 뺀 것)
> - `예정(COMING_SOON)` 상태 = `오픈(OPEN)` 상태와 동일하게 처리
> - `scaleCategory` 는 이벤트별 설정값이 아니라 대회(`Competition`)의 공용 카테고리 목록으로 관리
> - 기록 제출 화면의 스케일 UI는 변경용 토글이 아니라 신청된 `registration.scaleCategory` 표시로 해석

---

## 1. 대회 목록 화면 (`competition_list_screen.dart`)

| 항목 | 디자인 | 현재 앱 | 우선순위 |
|---|---|---|---|
| 카드 배너 영역 | 그라데이션 Hero 이미지 | ❌ 없음 | 중 |
| 참가자 수 표시 | `47명` | ❌ 없음 (모델에도 없음) | 낮음 |
| 신청 마감 표시 | `신청 마감: 2026.04.30` 단순 텍스트 | 기간 범위 + 상대적 시간 | 낮음 |
| 필터 '예정' | '예정' 칩 | '오픈' 칩 (동일 상태) | 낮음 |

---

## 2. 대회 상세 화면 (`competition_detail_screen.dart`)

| 항목 | 디자인 | 현재 앱 | 우선순위 |
|---|---|---|---|
| 상단 Hero 배너 | 그라데이션 + 대회명/상태 오버레이 | ❌ 없음, 텍스트만 | 중 |
| 이벤트 카드 — 나의 기록 | 기록값 + 승인 상태 배지 표시 | ❌ 없음 (제출 버튼만) | **높음** |
| 나의 참가 상태 박스 | '기록 제출하기' + '신청 내역 보기' 버튼 | ❌ 없음 | **높음** |
| 신청 CTA 버튼 | 하단 고정 `대회 신청하기` FilledButton | `_ActionRow` ListTile 형태 | 중 |

---

## 3. 참가 신청 화면 (`individual_reg_screen.dart`, `team_reg_screen.dart`)

| 항목 | 디자인 | 현재 앱 | 우선순위 |
|---|---|---|---|
| 참가비 안내 카드 | 금액 + 계좌 + "입금 후 주최자 확인" 문구 | ❌ 없음 | **높음** |
| 버튼 스타일 | 2열 토글 버튼 (커스텀 디자인) | `SegmentedButton` | 낮음 |
| 스케일 동적 바인딩 | 대회에 설정된 공용 스케일 목록을 API로 읽어와 표시 | ❌ 'RXD'/'SCALED'로 하드코딩됨 | **높음** |

> **동적 스케일 바인딩:** `Competition.scaleCategories` 목록을 API에서 읽어와 신청 화면에 동적으로 렌더링해야 함. 이벤트는 이 공용 카테고리를 참조해 리더보드/표시에 사용한다.

---

## 4. 기록 제출 화면 (`score_submit_screen.dart`)

| 항목 | 디자인 | 현재 앱 | 우선순위 |
|---|---|---|---|
| WOD 정보 헤더 | Hero 그라데이션 + WOD 타입/이벤트명/마감 | ❌ 없음 | **높음** |
| WOD 설명 카드 | "21-15-9 Thrusters..." 코드 폰트 | ❌ 없음 | **높음** |
| 스케일 표시 | RXD/SCALED 토글 | ❌ 없음 | **높음** |
| 기록 입력 UI | 큰 숫자 박스 (28px bold, 분/초 분리) | 일반 `TextField` | 중 |

> 기록 제출 화면에서는 신청 시점에 확정된 `registration.scaleCategory` 를 표시만 하고, 제출 API에 별도 `scaleCategory` 를 다시 전달하지 않는다.

---

## 5. 리더보드 화면 (`leaderboard_screen.dart`)

| 항목 | 디자인 | 현재 앱 | 우선순위 |
|---|---|---|---|
| 성별 필터 | 남/여/전체 | ❌ 없음 | 중 |
| 스케일 필터 | RXD/SCALED 동적 필터링 | ❌ 없음 | 중 |
| 나의 순위 하이라이트 | 내 행 강조 + sticky note | ❌ 없음 | 중 |

> 성별/스케일 필터는 `LeaderboardQuery` 모델에도 추가 필요. 스케일 필터 칩은 이벤트별 저장값이 아니라 `Competition.scaleCategories` 공용 목록을 기반으로 렌더링한다.

---

## 6. 선수 프로필 화면 (`athlete_profile_screen.dart`)

| 항목 | 디자인 | 현재 앱 | 우선순위 |
|---|---|---|---|
| 통계 카드 | 대회 참가 수, 이벤트 기록 수, 최고 순위 | ❌ 없음 | 중 |
| 대회 이력 접기/펼치기 | `ExpansionTile` 형태 | 항상 펼쳐진 `Card` | 낮음 |
| 이벤트 기록 상태 배지 | 승인/검토중/거절 배지 | 텍스트만 | 중 |

---

## 데이터 모델 GAP

| 필드 | 위치 | 상태 |
|---|---|---|
| `participants` (참가자 수) | `Competition` 모델 | 필요 시 추가 |
| `scaleCategories` | `Competition` 모델 | 공용 스케일 카테고리 소유 필드로 필요 |
| gender/scale 필터 | `LeaderboardQuery` | 리더보드 필터 구현 시 추가 |
| `myScore`, `myScoreStatus` | 이벤트별 나의 기록 | 대회 상세 API 번들에서 내 기록 포함 필요 |
