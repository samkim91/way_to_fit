package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.application.port.`in`.EventLeaderboardResult
import com.waytofit.competition.application.port.`in`.LeaderboardEntry
import com.waytofit.competition.application.port.`in`.OverallLeaderboardResult
import com.waytofit.competition.application.port.`in`.OverallLeaderboardEntry
import com.waytofit.competition.domain.enums.ResultStatus
import java.math.BigDecimal
import java.util.UUID

data class EventLeaderboardResponse(
    val eventId: UUID,
    val entries: List<LeaderboardEntryResponse>,
) {
    companion object {
        fun fromResult(result: EventLeaderboardResult) = EventLeaderboardResponse(
            eventId = result.eventId,
            entries = result.entries.map { LeaderboardEntryResponse.fromEntry(it) }
        )
    }
}

data class OverallLeaderboardResponse(
    val stageId: UUID,
    val entries: List<OverallLeaderboardEntryResponse>,
) {
    companion object {
        fun fromResult(result: OverallLeaderboardResult) = OverallLeaderboardResponse(
            stageId = result.stageId,
            entries = result.entries.map { OverallLeaderboardEntryResponse.fromEntry(it) }
        )
    }
}

data class LeaderboardEntryResponse(
    val rank: Int,
    val registrationId: UUID,
    val participantName: String,
    val scaleCategory: String,
    val resultTimeSeconds: Int?,
    val resultRounds: Int?,
    val resultReps: Int?,
    val resultWeight: BigDecimal?,
    val resultCustom: String?,
    val resultStatus: ResultStatus?,
    val videoUrl: String,
    val memberIds: List<UUID>,
) {
    companion object {
        fun fromEntry(entry: LeaderboardEntry) = LeaderboardEntryResponse(
            rank = entry.rank,
            registrationId = entry.registrationId,
            participantName = entry.participantName,
            scaleCategory = entry.scaleCategory,
            resultTimeSeconds = entry.resultTimeSeconds,
            resultRounds = entry.resultRounds,
            resultReps = entry.resultReps,
            resultWeight = entry.resultWeight,
            resultCustom = entry.resultCustom,
            resultStatus = entry.resultStatus,
            videoUrl = entry.videoUrl,
            memberIds = entry.memberIds
        )
    }
}

data class OverallLeaderboardEntryResponse(
    val rank: Int,
    val registrationId: UUID,
    val participantName: String,
    val scaleCategory: String,
    val totalPoints: Int,
    val eventRanks: Map<UUID, Int>,
    val manualRank: Int?,
    val memberIds: List<UUID>,
) {
    companion object {
        fun fromEntry(entry: OverallLeaderboardEntry) = OverallLeaderboardEntryResponse(
            rank = entry.rank,
            registrationId = entry.registrationId,
            participantName = entry.participantName,
            scaleCategory = entry.scaleCategory,
            totalPoints = entry.totalPoints,
            eventRanks = entry.eventRanks,
            manualRank = entry.manualRank,
            memberIds = entry.memberIds
        )
    }
}

data class OverrideRankRequest(
    val manualRank: Int?,
) {
    fun toCommand(registrationId: UUID) = com.waytofit.competition.application.port.`in`.OverrideRankCommand(
        registrationId = registrationId,
        manualRank = manualRank
    )
}
