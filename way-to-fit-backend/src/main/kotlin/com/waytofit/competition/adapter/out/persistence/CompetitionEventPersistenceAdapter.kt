package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionEventEntity
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionEventJpaRepository
import com.waytofit.competition.application.port.out.CompetitionEventRepository
import com.waytofit.competition.domain.CompetitionEvent
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class CompetitionEventPersistenceAdapter(
    private val jpaRepository: CompetitionEventJpaRepository,
) : CompetitionEventRepository {

    override fun save(event: CompetitionEvent): CompetitionEvent {
        return jpaRepository.save(CompetitionEventEntity.fromDomain(event)).toDomain()
    }

    override fun findById(id: UUID): CompetitionEvent? {
        return jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAllByStageId(stageId: UUID): List<CompetitionEvent> {
        return jpaRepository.findAllByStageIdOrderByOrderAsc(stageId).map { it.toDomain() }
    }

    override fun findPublishedByStageId(stageId: UUID): List<CompetitionEvent> {
        return jpaRepository.findPublishedByStageIdOrderByOrderAsc(stageId, Instant.now()).map { it.toDomain() }
    }

    override fun delete(id: UUID) {
        jpaRepository.deleteById(id)
    }
}
