package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionEntity
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionJpaRepository
import com.waytofit.competition.application.port.out.CompetitionRepository
import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.enums.CompetitionLifecycle
import com.waytofit.competition.domain.enums.CompetitionVisibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
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

    override fun findAllPublic(pageable: Pageable): Page<Competition> {
        return jpaRepository.findAllByVisibility(CompetitionVisibility.PUBLIC, pageable).map { it.toDomain() }
    }

    override fun findAllByIds(ids: List<UUID>): List<Competition> {
        return jpaRepository.findAllByIdIn(ids).map { it.toDomain() }
    }

    override fun findMyCompetitions(
        userId: UUID,
        lifecycles: List<CompetitionLifecycle>?,
        now: Instant,
        pageable: Pageable
    ): Page<Competition> {
        return jpaRepository.findMyCompetitions(userId, lifecycles, now, pageable).map { it.toDomain() }
    }
}
