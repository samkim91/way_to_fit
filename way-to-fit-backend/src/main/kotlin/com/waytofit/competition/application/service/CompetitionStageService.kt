package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.*
import com.waytofit.competition.application.port.out.CompetitionOrganizerRepository
import com.waytofit.competition.application.port.out.CompetitionStageRepository
import com.waytofit.competition.domain.CompetitionStage
import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class CompetitionStageService(
    private val stageRepository: CompetitionStageRepository,
    private val organizerRepository: CompetitionOrganizerRepository,
) : CompetitionStageCommandUseCase, CompetitionStageQueryUseCase {

    override fun createStage(command: CreateStageCommand, userId: UUID): CompetitionStage {
        if (!organizerRepository.isOrganizer(command.competitionId, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }

        val stage = CompetitionStage(
            competitionId = command.competitionId,
            name = command.name,
            stageType = command.stageType,
            stageFormat = command.stageFormat,
            startAt = command.startAt,
            endAt = command.endAt
        )
        return stageRepository.save(stage)
    }

    override fun updateStage(command: UpdateStageCommand, userId: UUID): CompetitionStage {
        val stage = stageRepository.findById(command.id)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        if (!organizerRepository.isOrganizer(stage.competitionId, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }

        val updatedStage = stage.copy(
            name = command.name ?: stage.name,
            stageType = command.stageType ?: stage.stageType,
            stageFormat = command.stageFormat ?: stage.stageFormat,
            startAt = command.startAt ?: stage.startAt,
            endAt = command.endAt ?: stage.endAt
        )
        return stageRepository.save(updatedStage)
    }

    override fun selectFinalists(command: SelectFinalistsCommand, userId: UUID) {
        val stage = stageRepository.findById(command.stageId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        if (!organizerRepository.isOrganizer(stage.competitionId, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }

        stageRepository.saveFinalists(command.stageId, command.registrationIds)
    }

    @Transactional(readOnly = true)
    override fun getStagesByCompetitionId(competitionId: UUID): List<CompetitionStage> {
        return stageRepository.findAllByCompetitionId(competitionId)
    }

    @Transactional(readOnly = true)
    override fun getFinalistRegistrationIds(stageId: UUID): List<UUID> {
        return stageRepository.getFinalistRegistrationIds(stageId)
    }
}
