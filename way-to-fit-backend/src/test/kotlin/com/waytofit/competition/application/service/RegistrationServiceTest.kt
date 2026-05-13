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

    @Test
    fun `updatePaymentStatus succeeds if user is organizer`() {
        val regId = UUID.randomUUID()
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = UpdatePaymentStatusCommand(regId, PaymentStatus.CONFIRMED)
        val registration = CompetitionRegistration(
            id = regId, competitionId = competitionId, userId = UUID.randomUUID(),
            registrationType = RegistrationType.INDIVIDUAL, gender = Gender.MALE, scaleCategory = "RXD", paymentStatus = PaymentStatus.PENDING
        )

        `when`(registrationRepository.findById(regId)).thenReturn(registration)
        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(true)
        `when`(registrationRepository.save(anyObject())).thenAnswer { it.arguments[0] }

        val result = registrationService.updatePaymentStatus(command, userId)

        assertEquals(PaymentStatus.CONFIRMED, result.paymentStatus)
        verify(registrationRepository).save(anyObject())
    }

    @Test
    fun `updatePaymentStatus fails if user is not organizer`() {
        val regId = UUID.randomUUID()
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = UpdatePaymentStatusCommand(regId, PaymentStatus.CONFIRMED)
        val registration = CompetitionRegistration(
            id = regId, competitionId = competitionId, userId = UUID.randomUUID(),
            registrationType = RegistrationType.INDIVIDUAL, gender = Gender.MALE, scaleCategory = "RXD", paymentStatus = PaymentStatus.PENDING
        )

        `when`(registrationRepository.findById(regId)).thenReturn(registration)
        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(false)

        assertThrows(BusinessException::class.java) {
            registrationService.updatePaymentStatus(command, userId)
        }
    }

    private fun <T> anyObject(): T = ArgumentMatchers.any()
}
