# 백엔드 AI 코드 생성 프롬프트
# 주간 정규 시간표 관리 (Weekly Schedule Template)

---

## 역할 및 기술 스택

당신은 Kotlin, Spring Boot, Spring Data JPA, PostgreSQL을 사용하는 시니어 백엔드 개발자입니다.
CrossFit 박스 관리자용 **주간 정규 시간표(Weekly Schedule Template)**를 버전별로 관리하는 도메인을 구현해야 합니다.

기술 스택:
- Kotlin + Spring Boot
- Spring Data JPA + PostgreSQL
- Spring Scheduler (`@Scheduled`)
- 타임존: 모든 날짜/시간은 `Instant` (UTC)로 저장, 비즈니스 로직 내 날짜 계산은 `Asia/Seoul` 기준

---

## 1. DB 스키마 (JPA Entities)

### ScheduleTemplate — 시간표 버전 마스터
```kotlin
@Entity
@Table(name = "schedule_templates")
class ScheduleTemplate(
    @Id val id: UUID = UUID.randomUUID(),
    val boxId: UUID,
    val effectiveStartDate: Instant,   // Asia/Seoul 기준 당일 00:00을 UTC Instant로 변환하여 저장
    val generateMonths: Int,            // 인스턴스 생성 개월 수 (1~12)
    @Enumerated(EnumType.STRING)
    var status: TemplateStatus = TemplateStatus.DRAFT,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
)

enum class TemplateStatus { DRAFT, SCHEDULED, ACTIVE, ARCHIVED }
```

### ClassRoutine — 버전에 속한 주간 수업 규칙
```kotlin
@Entity
@Table(name = "class_routines")
class ClassRoutine(
    @Id val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    val template: ScheduleTemplate,
    @Enumerated(EnumType.STRING)
    val dayOfWeek: DayOfWeek,          // java.time.DayOfWeek
    val startTime: Instant,
    val endTime: Instant,
    val capacity: Int?,                 // null = 무제한
)
```

### ClassRoutineCoach — 수업 ↔ 코치 N:M
```kotlin
@Entity
@Table(name = "class_routine_coaches")
class ClassRoutineCoach(
    @EmbeddedId val id: ClassRoutineCoachId,
)

@Embeddable
data class ClassRoutineCoachId(
    val routineId: UUID,
    val coachId: UUID,
) : Serializable
```

### ClassInstance — 실제 캘린더 수업
```kotlin
@Entity
@Table(name = "class_instances")
class ClassInstance(
    @Id val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    val routine: ClassRoutine,
    val classDate: Instant,             // 실제 수업 일시 (UTC)
    var isCancelled: Boolean = false,
    val createdAt: Instant = Instant.now(),
)
```

### ClassReservation — 회원 예약
```kotlin
@Entity
@Table(name = "class_reservations")
class ClassReservation(
    @Id val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id")
    val instance: ClassInstance,
    val memberId: UUID,
    @Enumerated(EnumType.STRING)
    var status: ReservationStatus = ReservationStatus.CONFIRMED,
    val createdAt: Instant = Instant.now(),
)

enum class ReservationStatus { CONFIRMED, CANCELLED }
```

---

## 2. REST API

### 2-1. 시간표 버전 관리

#### POST /api/v1/schedule-templates
- Template + Routines + CoachMappings를 **단일 트랜잭션**으로 생성
- 생성 시 status = DRAFT
- Request Body:
```json
{
  "effectiveStartDate": "2026-06-01T00:00:00Z",
  "generateMonths": 3,
  "routines": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "2026-06-01T06:00:00Z",
      "endTime": "2026-06-01T07:00:00Z",
      "capacity": 15,
      "coachIds": ["uuid1", "uuid2"]
    }
  ]
}
```

#### GET /api/v1/schedule-templates
- 버전 목록 반환
- Query param: `status` (optional, 복수 가능)

#### GET /api/v1/schedule-templates/active?at={instant}
- 특정 시점 기준으로 ACTIVE인 버전 조회
- effectiveStartDate <= at 조건 중 가장 최신 버전 반환

#### PUT /api/v1/schedule-templates/{id}
- **DRAFT 상태일 때만 수정 가능**, 다른 상태이면 400 반환

#### DELETE /api/v1/schedule-templates/{id}
- **DRAFT 상태일 때만 삭제 가능**, 다른 상태이면 400 반환

---

### 2-2. 배포 확정

#### POST /api/v1/schedule-templates/{id}/deploy

effectiveStartDate를 Asia/Seoul 기준으로 오늘 날짜와 비교하여 분기:

