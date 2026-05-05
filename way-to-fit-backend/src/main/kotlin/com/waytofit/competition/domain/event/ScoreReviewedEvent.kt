package com.waytofit.competition.domain.event

import com.waytofit.competition.domain.enums.ScoreStatus
import java.util.UUID

data class ScoreReviewedEvent(
    val competitionId: UUID,
    val stageId: UUID,
    val eventId: UUID,
    val scoreId: UUID,
    val status: ScoreStatus
)
