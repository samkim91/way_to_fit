package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionStageEntity
import com.waytofit.competition.adapter.out.persistence.entity.CompetitionStageFinalistEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CompetitionStageJpaRepository : JpaRepository<CompetitionStageEntity, UUID> {
    fun findAllByCompetitionId(competitionId: UUID): List<CompetitionStageEntity>
}

interface CompetitionStageFinalistJpaRepository : JpaRepository<CompetitionStageFinalistEntity, UUID> {
    fun findAllByStageId(stageId: UUID): List<CompetitionStageFinalistEntity>
    fun deleteAllByStageId(stageId: UUID)
}
