package com.waytofit.competition.domain

import java.util.UUID

data class CompetitionOrganizer(
    val competitionId: UUID,
    val userId: UUID,
)
