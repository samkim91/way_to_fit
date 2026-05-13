package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionEntity
import com.waytofit.competition.domain.enums.CompetitionLifecycle
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

interface CompetitionCustomRepository {
    fun findMyCompetitions(userId: UUID, lifecycles: List<CompetitionLifecycle>?, now: Instant, pageable: Pageable): Page<CompetitionEntity>
}
