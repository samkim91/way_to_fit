package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.Competition
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CompetitionRepository {
    fun save(competition: Competition): Competition
    fun findById(id: UUID): Competition?
    fun findAllExcludingDrafts(pageable: Pageable): Page<Competition>
    fun findAllByIds(ids: List<UUID>): List<Competition>
    fun findMyCompetitions(userId: UUID, statuses: List<CompetitionStatus>?, pageable: Pageable): Page<Competition>
}
