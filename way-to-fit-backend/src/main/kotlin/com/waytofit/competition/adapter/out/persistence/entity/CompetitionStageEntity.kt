package com.waytofit.competition.adapter.out.persistence.entity

import com.waytofit.competition.domain.CompetitionStage
import com.waytofit.competition.domain.enums.StageFormat
import com.waytofit.competition.domain.enums.StageType
import com.waytofit.global.domain.AuditInfo
import com.waytofit.global.persistence.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "competition_stages")
@SQLRestriction("deleted_at IS NULL")
class CompetitionStageEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "competition_id", nullable = false)
    val competitionId: UUID,

    @Column(name = "name", nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_type", nullable = false)
    val stageType: StageType,

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_format", nullable = false)
    val stageFormat: StageFormat,

    @Column(name = "start_at", nullable = false)
    val startAt: Instant,

    @Column(name = "end_at", nullable = false)
    val endAt: Instant,
) : BaseEntity() {

    fun toDomain(): CompetitionStage = CompetitionStage(
        id = id,
        competitionId = competitionId,
        name = name,
        stageType = stageType,
        stageFormat = stageFormat,
        startAt = startAt,
        endAt = endAt,
        audit = AuditInfo(
            createdAt = createdAt,
            createdBy = createdBy,
            lastModifiedAt = lastModifiedAt,
            lastModifiedBy = lastModifiedBy
        )
    )

    companion object {
        fun fromDomain(domain: CompetitionStage): CompetitionStageEntity = CompetitionStageEntity(
            id = domain.id,
            competitionId = domain.competitionId,
            name = domain.name,
            stageType = domain.stageType,
            stageFormat = domain.stageFormat,
            startAt = domain.startAt,
            endAt = domain.endAt
        )
    }
}

@Entity
@Table(name = "competition_stage_finalists")
class CompetitionStageFinalistEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "stage_id", nullable = false)
    val stageId: UUID,

    @Column(name = "registration_id", nullable = false)
    val registrationId: UUID,
)
