package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionOrganizerEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CompetitionOrganizerJpaRepository : JpaRepository<CompetitionOrganizerEntity, UUID> {
    fun existsByCompetitionIdAndUserId(competitionId: UUID, userId: UUID): Boolean
    fun findAllByUserId(userId: UUID): List<CompetitionOrganizerEntity>
}
