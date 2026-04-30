# WOD & Leaderboard Backend 구현 프롬프트

> 이 프롬프트는 AI 코드 생성 도구에 전달하여 WOD(Workout of the Day) & Leaderboard 백엔드를 구현하기 위한 self-contained 프롬프트입니다.

---

## 1. 프로젝트 컨텍스트

### 기술 스택
- **Language**: Kotlin 2.3.10
- **Framework**: Spring Boot 3.4.3
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA + QueryDSL 5.1.0
- **Auth**: Spring Security + JWT (jjwt 0.12.6) + Google OAuth2
- **API Docs**: springdoc-openapi (Swagger)
- **Build**: Gradle (Kotlin DSL)

### 아키텍처
**Hexagonal Architecture** (Domain-First) 기반. 기존 bounded context: `user`, `box`, `schedule`이 구현되어 있으며, 새로운 `wod` bounded context를 동일 패턴으로 추가합니다.

**의존성 방향**: `adapter` → `application` → `domain` ← `global`

### 베이스 패키지
```
com.waytofit.wod/
```

### 소스 루트
```
way-to-fit-backend/src/main/kotlin/com/waytofit/
```

---

## 2. 기존 코드 패턴 레퍼런스

> 아래 패턴을 그대로 따라 `wod` bounded context를 구현하세요.

### 2.1 Domain Entity 패턴

```kotlin
// 순수 비즈니스 도메인 객체 - Spring/JPA import 없음
package com.waytofit.schedule.domain

import com.waytofit.global.domain.AuditInfo
import java.time.Instant
import java.util.UUID

data class ClassInstance(
    val id: UUID? = null,
    val routineId: UUID,
    val classDate: Instant,
    var isCancelled: Boolean = false,
    val coachId: UUID? = null,
    val coachName: String? = null,
    val capacity: Int? = null,
    val audit: AuditInfo = AuditInfo.empty()
) {
    fun toggleCancel() {
        this.isCancelled = !this.isCancelled
    }
}
```

**팩토리 패턴 (복잡한 생성 시)**:
```kotlin
data class Box(
    val id: UUID?,
    val name: String,
    val description: String?,
    val address: String?,
    val audit: AuditInfo = AuditInfo.empty(),
) {
    companion object {
        fun create(name: String, description: String?, address: String?) = Box(
            id = null, name = name, description = description, address = address
        )
    }
}
```

### 2.2 JPA Entity 패턴

```kotlin
package com.waytofit.schedule.adapter.out.persistence.entity

import com.waytofit.global.domain.AuditInfo
import com.waytofit.global.persistence.BaseEntity
import com.waytofit.schedule.domain.ClassInstance
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "class_instances")
@SQLRestriction("deleted_at IS NULL")
class ClassInstanceEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "routine_id", nullable = false)
    val routineId: UUID,

    @Column(name = "class_date", nullable = false)
    val classDate: Instant,

    @Column(name = "is_cancelled", nullable = false)
    val isCancelled: Boolean = false,
) : BaseEntity() {

    fun toDomain() = ClassInstance(
        id = id, routineId = routineId, classDate = classDate,
        isCancelled = isCancelled,
        audit = AuditInfo(createdAt = createdAt, createdBy = createdBy,
            lastModifiedAt = lastModifiedAt, lastModifiedBy = lastModifiedBy)
    )

    companion object {
        fun fromDomain(domain: ClassInstance) = ClassInstanceEntity(
            id = domain.id, routineId = domain.routineId,
            classDate = domain.classDate, isCancelled = domain.isCancelled
        )
    }
}
```

### 2.3 BaseEntity

```kotlin
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null; protected set

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    var createdBy: String? = null; protected set

    @LastModifiedDate
    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: Instant? = null; protected set

    @LastModifiedBy
    @Column(name = "last_modified_by")
    var lastModifiedBy: String? = null; protected set

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null; protected set

    @Column(name = "deleted_by")
    var deletedBy: String? = null; protected set

    fun softDelete() {
        this.deletedAt = Instant.now()
        this.deletedBy = SecurityUtils.getCurrentAuditor()
    }
}
```

### 2.4 UseCase Interface (Input Port)

