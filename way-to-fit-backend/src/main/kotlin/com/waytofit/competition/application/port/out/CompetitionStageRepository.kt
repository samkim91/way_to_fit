package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.CompetitionStage
import java.util.UUID

interface CompetitionStageRepository {
    fun save(stage: CompetitionStage): CompetitionStage
    fun findById(id: UUID): CompetitionStage?
    fun findAllByCompetitionId(competitionId: UUID): List<CompetitionStage>
    fun saveFinalists(stageId: UUID, registrationIds: List<UUID>)
    fun getFinalistRegistrationIds(stageId: UUID): List<UUID>
}
