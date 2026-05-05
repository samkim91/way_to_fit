package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionScoreEntity
import com.waytofit.competition.domain.enums.ScoreStatus
import java.util.UUID

interface CompetitionScoreCustomRepository {
    fun findAllByEventIdWithDetails(eventId: UUID, status: ScoreStatus?): List<CompetitionScoreEntity>
}
