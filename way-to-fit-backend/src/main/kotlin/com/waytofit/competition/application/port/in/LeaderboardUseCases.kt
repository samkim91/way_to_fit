package com.waytofit.competition.application.port.`in`

import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.user.domain.enums.Gender
import com.waytofit.competition.domain.enums.ResultStatus
import java.math.BigDecimal
import java.util.UUID

interface GetEventLeaderboardUseCase {
    fun getEventLeaderboard(query: GetEventLeaderboardQuery): EventLeaderboardResult
}

interface GetOverallLeaderboardUseCase {
    fun getOverallLeaderboard(query: GetOverallLeaderboardQuery): OverallLeaderboardResult
}

interface OverrideRankUseCase {
    fun overrideRank(command: OverrideRankCommand, userId: UUID)
}

data class GetEventLeaderboardQuery(
    val eventId: UUID,
    val gender: Gender?,
    val scaleCategory: String?,
)

data class GetOverallLeaderboardQuery(
    val stageId: UUID,
    val registrationType: RegistrationType?,
    val gender: Gender?,
    val scaleCategory: String?,
)

data class EventLeaderboardResult(
    val eventId: UUID,
    val entries: List<LeaderboardEntry>,
)

data class OverallLeaderboardResult(
    val stageId: UUID,
    val entries: List<OverallLeaderboardEntry>,
)

data class LeaderboardEntry(
    val rank: Int,
    val registrationId: UUID,
    val registrationType: RegistrationType,
    val participantName: String,
    val scaleCategory: String,
    val resultStatus: ResultStatus,
    val resultTimeSeconds: Int?,
    val resultRounds: Int?,
    val resultReps: Int?,
    val resultWeight: BigDecimal?,
    val resultCustom: String?,
    val videoUrl: String,
    val memberIds: List<UUID>,
    val manualRank: Int? = null,
)

data class OverallLeaderboardEntry(
    val rank: Int,
    val registrationId: UUID,
    val participantName: String,
    val scaleCategory: String,
    val totalPoints: Int,
    val eventRanks: Map<UUID, Int>,
    val manualRank: Int?,
    val memberIds: List<UUID>,
)

data class OverrideRankCommand(
    val registrationId: UUID,
    val manualRank: Int?,
)
