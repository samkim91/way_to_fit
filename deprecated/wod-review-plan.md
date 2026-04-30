# WOD 도메인 수정 계획

리뷰 결과를 우선순위 순으로 정리한 작업 계획서. 각 단계는 **독립 PR**로 분리 가능하게 구성했다. 앞 단계부터 순차 진행 권장.

---

## Phase 1 — 보안·데이터 무결성 (긴급)

### Step 1. 서비스 계층 소유권(boxId) 검증 추가
**문제:** `WodCommandService.updateWod(boxId, wodId, ...)` 등에서 `boxId` 파라미터를 받지만 `wod.boxId == boxId` 검증을 하지 않음 (IDOR).

**영향 파일:**
- `application/service/WodCommandService.kt` — `updateWod`, `deleteWod`, `updateClassInstances`, `updatePublish`
- `application/service/WodRecordCommandService.kt` — `createRecord`, `updateRecord`, `deleteRecord`
- `application/service/WodTemplateCommandService.kt` — `updateTemplate`, `deleteTemplate`, `deployTemplate`
- `application/service/WodSettingsCommandService.kt` — 존재 시 동일 적용
- `application/service/WodTeamCommandService.kt` — 존재 시 동일 적용

**작업:**
1. `ResponseCode`에 `WOD_FORBIDDEN`(혹은 공통 `FORBIDDEN`) 추가
2. 각 메서드 시작부에 guard 추가:
   ```kotlin
   if (wod.boxId != boxId) throw BusinessException(ResponseCode.FORBIDDEN)
   ```
3. Record 계열은 `record.boxId != boxId` 혹은 `record.wodId`로 wod 조회 후 `wod.boxId` 비교
4. 테스트: 각 서비스별 "다른 박스의 리소스 접근 차단" 케이스 추가

**완료 조건:** 타 박스 UUID로 접근 시 `FORBIDDEN` 반환되는 테스트 통과

---

### Step 2. PR(개인 최고기록) 무결성 복구
**문제:**
- 새 PR 생성 시 기존 PR의 `isPr=false` 업데이트 안 함 → 중복 PR
- `updateRecord`에서 결과값 변경 시 PR 재계산 안 함
- `deleteRecord`에서 PR 제거 시 차순위 PR 승격 안 함

**영향 파일:**
- `application/service/WodRecordCommandService.kt`
- `application/service/PrDetector.kt` (로직 확장)

**작업:**
1. `PrDetector`에 "현재 PR 재산정" 메서드 추가:
   ```kotlin
   fun recomputePr(records: List<WodRecord>, wodType: WodType): UUID? // PR인 record id 반환
   ```
2. `createRecord` — 새 기록이 PR이면 기존 PR 기록들을 배치로 `isPr=false` 업데이트
3. `updateRecord` — 결과값(time/reps/rounds/weight/status) 변경 시 해당 (wodId, memberId, scaleGroupId) 그룹 PR 재산정
4. `deleteRecord` — 삭제된 것이 PR이었으면 동일 그룹 PR 재산정
5. `WodRecordPersistencePort.saveAll(records)` 필요 시 추가
6. 테스트: PR 전환 시나리오 4종 (최초 생성 / 신기록 갱신 / 결과 수정으로 PR 박탈 / PR 삭제로 승격)

**완료 조건:** 동일 그룹에서 `isPr=true`인 record가 항상 0 또는 1건

---

### Step 3. 리더보드 `gender` 필터 정상화
**문제:** `findLeaderboardRecords(wodId, wodType, gender, scaleGroupId)`의 `gender` 파라미터가 QueryDsl 구현체에서 사용되지 않음.

**영향 파일:**
- `adapter/out/persistence/repository/WodRecordQueryDslRepositoryImpl.kt`
- `application/port/out/BoxMemberQueryPort.kt` (필요 시 member gender 조회 API 확장)

**작업:**
1. Member gender가 다른 도메인(user/member) 소유이므로 두 경로 중 선택:
   - **(A) DB join**: `member` 테이블과 join (도메인 경계 침범) — 성능 유리
   - **(B) 애플리케이션 필터**: `BoxMemberQueryPort.findByIds`로 gender map 조회 후 서비스 레이어에서 필터 — 경계 유지, 건수 많으면 불리
