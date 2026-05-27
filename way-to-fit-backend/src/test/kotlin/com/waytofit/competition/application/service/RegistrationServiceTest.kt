package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.RegisterIndividualCommand
import com.waytofit.competition.application.port.`in`.UpdatePaymentStatusCommand
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.BankInfo
import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.CompetitionRegistration
import com.waytofit.competition.domain.enums.CompetitionVisibility
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.global.error.BusinessException
import com.waytofit.user.domain.enums.Gender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.time.Instant
import java.util.UUID

class RegistrationServiceTest {

    private val registrationRepository = mock(CompetitionRegistrationRepository::class.java)
    private val competitionRepository = mock(CompetitionRepository::class.java)
    private val teamMemberRepository = mock(CompetitionTeamMemberRepository::class.java)
    private val eventLineupRepository = mock(EventLineupRepository::class.java)
    private val organizerRepository = mock(CompetitionOrganizerRepository::class.java)
    private val athleteProfileRepository = mock(AthleteProfileRepository::class.java)
    private val registrationService = RegistrationService(
        registrationRepository, competitionRepository, teamMemberRepository, eventLineupRepository, organizerRepository, athleteProfileRepository
    )

    @Test
    fun `registerIndividual succeeds within registration period`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = RegisterIndividualCommand(competitionId, Gender.MALE, "RXD")
        val now = Instant.now()
        val competition = Competition(
            id = competitionId, name = "Test", description = "Desc",
            startAt = now.plusSeconds(3600), endAt = now.plusSeconds(7200),
            registrationStartAt = now.minusSeconds(3600), registrationEndAt = now.plusSeconds(3600),
            visibility = CompetitionVisibility.PUBLIC, bankInfo = BankInfo.empty()
        )

        `when`(competitionRepository.findById(competitionId)).thenReturn(competition)
        `when`(registrationRepository.existsByCompetitionIdAndUserIdAndType(competitionId, userId, RegistrationType.INDIVIDUAL)).thenReturn(false)
        `when`(registrationRepository.save(anyObject())).thenAnswer { it.arguments[0].let { r -> (r as CompetitionRegistration).copy(id = UUID.randomUUID()) } }

        val result = registrationService.registerIndividual(command, userId)

        assertEquals(competitionId, result.competitionId)
        assertEquals(userId, result.userId)
        assertEquals("RXD", result.scaleCategory)
        verify(teamMemberRepository).saveAll(anyObject())
    }



    private fun <T> anyObject(): T = ArgumentMatchers.any()
}
