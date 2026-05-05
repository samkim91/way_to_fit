package com.waytofit.competition.adapter.out.persistence.entity

import com.waytofit.competition.domain.CompetitionScore
import com.waytofit.competition.domain.enums.ScoreStatus
import com.waytofit.global.domain.AuditInfo
import com.waytofit.global.persistence.BaseEntity
import com.waytofit.competition.domain.enums.ResultStatus
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(
    name = "competition_scores",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["event_id", "registration_id"])
    ]
)
@SQLRestriction("deleted_at IS NULL")
class CompetitionScoreEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "event_id", nullable = false)
    val eventId: UUID,

    @Column(name = "registration_id", nullable = false)
    val registrationId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false)
    val resultStatus: ResultStatus,

    @Column(name = "result_time_seconds")
    val resultTimeSeconds: Int? = null,

    @Column(name = "result_rounds")
    val resultRounds: Int? = null,

    @Column(name = "result_reps")
    val resultReps: Int? = null,

    @Column(name = "result_weight", precision = 10, scale = 2)
    val resultWeight: BigDecimal? = null,

    @Column(name = "result_custom", columnDefinition = "TEXT")
    val resultCustom: String? = null,

    @Column(name = "video_url", nullable = false)
    val videoUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: ScoreStatus,

    @Column(name = "reviewer_note", columnDefinition = "TEXT")
    val reviewerNote: String? = null,

    @Column(name = "adjusted_by")
    val adjustedBy: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", insertable = false, updatable = false)
    val registration: CompetitionRegistrationEntity? = null,
) : BaseEntity() {

    fun toDomain(): CompetitionScore = CompetitionScore(
        id = id,
        eventId = eventId,
        registrationId = registrationId,
        resultStatus = resultStatus,
        resultTimeSeconds = resultTimeSeconds,
        resultRounds = resultRounds,
        resultReps = resultReps,
        resultWeight = resultWeight,
        resultCustom = resultCustom,
        videoUrl = videoUrl,
        status = status,
        reviewerNote = reviewerNote,
        adjustedBy = adjustedBy,
        audit = AuditInfo(
            createdAt = createdAt,
            createdBy = createdBy,
            lastModifiedAt = lastModifiedAt,
            lastModifiedBy = lastModifiedBy
        )
    )

    companion object {
        fun fromDomain(domain: CompetitionScore): CompetitionScoreEntity = CompetitionScoreEntity(
            id = domain.id,
            eventId = domain.eventId,
            registrationId = domain.registrationId,
            resultStatus = domain.resultStatus,
            resultTimeSeconds = domain.resultTimeSeconds,
            resultRounds = domain.resultRounds,
            resultReps = domain.resultReps,
            resultWeight = domain.resultWeight,
            resultCustom = domain.resultCustom,
            videoUrl = domain.videoUrl,
            status = domain.status,
            reviewerNote = domain.reviewerNote,
            adjustedBy = domain.adjustedBy
        )
    }
}