```kotlin
interface TemplateCommandUseCase {
    fun createTemplate(command: CreateTemplateCommand): UUID
    fun updateTemplate(id: UUID, command: UpdateTemplateCommand)
    fun deleteTemplate(id: UUID)
}
```

### 2.5 Persistence Port (Output Port)

```kotlin
interface TemplatePersistencePort {
    fun save(template: ScheduleTemplate): ScheduleTemplate
    fun findById(id: UUID): ScheduleTemplate?
    fun delete(id: UUID)
    fun findAllByBoxIdAndStatusIn(boxId: UUID, statuses: List<TemplateStatus>): List<ScheduleTemplate>
}
```

### 2.6 Persistence Adapter

```kotlin
@Component
class TemplatePersistenceAdapter(
    private val templateJpaRepository: ScheduleTemplateJpaRepository,
) : TemplatePersistencePort {

    override fun save(template: ScheduleTemplate): ScheduleTemplate {
        return templateJpaRepository.save(ScheduleTemplateEntity.fromDomain(template)).toDomain()
    }

    override fun findById(id: UUID): ScheduleTemplate? {
        return templateJpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }
}
```

### 2.7 Controller 패턴

```kotlin
@Tag(name = "Schedule Template", description = "주간 정규 시간표 버전 관리")
@RestController
@RequestMapping("/api/v1/admin/schedule-templates")
class AdminScheduleTemplateController(
    private val templateCommandUseCase: TemplateCommandUseCase,
    private val templateQueryUseCase: TemplateQueryUseCase
) {
    @Operation(summary = "시간표 버전 생성")
    @PostMapping
    fun createTemplate(@RequestBody request: CreateTemplateRequestDto): ApiResponse<UUID> {
        val command = CreateTemplateCommand(
            boxId = request.boxId,
            effectiveStartDate = request.effectiveStartDate,
            generateMonths = request.generateMonths,
            routines = request.routines.map { RoutineCommand(it.dayOfWeek, it.startTime, it.endTime, it.capacity, it.coachId) }
        )
        return ApiResponse.success(templateCommandUseCase.createTemplate(command))
    }
}
```

### 2.8 ApiResponse

```kotlin
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val code: String,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T? = null): ApiResponse<T> =
            ApiResponse(code = ResponseCode.SUCCESS.code, message = ResponseCode.SUCCESS.message, data = data)
        fun <T> error(responseCode: ResponseCode, message: String? = null): ApiResponse<T> =
            ApiResponse(code = responseCode.code, message = message ?: responseCode.message)
    }
}
```

### 2.9 ResponseCode & BusinessException

```kotlin
enum class ResponseCode(val httpStatus: HttpStatus, val code: String, val message: String) {
    SUCCESS(HttpStatus.OK, "0000", "성공"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "0002", "존재하지 않는 리소스입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "0010", "권한이 없습니다."),
    // Schedule (4000~4999)
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "4004", "존재하지 않는 시간표입니다."),
    ;
}

// WOD 도메인은 5000~5999 범위 사용
```

```kotlin
open class BusinessException(
    val responseCode: ResponseCode,
    val overrideMessage: String? = null
) : RuntimeException(overrideMessage ?: responseCode.message)
```

### 2.10 Enum 패턴

```kotlin
// domain/enums/ 디렉토리에 위치
package com.waytofit.schedule.domain.enums

enum class TemplateStatus { DRAFT, SCHEDULED, ACTIVE, ARCHIVED }
```

---

## 3. WOD 데이터 모델

### 3.1 `box_wod_settings` (박스별 WOD 공개 정책)

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

#### `published_at` 자동 계산 로직

```
policy = manual → published_at = NULL

policy = scheduled, anchor = class_start
  → 연결된 class_instance의 startTime + publish_offset_minutes
  → 수업 없는 독립 WOD → manual 처리

policy = scheduled, anchor = fixed_time_of_day
  → (wod.date + publish_fixed_day_offset)의 publish_fixed_time
  → box timezone 기준으로 계산 후 UTC Instant 변환
  예) date=8/20, offset=-1, time=22:00, tz=Asia/Seoul → 2025-08-19T13:00:00Z
```

