package com.waytofit.competition.application.port.`in`

import com.waytofit.competition.adapter.`in`.web.dto.AthleteCompetitionHistoryResponse
import java.util.UUID

interface GetAthleteCompetitionHistoryUseCase {
    fun getAthleteCompetitionHistory(userId: UUID): AthleteCompetitionHistoryResponse
}
