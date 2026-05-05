package com.waytofit.competition.domain

import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.competition.domain.enums.ResultStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CompetitionHistorySnapshot(
    val id: UUID? = null,
    val competitionId: UUID,
    val userId: UUID,
    val registrationId: UUID,
    val registrationType: RegistrationType,
    val scaleCategory: String,
    val competitionName: String,
    val bannerImageUrl: String?,
    val competitionEndAt: Instant,
    val overallRank: Int?,
    val totalPoints: Int?,
    val eventScores: List<EventScoreSnapshot>
)

data class EventScoreSnapshot(
    val eventId: UUID,
    val eventName: String,
    val rank: Int,
    val resultStatus: ResultStatus,
    val resultTimeSeconds: Int?,
    val resultRounds: Int?,
    val resultReps: Int?,
    val resultWeight: BigDecimal?,
    val resultCustom: String?
)
