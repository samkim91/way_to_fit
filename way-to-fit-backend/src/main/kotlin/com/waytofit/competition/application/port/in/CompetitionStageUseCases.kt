package com.waytofit.competition.application.port.`in`

import com.waytofit.competition.domain.CompetitionStage
import com.waytofit.competition.domain.enums.StageFormat
import com.waytofit.competition.domain.enums.StageType
import java.time.Instant
import java.util.UUID

interface CompetitionStageCommandUseCase {
    fun createStage(command: CreateStageCommand, userId: UUID): CompetitionStage
    fun updateStage(command: UpdateStageCommand, userId: UUID): CompetitionStage
    fun selectFinalists(command: SelectFinalistsCommand, userId: UUID)
}

interface CompetitionStageQueryUseCase {
    fun getStagesByCompetitionId(competitionId: UUID): List<CompetitionStage>
    fun getFinalistRegistrationIds(stageId: UUID): List<UUID>
}

data class CreateStageCommand(
    val competitionId: UUID,
    val name: String,
    val stageType: StageType,
    val stageFormat: StageFormat,
    val startAt: Instant,
    val endAt: Instant,
)

data class UpdateStageCommand(
    val id: UUID,
    val name: String?,
    val stageType: StageType?,
    val stageFormat: StageFormat?,
    val startAt: Instant?,
    val endAt: Instant?,
)

data class SelectFinalistsCommand(
    val stageId: UUID,
    val registrationIds: List<UUID>,
)