### 3.2 `wod_templates` (WOD 은행)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `box_id` | UUID FK | NULL = 플랫폼 공용 |
| `coach_id` | UUID FK | 작성 코치 |
| `name` | VARCHAR(100) | 필수 |
| `description` | TEXT | 운동 내용 (Rx'd 기준) |
| `image_urls` | TEXT[] | |
| `video_url` | VARCHAR(500) | |
| `wod_type` | ENUM | `for_time` \| `amrap` \| `emom` \| `max_weight` \| `custom` |
| `custom_type_label` | VARCHAR(100) | `wod_type = custom`일 때 필수 |
| `time_cap` | INT NULL | 제한 시간 초 (For Time / Custom) |
| `amrap_duration` | INT NULL | AMRAP 총 시간 (초) |
| `emom_duration` | INT NULL | EMOM 총 시간 (초, NULL = Death by 등 무제한) |
| `emom_target_reps` | INT NULL | EMOM 목표 총 rep |
| `weight_unit` | ENUM NULL | `kg` \| `lb` |
| `scale_groups` | JSONB | 스케일 그룹 정의 |
| `competition_mode` | ENUM | `none` \| `individual` \| `team` |
| `named_wod_id` | UUID FK NULL | Named WOD 태깅 |
| `tags` | TEXT[] | |
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

> - `groups` 배열 비어 있으면 Rx'd 단일 그룹
> - 배열 index 순서 = 리더보드 우선순위
> - Rx'd 기록은 `scale_group_id = NULL`로 별도 처리

### 3.3 `wods` (배정된 WOD)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `box_id` | UUID FK | |
| `coach_id` | UUID FK | 배정 코치 |
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
| `emom_duration` | INT NULL | |
| `emom_target_reps` | INT NULL | |
| `weight_unit` | ENUM NULL | |
| `scale_groups` | JSONB | wod_templates와 동일 구조 (snapshot) |
| `competition_mode` | ENUM | `none` \| `individual` \| `team` |
| `named_wod_id` | UUID FK NULL | |
| `published_at` | TIMESTAMP NULL | NULL=draft, 미래=scheduled, 과거=published |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |

> `wod_type` 변경 규칙: `wod_records`가 1개라도 존재하면 변경 완전 차단 (`409 Conflict`)

### 3.4 `wod_class_instances` (WOD ↔ 수업 연결)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `wod_id` | UUID FK | |
| `class_instance_id` | UUID FK | |
| `display_order` | INT | 같은 수업 내 WOD 순서 (Part A=1, Part B=2) |
| **PK** | `(wod_id, class_instance_id)` | |

### 3.5 `wod_records` (개인 기록)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `wod_id` | UUID FK | |
| `member_id` | UUID FK | |
| `box_id` | UUID FK | |
| `team_id` | UUID FK NULL | 팀전 소속 팀 |
| `scale_group_id` | VARCHAR(50) NULL | NULL = Rx'd |
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

| WOD 타입 | 사용 필드 |
|---|---|
| For Time | `result_time_seconds` |
| AMRAP | `result_rounds`, `result_reps` |
| EMOM | `result_reps` |
| Max Weight | `result_weight` |
| Custom | `result_custom` |

### 3.6 `wod_teams` (팀전 팀)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `wod_id` | UUID FK | |
| `box_id` | UUID FK | |
| `name` | VARCHAR(100) | 팀명 (기본값: 팀원 이름 이어붙임) |
| `gender_category` | ENUM | `men` \| `women` \| `mixed` (자동 계산) |
| `scale_group_id` | VARCHAR(50) NULL | NULL = Rx'd |
| `result_time_seconds` | INT NULL | |
| `result_rounds` | INT NULL | |
| `result_reps` | INT NULL | |
| `result_weight` | DECIMAL(6,2) NULL | |
| `result_custom` | TEXT NULL | |
| `result_status` | ENUM | `completed` \| `dnf` |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |

### 3.7 `wod_team_members` (팀 구성원)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `team_id` | UUID FK | |
| `member_id` | UUID FK | |
| `role` | ENUM | `leader` \| `member` |
| **PK** | `(team_id, member_id)` | |

### 3.8 `named_wods` (벤치마크 WOD 마스터)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | UUID PK | |
| `name` | VARCHAR(100) | 예: "Fran", "Murph" |
| `category` | ENUM | `the_girls` \| `hero` \| `other` |
| `description` | TEXT | 공식 내용 |
| `default_wod_type` | ENUM | 기본 측정 타입 |

### 3.9 `movements` / `member_1rm_records` (1RM 기록)

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
| `wod_record_id` | UUID FK NULL | WOD 기록 연동 시 |
| `created_at` | TIMESTAMP | |

---

## 4. API 명세

### API Prefix 규칙
- **코치/어드민**: `/api/v1/admin/boxes/:boxId/...`
- **회원 (추후)**: `/api/v1/boxes/:boxId/...`

### 4.1 박스 WOD 설정

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

### 4.2 WOD 은행 (Templates)

> 모든 엔드포인트: Head Coach 이상

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
      { "id": "bg", "description": "20/15kg, 링로우" }
    ]
  },
  "competition_mode": "individual",
  "named_wod_id": "uuid-fran",
  "tags": ["전신", "benchmark"]
}
```

#### `GET /api/v1/admin/boxes/:boxId/wod-templates`

**Query Params**: `q`, `wod_type`, `named_wod_id`, `tags`, `page`, `limit`

#### `GET /api/v1/admin/boxes/:boxId/wod-templates/:templateId`

#### `PATCH /api/v1/admin/boxes/:boxId/wod-templates/:templateId`

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

**서버 처리**:
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

### 4.3 WOD CRUD

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
    "groups": [{ "id": "scaled", "description": "무게 조절" }]
  },
  "competition_mode": "none",
  "published_at": "auto"
}
```

