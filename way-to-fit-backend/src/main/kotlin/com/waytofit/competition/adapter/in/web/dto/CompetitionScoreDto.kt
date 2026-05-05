package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.domain.CompetitionScore
import com.waytofit.competition.domain.enums.ScoreStatus
import com.waytofit.competition.domain.enums.ResultStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class SubmitScoreRequest(
    val registrationId: UUID,
    val videoUrl: String,
    val resultTimeSeconds: Int?,
    val resultRounds: Int?,
    val resultReps: Int?,
    val resultWeight: BigDecimal?,
    val resultCustom: String?,
    val resultStatus: ResultStatus,
) {
    fun toCommand(eventId: UUID) = com.waytofit.competition.application.port.`in`.SubmitScoreCommand(
        eventId = eventId,
        registrationId = registrationId,
        videoUrl = videoUrl,
        resultTimeSeconds = resultTimeSeconds,
        resultRounds = resultRounds,
        resultReps = resultReps,
        resultWeight = resultWeight,
        resultCustom = resultCustom,
        resultStatus = resultStatus
    )
}

data class ReviewScoreRequest(
    val status: ScoreStatus,
    val reviewerNote: String?,
    val resultTimeSeconds: Int?,
    val resultRounds: Int?,
    val resultReps: Int?,
    val resultWeight: BigDecimal?,
    val resultCustom: String?,
    val resultStatus: ResultStatus?,
) {
    fun toCommand(scoreId: UUID) = com.waytofit.competition.application.port.`in`.ReviewScoreCommand(
        scoreId = scoreId,
        status = status,
        reviewerNote = reviewerNote,
        adjustedResultTimeSeconds = resultTimeSeconds,
        adjustedResultRounds = resultRounds,
        adjustedResultReps = resultReps,
        adjustedResultWeight = resultWeight,
        adjustedResultCustom = resultCustom,
    )
}

data class ScoreResponse(
    val id: UUID,
    val eventId: UUID,
    val registrationId: UUID,
    val videoUrl: String,
    val status: ScoreStatus,
    val reviewerNote: String?,
    val resultTimeSeconds: Int?,
    val resultRounds: Int?,
    val resultReps: Int?,
    val resultWeight: BigDecimal?,
    val resultCustom: String?,
    val resultStatus: ResultStatus,
    val createdAt: Instant?,
) {
    companion object {
        fun fromDomain(domain: CompetitionScore) = ScoreResponse(
            id = domain.id!!,
            eventId = domain.eventId,
            registrationId = domain.registrationId,
            videoUrl = domain.videoUrl,
            status = domain.status,
            reviewerNote = domain.reviewerNote,
            resultTimeSeconds = domain.resultTimeSeconds,
            resultRounds = domain.resultRounds,
            resultReps = domain.resultReps,
            resultWeight = domain.resultWeight,
            resultCustom = domain.resultCustom,
            resultStatus = domain.resultStatus,
            createdAt = domain.audit.createdAt
        )
    }
}