```
오늘이면 → 즉시 deployTemplate(template) 실행
미래이면 → status = SCHEDULED로 저장, 스케줄러에 위임
```

**deployTemplate() 내부 흐름 (즉시 적용 / 스케줄러 공통):**
1. 동일 boxId의 기존 ACTIVE 템플릿 → ARCHIVED 전환
2. 기존 ACTIVE 템플릿의 미래 ClassInstance 일괄 삭제
3. 삭제된 인스턴스에 연결된 CONFIRMED ClassReservation → CANCELLED 처리
4. 예약 취소된 회원에게 알림 발송 (알림 서비스 호출)
5. 신규 템플릿 → ACTIVE 전환
6. generateMonths 개월치 ClassInstance Bulk Insert

> 즉시 적용과 스케줄러 실행이 **동일한 내부 함수 `deployTemplate()`을 공유**하도록 Service 계층에서 추출하세요.

---

### 2-3. 인스턴스(캘린더) 관리

#### GET /api/v1/class-instances?from={instant}&to={instant}
- 지정 기간 내 ClassInstance 목록 반환 (월간 캘린더용)

#### PATCH /api/v1/class-instances/{id}/cancel
- isCancelled 값을 반전(toggle)
- Response: 변경된 ClassInstance 반환

---

## 3. Service Layer 규칙

### Template 저장 시 검증
```kotlin
// 1. effectiveStartDate는 현재 시점 이후여야 함
if (request.effectiveStartDate <= Instant.now()) throw BadRequestException("적용 시작일은 현재 이후여야 합니다")

// 2. 동일 boxId 내 effectiveStartDate 중복 불가 (DRAFT 제외)
val duplicate = templateRepository.findByBoxIdAndEffectiveDateExcludingDraft(boxId, request.effectiveStartDate)
if (duplicate != null) throw ConflictException("동일한 적용 시작일이 이미 존재합니다")
```

### 타임존 변환 유틸
```kotlin
val SEOUL_ZONE = ZoneId.of("Asia/Seoul")

// 오늘(Asia/Seoul) 00:00 UTC Instant 계산
fun todayStartInstant(): Instant =
    LocalDate.now(SEOUL_ZONE).atStartOfDay(SEOUL_ZONE).toInstant()

// effectiveStartDate가 오늘인지 판단
fun isToday(instant: Instant): Boolean =
    instant == todayStartInstant()
```

---

## 4. 스케줄러

### 실행 조건
- 매일 **00:00 Asia/Seoul** 기준 실행
- `@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")`

### 처리 흐름
```kotlin
@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
fun activateScheduledTemplates() {
    val todayStart = todayStartInstant()
    val targets = templateRepository.findAllByStatusAndEffectiveStartDate(
        TemplateStatus.SCHEDULED, todayStart
    )
    targets.forEach { template ->
        deployTemplate(template)
    }
}
```

---

## 5. Instance Generator (Bulk Insert)

### 생성 로직
```kotlin
fun generateInstances(template: ScheduleTemplate) {
    val seoulZone = ZoneId.of("Asia/Seoul")
    val startDate = template.effectiveStartDate.atZone(seoulZone).toLocalDate()
    val endDate = startDate.plusMonths(template.generateMonths.toLong())

    val instances = template.routines.flatMap { routine ->
        generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it < endDate }
            .filter { it.dayOfWeek == routine.dayOfWeek }
            .map { date ->
                ClassInstance(
                    routine = routine,
                    classDate = date.atStartOfDay(seoulZone).toInstant()
                    // 실제 수업 시각은 routine.startTime 기준으로 별도 계산 필요
                )
            }
    }

    // JDBC batch insert 사용 권장 (JPA saveAll은 N+1 위험)
    instanceRepository.bulkInsert(instances)
}
```

### 성능 주의
- `saveAll()` 대신 JDBC batch insert 사용 (`JdbcTemplate.batchUpdate`)
- 수천 건 이상 insert 시 단일 트랜잭션으로 처리

---

## 6. 비즈니스 로직 주의사항

1. `deployTemplate()`은 기존 ACTIVE 인스턴스 폐기 → 예약 취소 → 신규 인스턴스 생성 순서를 **단일 트랜잭션**으로 처리
2. 예약 취소 알림 발송은 트랜잭션 커밋 후 이벤트 발행(`ApplicationEventPublisher`) 방식으로 분리 권장
3. 스케줄러 실행 중 예외 발생 시 해당 템플릿만 롤백되고 다른 템플릿은 계속 처리되도록 try-catch 처리
4. 모든 Instant 값은 PostgreSQL `TIMESTAMP WITH TIME ZONE` 컬럼에 매핑