#### `GET /api/v1/admin/boxes/:boxId/wods`

**Query Params**: `date`, `from`/`to`, `publish_status`, `competition_mode`, `named_wod_id`

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
```

### 4.4 WOD 기록

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
  "record": { "..." : "..." },
  "is_pr": true,
  "previous_best": { "result_time_seconds": 510 }
}
```

#### `PATCH /api/v1/admin/boxes/:boxId/wods/:wodId/records/:recordId`
Coach 이상

#### `DELETE /api/v1/admin/boxes/:boxId/wods/:wodId/records/:recordId`
Coach 이상 또는 당일 본인

### 4.5 팀 기록

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

**팀명 자동 생성**: `name = null` → leader 이름 우선, member 순, `" · "` 구분자
**`gender_category` 자동 계산**: 전원 male→`men`, 전원 female→`women`, 혼성→`mixed`

**Validation**:
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
팀원 변경 시 `gender_category` 자동 재계산

#### `DELETE /api/v1/admin/boxes/:boxId/wods/:wodId/teams/:teamId`

### 4.6 리더보드

#### `GET /api/v1/admin/boxes/:boxId/wods/:wodId/leaderboard`

**Query Params**: `gender` (`male`|`female`|`all`), `scale_group_id` (`null`|`scaled`|`all`), `mode` (`individual`|`team`), `gender_category` (`men`|`women`|`mixed`|`all`)

**정렬 로직**
```
1. scale_group_id IS NULL 최상단 (Rx'd)
2. scale_groups.groups 배열 index 순
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
  "wod": { "..." : "..." },
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
    }
  ],
  "total_count": 24
}
```

**Response (팀전)**
```json
{
  "wod": { "..." : "..." },
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
          { "name": "홍길동", "gender": "male", "role": "leader" }
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

> `gender_composition`: API 응답에서 계산, DB 저장 안 함

### 4.7 Named WOD 벤치마크

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

### 4.8 스케줄러

**`WodPublishScheduler`** — 매 5분 실행

```
1. published_at <= NOW() AND published_at > NOW() - interval '5 minutes' 인 wods 조회
2. 연결된 class_instance 예약자 대상 푸시 알림 발송
3. WebSocket / SSE 이벤트 발행
```

---

## 5. 비즈니스 규칙

### 5.1 공개 상태 판단

```sql
-- publish_status (API 응답에서 계산, DB 컬럼 없음)
CASE
  WHEN published_at IS NULL        THEN 'draft'
  WHEN published_at > NOW()        THEN 'scheduled'
  ELSE                                  'published'