2. 권장: **(B)** 채택 후, `LeaderboardQueryService.buildIndividualLeaderboard`에서 records 조회 → memberInfoMap 조회 후 `query.gender` 기준 필터링
3. Repository에서 `gender` 파라미터 제거 (의도치 않은 무시 방지)
4. 테스트: 남녀 혼합 데이터에서 gender 필터 적용 시 결과 검증

**완료 조건:** `gender="MALE"` 요청 시 남자 멤버 기록만 반환

---

### Step 4. `WodPublishScheduledService` 구현 또는 제거
**문제:** 5분마다 실행되지만 실제 처리 로직 없음 (로그만).

**작업:**
1. 발행에 수반되는 사이드 이펙트 정의 (선택):
   - 알림 발송 (푸시/이메일)
   - 검색/캐시 인덱싱
   - 외부 시스템 연동
2. 사이드 이펙트가 없다면 **스케줄러 자체 제거** (`publishStatus()`는 derived라 불필요)
3. 사이드 이펙트가 있다면:
   - `Wod`에 `notifiedAt: Instant?` 컬럼 추가 (멱등성 확보)
   - 조회 쿼리를 `publishedAt ≤ now AND notifiedAt IS NULL`로 변경
   - 처리 후 `notifiedAt` 갱신

**완료 조건:** 의도 명확화 — 둘 중 하나로 확정

---

## Phase 2 — 성능·확장성

### Step 5. `WodPersistenceAdapter.findAllByBoxIdAndFilters`의 publishStatus를 DB로 푸시
**영향 파일:**
- `adapter/out/persistence/repository/WodJpaRepository.kt`
- `adapter/out/persistence/WodPersistenceAdapter.kt`
- `application/port/out/WodPersistencePort.kt`

**작업:**
1. JPQL `@Query`에 `:publishStatus` 분기 추가:
   ```sql
   AND (
     :publishStatus IS NULL
     OR (:publishStatus = 'DRAFT'     AND w.publishedAt IS NULL)
     OR (:publishStatus = 'SCHEDULED' AND w.publishedAt >  :now)
     OR (:publishStatus = 'PUBLISHED' AND w.publishedAt <= :now)
   )
   ```
2. Adapter에서 메모리 필터링 제거
3. 테스트: 기존 결과와 동일함을 검증

**완료 조건:** DB에서 필터된 개수만큼만 반환

---

### Step 6. `BenchmarkQueryService`의 N+1 제거
**영향 파일:**
- `application/port/out/WodPersistencePort.kt` — `findAllByIds(ids)` 추가
- `adapter/out/persistence/WodPersistenceAdapter.kt`
- `application/service/BenchmarkQueryService.kt`

**작업:**
1. `WodPersistencePort.findAllByIds(ids: Collection<UUID>): List<Wod>` 추가
2. `BenchmarkQueryService.getMemberBenchmarks`에서 전체 wodIds 수집 후 한 번에 조회
3. 가능하면 `recordPersistence.findByMemberIdAndNamedWodIds(memberId, namedWodIds)` 형태로 outer loop의 쿼리도 묶기
4. `wod == null` fallback을 `LocalDate.now()`로 하지 말고 해당 record 스킵 + warn 로그

**완료 조건:** 멤버별 벤치마크 조회 쿼리 수가 O(namedWodCount)에서 O(1~2)로 감소

---

### Step 7. 리더보드 동률(tie) 처리
**영향 파일:**
- `application/service/LeaderboardQueryService.kt`

**작업:**
1. `assignRanks`를 표준 경기 순위(dense/standard) 방식으로 변경:
   - 동률이면 같은 rank, 다음 항목은 건너뛴 rank
2. DNF는 rank 부여하지 않거나 별도 섹션으로 분리 (UX 정책 확인)
3. 비교 키 (`resultTimeSeconds` 등)를 `compareKey(entry)` 유틸로 추출해 individual/team 로직 통합
4. 테스트: 동률 3명 / DNF 혼합 케이스

**완료 조건:** 동률 입력 시 동일 rank 반환

---

## Phase 3 — 품질·유지보수성

