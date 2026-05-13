package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.enums.CompetitionLifecycle
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

interface CompetitionRepository {
    fun save(competition: Competition): Competition
    fun findById(id: UUID): Competition?
    fun findAllPublic(pageable: Pageable): Page<Competition>
    fun findAllByIds(ids: List<UUID>): List<Competition>
    fun findMyCompetitions(userId: UUID, lifecycles: List<CompetitionLifecycle>?, now: Instant, pageable: Pageable): Page<Competition>
}