END
```

### 5.2 PR 감지

| WOD 타입 | 조건 |
|---|---|
| For Time | `new_time < prev_best` (DNF 제외) |
| AMRAP | `(rounds, reps) > prev_best` (tuple 비교) |
| EMOM | `new_reps > prev_best` |
| Max Weight | `new_weight > prev_best` |
| Custom | 없음 |

> - 동일 `member_id + wod_id` 내 이전 기록 비교
> - Named WOD 통산 PR은 `named_wod_id` 기준 별도 집계

### 5.3 Template → WOD Snapshot 규칙

- deploy 시 template 전 필드 복사 + overrides 병합
- 이후 template 수정은 기배정 wod 무영향
- template 삭제 시 `wods.template_id → NULL`

### 5.4 wod_type 변경 차단

- `wod_records` 1개라도 존재 → `409 Conflict`
- ResponseCode: `WOD_TYPE_CHANGE_BLOCKED`

### 5.5 Scale Group id 변경 차단

- 해당 group id 참조하는 `wod_records` 존재 시 id 변경 불가

### 5.6 ClassInstance 연결 규칙

- 1개 수업에 복수 WOD 가능 (Part A / Part B)
- `display_order`로 수업 내 순서 보장
- ClassInstance 취소 시 WOD·기록 유지
- WOD 삭제 시 `wod_class_instances` cascade 삭제

### 5.7 Competition Mode 규칙

| mode | 기록 대상 | 팀 생성 |
|---|---|---|
| `none` | `wod_records` | 불가 |
| `individual` | `wod_records` | 불가 |
| `team` | `wod_teams` | 가능 |

### 5.8 팀명 자동 생성 규칙

- `name = null` → leader 우선 + member 순, `" · "` 구분자
- 코치 직접 수정 후 팀원 변경 시 자동 갱신 안 함

### 5.9 `gender_category` 자동 계산

- 전원 male → `men`, 전원 female → `women`, 혼성 → `mixed`
- 팀원 변경 시 서버 재계산
- `gender_composition` (예: `"2M/1W"`)은 API 응답에서 실시간 계산, DB 저장 안 함

### 5.10 권한 매트릭스

| 기능 | Coach | Head Coach | Super Admin |
|---|---|---|---|
| WOD 은행 접근/생성/수정/삭제 | X | O | O |
| WOD 조회/수정 | O | O | O |
| WOD 등록/삭제 | X | O | O |
| 공개 설정 변경 | O | O | O |
| 박스 WOD 설정 변경 | X | O | O |
| 타인 기록 입력/수정/삭제 | O | O | O |
| 팀 생성/수정/삭제 | O | O | O |

---

## 6. 구현 단계

### Phase 1: Domain + Enum

`wod/domain/` 디렉토리에 순수 도메인 객체와 enum 생성.

**생성 파일:**
```
wod/domain/
├── BoxWodSettings.kt
├── WodTemplate.kt
├── Wod.kt
├── WodClassInstance.kt
├── WodRecord.kt
├── WodTeam.kt
├── WodTeamMember.kt
├── NamedWod.kt
├── Movement.kt
├── Member1rmRecord.kt
└── enums/
    ├── WodType.kt           # FOR_TIME, AMRAP, EMOM, MAX_WEIGHT, CUSTOM
    ├── PublishPolicy.kt      # MANUAL, SCHEDULED
    ├── PublishAnchor.kt      # CLASS_START, FIXED_TIME_OF_DAY
    ├── CompetitionMode.kt    # NONE, INDIVIDUAL, TEAM
    ├── ResultStatus.kt       # COMPLETED, DNF
    ├── WeightUnit.kt         # KG, LB
    ├── GenderCategory.kt     # MEN, WOMEN, MIXED
    ├── TeamMemberRole.kt     # LEADER, MEMBER
    ├── NamedWodCategory.kt   # THE_GIRLS, HERO, OTHER
    └── MovementCategory.kt   # BARBELL, GYMNASTIC, CARDIO, OTHER
