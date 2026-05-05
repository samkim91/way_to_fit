package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionOrganizerEntity
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionOrganizerJpaRepository
import com.waytofit.competition.application.port.out.CompetitionOrganizerRepository
import com.waytofit.competition.domain.CompetitionOrganizer
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CompetitionOrganizerPersistenceAdapter(
    private val jpaRepository: CompetitionOrganizerJpaRepository,
) : CompetitionOrganizerRepository {

    override fun isOrganizer(competitionId: UUID, userId: UUID): Boolean {
        return jpaRepository.existsByCompetitionIdAndUserId(competitionId, userId)
    }

    override fun saveOrganizer(competitionId: UUID, userId: UUID): CompetitionOrganizer {
        return jpaRepository.save(CompetitionOrganizerEntity(competitionId = competitionId, userId = userId)).toDomain()
    }

    override fun findCompetitionIdsByUserId(userId: UUID): List<UUID> {
        return jpaRepository.findAllByUserId(userId).map { it.competitionId }
    }
}
