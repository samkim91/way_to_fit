package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionStageEntity
import com.waytofit.competition.adapter.out.persistence.entity.CompetitionStageFinalistEntity
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionStageFinalistJpaRepository
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionStageJpaRepository
import com.waytofit.competition.application.port.out.CompetitionStageRepository
import com.waytofit.competition.domain.CompetitionStage
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class CompetitionStagePersistenceAdapter(
    private val stageJpaRepository: CompetitionStageJpaRepository,
    private val finalistJpaRepository: CompetitionStageFinalistJpaRepository,
) : CompetitionStageRepository {

    override fun save(stage: CompetitionStage): CompetitionStage {
        return stageJpaRepository.save(CompetitionStageEntity.fromDomain(stage)).toDomain()
    }

    override fun findById(id: UUID): CompetitionStage? {
        return stageJpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAllByCompetitionId(competitionId: UUID): List<CompetitionStage> {
        return stageJpaRepository.findAllByCompetitionId(competitionId).map { it.toDomain() }
    }

    @Transactional
    override fun saveFinalists(stageId: UUID, registrationIds: List<UUID>) {
        finalistJpaRepository.deleteAllByStageId(stageId)
        val entities = registrationIds.map {
            CompetitionStageFinalistEntity(stageId = stageId, registrationId = it)
        }
        finalistJpaRepository.saveAll(entities)
    }

    override fun getFinalistRegistrationIds(stageId: UUID): List<UUID> {
        return finalistJpaRepository.findAllByStageId(stageId).map { it.registrationId }
    }
}
