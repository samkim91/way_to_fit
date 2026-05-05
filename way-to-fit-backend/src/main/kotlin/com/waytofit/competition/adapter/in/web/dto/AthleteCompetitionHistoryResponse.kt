package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.competition.domain.enums.ResultStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class AthleteCompetitionHistoryResponse(
    val userId: UUID,
    val items: List<CompetitionHistoryItem>
)

data class CompetitionHistoryItem(
    val competitionId: UUID,
    val name: String,
    val bannerImageUrl: String?,
    val endAt: Instant,
    val registrationId: UUID,
    val registrationType: RegistrationType,
    val scaleCategory: String,
    val overallRank: Int?,
    val totalPoints: Int?,
    val eventScores: List<EventScoreItem>
)

data class EventScoreItem(
    val eventId: UUID,
    val eventName: String,
    val rank: Int?,
    val resultStatus: ResultStatus?,
    val resultTimeSeconds: Int?,
    val resultRounds: Int?,
    val resultReps: Int?,
    val resultWeight: BigDecimal?,
    val resultCustom: String?
)
