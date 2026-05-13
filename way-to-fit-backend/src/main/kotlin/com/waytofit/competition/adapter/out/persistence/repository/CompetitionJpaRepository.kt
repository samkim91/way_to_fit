package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionEntity
import com.waytofit.competition.domain.enums.CompetitionVisibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CompetitionJpaRepository : JpaRepository<CompetitionEntity, UUID>, CompetitionCustomRepository {
    fun findAllByVisibility(visibility: CompetitionVisibility, pageable: Pageable): Page<CompetitionEntity>
    fun findAllByIdIn(ids: List<UUID>): List<CompetitionEntity>
}
