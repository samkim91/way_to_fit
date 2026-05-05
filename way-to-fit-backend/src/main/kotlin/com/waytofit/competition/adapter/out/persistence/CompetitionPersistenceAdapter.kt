package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionEntity
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionJpaRepository
import com.waytofit.competition.application.port.out.CompetitionRepository
import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.enums.CompetitionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CompetitionPersistenceAdapter(
    private val jpaRepository: CompetitionJpaRepository,
) : CompetitionRepository {

    override fun save(competition: Competition): Competition {
        return jpaRepository.save(CompetitionEntity.fromDomain(competition)).toDomain()
    }

    override fun findById(id: UUID): Competition? {
        return jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAllExcludingDrafts(pageable: Pageable): Page<Competition> {
        return jpaRepository.findAllByStatusNot(CompetitionStatus.DRAFT, pageable).map { it.toDomain() }
    }

    override fun findAllByIds(ids: List<UUID>): List<Competition> {
        return jpaRepository.findAllByIdIn(ids).map { it.toDomain() }
    }

    override fun findMyCompetitions(userId: UUID, statuses: List<CompetitionStatus>?, pageable: Pageable): Page<Competition> {
        return Page.empty()
    }
}