### Step 8. Partial update 시 nullable 필드 클리어 가능하게
**영향 파일:**
- `application/port/in/` 하위 Command DTO들
- 관련 Service `update*` 메서드들

**작업:**
1. "미지정" vs "null로 설정"을 구분할 수 있는 래퍼 도입 (예: `sealed class Patch<T> { Unchanged, Set(value) }`) 또는 Jackson `JsonNullable`
2. Service에서 `command.description ?: wod.description` 패턴을 `command.description.applyTo(wod.description)`로 치환
3. Request DTO/Controller에서 역직렬화 규칙 명시

**완료 조건:** `description: null` 요청으로 실제 null 업데이트 가능

---

### Step 9. `PrDetector` null 처리 일관화
**영향 파일:** `application/service/PrDetector.kt`

**작업:**
1. Service 계층에서 `COMPLETED` 상태인데 결과값이 없는 record 생성/수정 차단 (validation)
2. `PrDetector` 내부는 non-null 가정으로 정리, 각 분기의 `?: return ...` 제거
3. 명시적 케이스 누락은 `require(...)` 또는 도메인 예외

**완료 조건:** PrDetector의 `?:` 분기 개수가 1개 이하

---

### Step 10. 타입 안전성 개선 및 매직 스트링 제거
**영향 파일:**
- `domain/BoxWodSettings.kt`
- `application/service/PublishAtCalculator.kt`
- `application/port/in/dto/LeaderboardQuery` 등

**작업:**
1. `BoxWodSettings.publishFixedTime: String` → `LocalTime?`
2. `publishedAtOverride: String?` → sealed class:
   ```kotlin
   sealed class PublishOverride {
       object Auto : PublishOverride()
       data class At(val instant: Instant) : PublishOverride()
       object Draft : PublishOverride()
   }
   ```
3. `LeaderboardQuery.scaleGroupId`의 `"all"`/`"null"` 매직 스트링 → `ScaleGroupFilter` sealed class
4. DTO↔도메인 변환은 adapter 레이어에서만

**완료 조건:** 문자열 비교 기반 분기가 도메인/서비스에 없음

---

### Step 11. QueryDsl 중복 제거
**영향 파일:** `adapter/out/persistence/repository/WodRecordQueryDslRepositoryImpl.kt`

**작업:**
1. `wodRecordEntity.deletedAt.isNull` 제거 (`@SQLRestriction`과 중복)
2. 프로젝트 내 모든 QueryDsl 쿼리에서 동일 패턴 점검

**완료 조건:** SQLRestriction과 중복된 조건 제거

---

## Phase 4 — (선택) 날짜 타입 일관화

### Step 12. `Instant` 선호 반영 검토
**배경:** 유저 선호는 `Instant` > `LocalDate`. 다만 `Wod.date`는 캘린더 날짜 의미가 본질이라 트레이드오프 존재.

**선택지:**
- **유지**: 캘린더성 필드(`Wod.date`, `Member1rmRecord.recordedDate`)만 `LocalDate` 유지 — 의미 정확
- **전환**: `Wod.occurredAt: Instant` + `boxTimezone`으로 저장, 조회 시 `LocalDate`로 프로젝션 — 글로벌/멀티타임존 대비

**작업(전환 선택 시):**
1. 마이그레이션 스크립트 작성 (기존 `date` → `occurredAt = date.atStartOfDay(boxTz).toInstant()`)
2. Domain/DTO/Repository 전반 치환
3. 쿼리의 `BETWEEN date` 범위 조건을 `occurredAt` 범위로 변환
4. 프론트 API 영향도 확인

**완료 조건:** 의사 결정 후 둘 중 하나로 확정 (결정 자체가 산출물)

---

## 진행 순서 요약

```
Phase 1 (보안/무결성)  →  Step 1 → 2 → 3 → 4
Phase 2 (성능)         →  Step 5 → 6 → 7
Phase 3 (품질)         →  Step 8 → 9 → 10 → 11
Phase 4 (선택)         →  Step 12
```

각 Step마다:
1. 브랜치 분기
2. 변경 + 테스트
3. PR 리뷰
4. 병합

Phase 1은 운영 리스크가 있으니 **가장 먼저** 진행하는 것을 권장한다.
