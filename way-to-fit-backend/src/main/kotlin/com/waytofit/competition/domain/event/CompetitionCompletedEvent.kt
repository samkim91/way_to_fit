package com.waytofit.competition.domain.event

import java.util.UUID

data class CompetitionCompletedEvent(
    val competitionId: UUID
)
