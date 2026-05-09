package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.CreateCompetitionCommand
import com.waytofit.competition.application.port.`in`.UpdateCompetitionCommand
import com.waytofit.competition.application.port.out.CompetitionOrganizerRepository
import com.waytofit.competition.application.port.out.CompetitionRepository
import com.waytofit.competition.domain.BankInfo
import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.enums.CompetitionStatus
import com.waytofit.global.error.BusinessException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.util.UUID

class CompetitionServiceTest {

    private val competitionRepository = mock(CompetitionRepository::class.java)
    private val competitionOrganizerRepository = mock(CompetitionOrganizerRepository::class.java)
    private val eventPublisher = mock(ApplicationEventPublisher::class.java)
    private val competitionService = CompetitionService(competitionRepository, competitionOrganizerRepository, eventPublisher)

    @Test
    fun `createCompetition saves competition and registers organizer`() {
        val userId = UUID.randomUUID()
        val competitionId = UUID.randomUUID()
        val command = CreateCompetitionCommand(
            name = "Test Comp",
            description = "Desc",
            bannerImageUrl = "banner",
            startAt = Instant.now().plusSeconds(3600),
            endAt = Instant.now().plusSeconds(7200),
            registrationStartAt = Instant.now(),
            registrationEndAt = Instant.now().plusSeconds(1800),
            bankName = "Bank",
            accountNumber = "123",
            accountHolder = "Owner",
            entryFee = 10000
        )
        val savedCompetition = Competition(
            id = competitionId,
            name = command.name,
            description = command.description,
            bannerImageUrl = command.bannerImageUrl,
            startAt = command.startAt,
            endAt = command.endAt,
            registrationStartAt = command.registrationStartAt,
            registrationEndAt = command.registrationEndAt,
            status = CompetitionStatus.DRAFT,
            bankInfo = BankInfo(command.bankName, command.accountNumber, command.accountHolder, command.entryFee)
        )

        `when`(competitionRepository.save(anyObject())).thenReturn(savedCompetition)

        val result = competitionService.createCompetition(command, userId)

        assertEquals(savedCompetition, result)
        verify(competitionRepository).save(anyObject())
        verify(competitionOrganizerRepository).saveOrganizer(competitionId, userId)
    }

    @Test
    fun `updateCompetition fails if user is not organizer`() {
        val userId = UUID.randomUUID()
        val competitionId = UUID.randomUUID()
        val command = UpdateCompetitionCommand(
            id = competitionId,
            name = "Updated",
            description = null, bannerImageUrl = null, startAt = null, endAt = null,
            registrationStartAt = null, registrationEndAt = null,
            status = null, bankName = null, accountNumber = null, accountHolder = null, entryFee = null
        )
        val existingCompetition = Competition(
            id = competitionId,
            name = "Old",
            description = "Desc",
            startAt = Instant.now().plusSeconds(3600),
            endAt = Instant.now().plusSeconds(7200),
            registrationStartAt = Instant.now(),
            registrationEndAt = Instant.now().plusSeconds(1800),
            status = CompetitionStatus.DRAFT,
            bankInfo = BankInfo("Bank", "123", "Owner", 10000)
        )

        `when`(competitionRepository.findById(competitionId)).thenReturn(existingCompetition)
        `when`(competitionOrganizerRepository.isOrganizer(competitionId, userId)).thenReturn(false)

        assertThrows(BusinessException::class.java) {
            competitionService.updateCompetition(command, userId)
        }
    }

    private fun <T> anyObject(): T = ArgumentMatchers.any()
}