```

**규칙:**
- `data class` 사용, `id: UUID? = null`
- `audit: AuditInfo = AuditInfo.empty()` 포함
- Spring/JPA import 없음
- 비즈니스 로직 메서드 포함 (예: `Wod.publishStatus()`, `Wod.isPublished()`)
- `ScaleGroups` 값 객체 별도 정의 (groups 리스트 래핑)

### Phase 2: Application Port (UseCase, PersistencePort, Command/Query DTO)

**생성 파일:**
```
wod/application/
├── port/
│   ├── input/
│   │   ├── WodSettingsCommandUseCase.kt
│   │   ├── WodSettingsQueryUseCase.kt
│   │   ├── WodTemplateCommandUseCase.kt
│   │   ├── WodTemplateQueryUseCase.kt
│   │   ├── WodCommandUseCase.kt
│   │   ├── WodQueryUseCase.kt
│   │   ├── WodRecordCommandUseCase.kt
│   │   ├── WodRecordQueryUseCase.kt
│   │   ├── WodTeamCommandUseCase.kt
│   │   ├── WodTeamQueryUseCase.kt
│   │   ├── LeaderboardQueryUseCase.kt
│   │   ├── BenchmarkQueryUseCase.kt
│   │   └── dto/
│   │       ├── commands.kt        # CreateWodCommand, UpdateWodCommand, DeployTemplateCommand 등
│   │       └── results.kt         # WodListResult, LeaderboardResult, BenchmarkResult 등
│   └── output/
│       ├── WodSettingsPersistencePort.kt
│       ├── WodTemplatePersistencePort.kt
│       ├── WodPersistencePort.kt
│       ├── WodClassInstancePersistencePort.kt
│       ├── WodRecordPersistencePort.kt
│       ├── WodTeamPersistencePort.kt
│       ├── NamedWodPersistencePort.kt
│       └── MovementPersistencePort.kt
└── service/
    # (Phase 5에서 구현)
```

### Phase 3: Adapter Out (JPA Entity, Repository, PersistenceAdapter)

**생성 파일:**
```
wod/adapter/out/persistence/
├── entity/
│   ├── BoxWodSettingsEntity.kt
│   ├── WodTemplateEntity.kt
│   ├── WodEntity.kt
│   ├── WodClassInstanceEntity.kt
│   ├── WodRecordEntity.kt
│   ├── WodTeamEntity.kt
│   ├── WodTeamMemberEntity.kt
│   ├── NamedWodEntity.kt
│   ├── MovementEntity.kt
│   └── Member1rmRecordEntity.kt
├── repository/
│   ├── BoxWodSettingsJpaRepository.kt
│   ├── WodTemplateJpaRepository.kt
│   ├── WodJpaRepository.kt
│   ├── WodClassInstanceJpaRepository.kt
│   ├── WodRecordJpaRepository.kt
│   ├── WodTeamJpaRepository.kt
│   ├── WodTeamMemberJpaRepository.kt
│   ├── NamedWodJpaRepository.kt
│   ├── MovementJpaRepository.kt
│   └── Member1rmRecordJpaRepository.kt
├── WodSettingsPersistenceAdapter.kt
├── WodTemplatePersistenceAdapter.kt
├── WodPersistenceAdapter.kt
├── WodClassInstancePersistenceAdapter.kt
├── WodRecordPersistenceAdapter.kt
├── WodTeamPersistenceAdapter.kt
├── NamedWodPersistenceAdapter.kt
└── MovementPersistenceAdapter.kt
```

**주의사항:**
- `scale_groups` JSONB 필드는 `@Type(JsonType::class)` 또는 `@JdbcTypeCode(SqlTypes.JSON)` 사용
- `WodClassInstanceEntity`는 복합키 `@IdClass` 또는 `@EmbeddedId` 사용
- `WodTeamMemberEntity`도 복합키
- 모든 Entity는 `BaseEntity` 상속 (`box_wod_settings` 제외 가능)
- `toDomain()`, `fromDomain()` 변환 메서드 필수

### Phase 4: Adapter In (Controller, Request/Response DTO)

**생성 파일:**
```
wod/adapter/input/web/
├── AdminWodSettingsController.kt
├── AdminWodTemplateController.kt
├── AdminWodController.kt
├── AdminWodRecordController.kt
├── AdminWodTeamController.kt
├── AdminLeaderboardController.kt
├── AdminBenchmarkController.kt
├── NamedWodController.kt
└── dto/
    ├── request/
    │   ├── UpdateWodSettingsRequest.kt
    │   ├── CreateWodTemplateRequest.kt
    │   ├── UpdateWodTemplateRequest.kt
    │   ├── DeployTemplateRequest.kt
    │   ├── CreateWodRequest.kt
    │   ├── UpdateWodRequest.kt
    │   ├── UpdateClassInstancesRequest.kt
    │   ├── UpdatePublishRequest.kt
    │   ├── CreateWodRecordRequest.kt
    │   ├── UpdateWodRecordRequest.kt
    │   ├── CreateWodTeamRequest.kt
    │   └── UpdateWodTeamRequest.kt
    └── response/
        ├── WodSettingsResponse.kt
        ├── WodTemplateListResponse.kt
        ├── WodTemplateDetailResponse.kt
        ├── DeployResponse.kt
        ├── WodListResponse.kt
        ├── WodDetailResponse.kt
        ├── WodRecordResponse.kt
        ├── WodTeamResponse.kt
        ├── LeaderboardResponse.kt
        ├── BenchmarkResponse.kt
        └── NamedWodResponse.kt
