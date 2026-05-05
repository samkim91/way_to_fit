package com.waytofit.competition.application.port.`in`

import com.waytofit.competition.domain.CompetitionScore
import com.waytofit.competition.domain.enums.ScoreStatus
import com.waytofit.competition.domain.enums.ResultStatus
import java.math.BigDecimal
import java.util.UUID

interface SubmitScoreUseCase {
    fun submitScore(command: SubmitScoreCommand, userId: UUID): CompetitionScore
}

interface GetScoreUseCase {
    fun getScore(eventId: UUID, registrationId: UUID): CompetitionScore?
}

interface OrganizerScoreQueryUseCase {
    fun getScoresByEventId(eventId: UUID, status: ScoreStatus?, userId: UUID): List<CompetitionScore>
}

interface ReviewScoreUseCase {
    fun reviewScore(command: ReviewScoreCommand, userId: UUID): CompetitionScore
}

data class SubmitScoreCommand(
    val eventId: UUID,
    val registrationId: UUID,
    val resultStatus: ResultStatus,
    val resultTimeSeconds: Int? = null,
    val resultRounds: Int? = null,
    val resultReps: Int? = null,
    val resultWeight: BigDecimal? = null,
    val resultCustom: String? = null,
    val videoUrl: String,
)

data class ReviewScoreCommand(
    val scoreId: UUID,
    val status: ScoreStatus,
    val adjustedResultTimeSeconds: Int? = null,
    val adjustedResultRounds: Int? = null,
    val adjustedResultReps: Int? = null,
    val adjustedResultWeight: BigDecimal? = null,
    val adjustedResultCustom: String? = null,
    val reviewerNote: String? = null,
)
