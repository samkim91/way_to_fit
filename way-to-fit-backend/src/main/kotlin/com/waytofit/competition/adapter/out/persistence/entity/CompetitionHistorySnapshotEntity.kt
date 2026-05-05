package com.waytofit.competition.adapter.out.persistence.entity

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.waytofit.competition.domain.CompetitionHistorySnapshot
import com.waytofit.competition.domain.EventScoreSnapshot
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.global.persistence.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "competition_history_snapshots",
    indexes = [
        Index(name = "idx_snapshot_user_id", columnList = "user_id")
    ]
)
class CompetitionHistorySnapshotEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "competition_id", nullable = false)
    val competitionId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "registration_id", nullable = false)
    val registrationId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_type", nullable = false)
    val registrationType: RegistrationType,

    @Column(name = "scale_category", nullable = false)
    val scaleCategory: String,

    @Column(name = "competition_name", nullable = false)
    val competitionName: String,

    @Column(name = "banner_image_url")
    val bannerImageUrl: String? = null,

    @Column(name = "competition_end_at", nullable = false)
    val competitionEndAt: Instant,

    @Column(name = "overall_rank")
    val overallRank: Int? = null,

    @Column(name = "total_points")
    val totalPoints: Int? = null,

    @Column(name = "event_scores_json", columnDefinition = "TEXT", nullable = false)
    val eventScoresJson: String
) : BaseEntity() {

    fun toDomain(): CompetitionHistorySnapshot {
        val mapper = jacksonObjectMapper()
        return CompetitionHistorySnapshot(
            id = id,
            competitionId = competitionId,
            userId = userId,
            registrationId = registrationId,
            registrationType = registrationType,
            scaleCategory = scaleCategory,
            competitionName = competitionName,
            bannerImageUrl = bannerImageUrl,
            competitionEndAt = competitionEndAt,
            overallRank = overallRank,
            totalPoints = totalPoints,
            eventScores = mapper.readValue(eventScoresJson)
        )
    }

    companion object {
        fun fromDomain(domain: CompetitionHistorySnapshot): CompetitionHistorySnapshotEntity {
            val mapper = jacksonObjectMapper()
            return CompetitionHistorySnapshotEntity(
                id = domain.id,
                competitionId = domain.competitionId,
                userId = domain.userId,
                registrationId = domain.registrationId,
                registrationType = domain.registrationType,
                scaleCategory = domain.scaleCategory,
                competitionName = domain.competitionName,
                bannerImageUrl = domain.bannerImageUrl,
                competitionEndAt = domain.competitionEndAt,
                overallRank = domain.overallRank,
                totalPoints = domain.totalPoints,
                eventScoresJson = mapper.writeValueAsString(domain.eventScores)
            )
        }
    }
}
