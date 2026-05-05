package com.waytofit.competition.adapter.out.persistence.entity

import com.waytofit.competition.domain.CompetitionEvent
import com.waytofit.competition.domain.enums.EventType
import com.waytofit.global.domain.AuditInfo
import com.waytofit.global.persistence.BaseEntity
import com.waytofit.competition.domain.enums.GenderCategory
import com.waytofit.competition.domain.enums.WeightUnit
import com.waytofit.competition.domain.enums.WodType
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "competition_events")
@SQLRestriction("deleted_at IS NULL")
class CompetitionEventEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "stage_id", nullable = false)
    val stageId: UUID,

    @Column(name = "competition_id", nullable = false)
    val competitionId: UUID,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    val eventType: EventType,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    val gender: GenderCategory,

    @Enumerated(EnumType.STRING)
    @Column(name = "wod_type", nullable = false)
    val wodType: WodType,

    @Column(name = "time_cap")
    val timeCap: Int? = null,

    @Column(name = "amrap_duration")
    val amrapDuration: Int? = null,

    @Column(name = "emom_duration")
    val emomDuration: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_unit")
    val weightUnit: WeightUnit? = null,

    @Column(name = "event_order", nullable = false)
    val order: Int,

    @ElementCollection
    @CollectionTable(
        name = "competition_event_scale_categories",
        joinColumns = [JoinColumn(name = "event_id")]
    )
    @Column(name = "category_name")
    val scaleCategories: List<String>,

    @Column(name = "release_at")
    val releaseAt: Instant? = null,

    @Column(name = "submission_deadline", nullable = false)
    val submissionDeadline: Instant,
) : BaseEntity() {

    fun toDomain(): CompetitionEvent = CompetitionEvent(
        id = id,
        stageId = stageId,
        competitionId = competitionId,
        name = name,
        description = description,
        eventType = eventType,
        gender = gender,
        wodType = wodType,
        timeCap = timeCap,
        amrapDuration = amrapDuration,
        emomDuration = emomDuration,
        weightUnit = weightUnit,
        order = order,
        scaleCategories = scaleCategories,
        releaseAt = releaseAt,
        submissionDeadline = submissionDeadline,
        audit = AuditInfo(
            createdAt = createdAt,
            createdBy = createdBy,
            lastModifiedAt = lastModifiedAt,
            lastModifiedBy = lastModifiedBy
        )
    )

    companion object {
        fun fromDomain(domain: CompetitionEvent): CompetitionEventEntity = CompetitionEventEntity(
            id = domain.id,
            stageId = domain.stageId,
            competitionId = domain.competitionId,
            name = domain.name,
            description = domain.description,
            eventType = domain.eventType,
            gender = domain.gender,
            wodType = domain.wodType,
            timeCap = domain.timeCap,
            amrapDuration = domain.amrapDuration,
            emomDuration = domain.emomDuration,
            weightUnit = domain.weightUnit,
            order = domain.order,
            scaleCategories = domain.scaleCategories,
            releaseAt = domain.releaseAt,
            submissionDeadline = domain.submissionDeadline
        )
    }
}
