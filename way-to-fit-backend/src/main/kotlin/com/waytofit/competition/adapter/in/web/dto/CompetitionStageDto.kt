package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.domain.CompetitionStage
import com.waytofit.competition.domain.enums.StageFormat
import com.waytofit.competition.domain.enums.StageType
import java.time.Instant
import java.util.UUID

data class CreateStageRequest(
    val name: String,
    val stageType: StageType,
    val stageFormat: StageFormat,
    val startAt: Instant,
    val endAt: Instant,
) {
    fun toCommand(competitionId: UUID) = com.waytofit.competition.application.port.`in`.CreateStageCommand(
        competitionId = competitionId,
        name = name,
        stageType = stageType,
        stageFormat = stageFormat,
        startAt = startAt,
        endAt = endAt
    )
}

data class UpdateStageRequest(
    val name: String?,
    val stageType: StageType?,
    val stageFormat: StageFormat?,
    val startAt: Instant?,
    val endAt: Instant?,
) {
    fun toCommand(id: UUID) = com.waytofit.competition.application.port.`in`.UpdateStageCommand(
        id = id,
        name = name,
        stageType = stageType,
        stageFormat = stageFormat,
        startAt = startAt,
        endAt = endAt
    )
}

data class SelectFinalistsRequest(
    val registrationIds: List<UUID>,
)

data class CompetitionStageResponse(
    val id: UUID,
    val competitionId: UUID,
    val name: String,
    val stageType: StageType,
    val stageFormat: StageFormat,
    val startAt: Instant,
    val endAt: Instant,
) {
    companion object {
        fun fromDomain(stage: CompetitionStage) = CompetitionStageResponse(
            id = stage.id!!,
            competitionId = stage.competitionId,
            name = stage.name,
            stageType = stage.stageType,
            stageFormat = stage.stageFormat,
            startAt = stage.startAt,
            endAt = stage.endAt
        )
    }
}