```

**ResponseCode 추가 (5000~5999 범위):**
```kotlin
// WOD (5000 ~ 5999)
WOD_NOT_FOUND(HttpStatus.NOT_FOUND, "5000", "존재하지 않는 WOD입니다."),
WOD_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "5001", "존재하지 않는 WOD 템플릿입니다."),
WOD_TYPE_CHANGE_BLOCKED(HttpStatus.CONFLICT, "5002", "기록이 존재하는 WOD의 타입은 변경할 수 없습니다."),
WOD_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "5003", "존재하지 않는 기록입니다."),
WOD_TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "5004", "존재하지 않는 팀입니다."),
WOD_TEAM_DUPLICATE_MEMBER(HttpStatus.CONFLICT, "5005", "이미 다른 팀에 소속된 회원입니다."),
WOD_TEAM_INVALID_LEADER(HttpStatus.BAD_REQUEST, "5006", "리더는 정확히 1명이어야 합니다."),
WOD_NOT_TEAM_MODE(HttpStatus.BAD_REQUEST, "5007", "팀전 모드가 아닌 WOD입니다."),
WOD_SCALE_GROUP_CHANGE_BLOCKED(HttpStatus.CONFLICT, "5008", "기록이 있는 스케일 그룹의 ID는 변경할 수 없습니다."),
WOD_SETTINGS_NOT_FOUND(HttpStatus.NOT_FOUND, "5009", "WOD 설정이 존재하지 않습니다."),
```

### Phase 5: Service 구현 + 스케줄러

**생성 파일:**
```
wod/application/service/
├── WodSettingsCommandService.kt
├── WodSettingsQueryService.kt
├── WodTemplateCommandService.kt
├── WodTemplateQueryService.kt
├── WodCommandService.kt
├── WodQueryService.kt
├── WodRecordCommandService.kt
├── WodRecordQueryService.kt
├── WodTeamCommandService.kt
├── WodTeamQueryService.kt
├── LeaderboardQueryService.kt
├── BenchmarkQueryService.kt
├── PublishAtCalculator.kt         # published_at 자동 계산 유틸
└── PrDetector.kt                  # PR 감지 유틸

wod/adapter/input/scheduler/
└── WodPublishScheduler.kt         # 매 5분 실행
```

**핵심 구현 포인트:**

1. **`PublishAtCalculator`**: `box_wod_settings` + `wod.date` + `class_instance.startTime` 기반 `published_at` UTC Instant 계산
2. **`PrDetector`**: WOD 타입별 PR 비교 로직. Named WOD 통산 PR은 `named_wod_id` 기준
3. **`LeaderboardQueryService`**: scale_group 우선순위 + WOD 타입별 정렬 + DNF 최하단
4. **`WodPublishScheduler`**: `@Scheduled(fixedRate = 300_000)` 매 5분, 공개 대상 조회 + 알림
5. **`WodTemplateCommandService.deploy()`**: snapshot 생성 + overrides 병합 + class_instances 연결

---

## 7. 참고사항

- `gender_composition` (예: `"2M/1W"`)은 DB 저장 안 함, API 응답 시 실시간 계산
- `publish_status`는 DB 컬럼 없음, `published_at` 기반 실시간 계산
- 설정 변경은 이후 새로 배정되는 WOD부터 적용 (기존 `published_at` 소급 적용 없음)
- EMOM Death by 패턴은 MVP에서 Custom 타입으로 우회
- `result_display` (예: `"8:00"`, `"102.5kg"`)는 서버에서 포매팅하여 응답
