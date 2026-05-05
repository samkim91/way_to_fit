package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.CreateStageCommand
import com.waytofit.competition.application.port.`in`.SelectFinalistsCommand
import com.waytofit.competition.application.port.out.CompetitionOrganizerRepository
import com.waytofit.competition.application.port.out.CompetitionStageRepository
import com.waytofit.competition.domain.CompetitionStage
import com.waytofit.competition.domain.enums.StageFormat
import com.waytofit.competition.domain.enums.StageType
import com.waytofit.global.error.BusinessException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.time.Instant
import java.util.UUID

class CompetitionStageServiceTest {

    private val stageRepository = mock(CompetitionStageRepository::class.java)
    private val organizerRepository = mock(CompetitionOrganizerRepository::class.java)
    private val stageService = CompetitionStageService(stageRepository, organizerRepository)

    @Test
    fun `createStage saves stage if user is organizer`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = CreateStageCommand(
            competitionId = competitionId,
            name = "Qualifier",
            stageType = StageType.QUALIFIER,
            stageFormat = StageFormat.ONLINE,
            startAt = Instant.now(),
            endAt = Instant.now().plusSeconds(3600)
        )
        val stage = CompetitionStage(
            competitionId = competitionId,
            name = command.name,
            stageType = command.stageType,
            stageFormat = command.stageFormat,
            startAt = command.startAt,
            endAt = command.endAt
        )

        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(true)
        `when`(stageRepository.save(anyObject())).thenReturn(stage)

        val result = stageService.createStage(command, userId)

        assertEquals(stage, result)
        verify(stageRepository).save(anyObject())
    }

    @Test
    fun `createStage fails if user is not organizer`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = CreateStageCommand(
            competitionId = competitionId,
            name = "Qualifier",
            stageType = StageType.QUALIFIER,
            stageFormat = StageFormat.ONLINE,
            startAt = Instant.now(),
            endAt = Instant.now().plusSeconds(3600)
        )

        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(false)

        assertThrows(BusinessException::class.java) {
            stageService.createStage(command, userId)
        }
    }

    @Test
    fun `selectFinalists saves finalists if user is organizer`() {
        val stageId = UUID.randomUUID()
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val registrationIds = listOf(UUID.randomUUID(), UUID.randomUUID())
        val command = SelectFinalistsCommand(stageId, registrationIds)
        val stage = CompetitionStage(
            id = stageId,
            competitionId = competitionId,
            name = "Final",
            stageType = StageType.FINAL,
            stageFormat = StageFormat.OFFLINE,
            startAt = Instant.now(),
            endAt = Instant.now()
        )

        `when`(stageRepository.findById(stageId)).thenReturn(stage)
        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(true)

        stageService.selectFinalists(command, userId)

        verify(stageRepository).saveFinalists(stageId, registrationIds)
    }

    private fun <T> anyObject(): T = ArgumentMatchers.any()
}
