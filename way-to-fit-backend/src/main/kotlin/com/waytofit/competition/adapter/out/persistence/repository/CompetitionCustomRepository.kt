package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionEntity
import com.waytofit.competition.domain.enums.CompetitionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CompetitionCustomRepository {
    fun findMyCompetitions(userId: UUID, statuses: List<CompetitionStatus>?, pageable: Pageable): Page<CompetitionEntity>
}
