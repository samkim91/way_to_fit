package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.UpdatePaymentStatusCommand
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.CompetitionRegistration
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.global.error.BusinessException
import com.waytofit.user.domain.enums.Gender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.UUID

class RegistrationOrganizerTest {

    private val registrationRepository = mock(CompetitionRegistrationRepository::class.java)
    private val competitionRepository = mock(CompetitionRepository::class.java)
    private val teamMemberRepository = mock(CompetitionTeamMemberRepository::class.java)
    private val eventLineupRepository = mock(EventLineupRepository::class.java)
    private val organizerRepository = mock(CompetitionOrganizerRepository::class.java)
    private val athleteProfileRepository = mock(AthleteProfileRepository::class.java)
    private val userQueryPort = mock(UserQueryPort::class.java)
    private val registrationService = RegistrationService(
        registrationRepository, competitionRepository, teamMemberRepository, eventLineupRepository, organizerRepository, athleteProfileRepository, userQueryPort
    )

    @Test
    fun `getRegistrations succeeds for organizer`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val pageable = PageRequest.of(0, 10)
        val registrations = listOf(
            CompetitionRegistration(
                id = UUID.randomUUID(), competitionId = competitionId, userId = UUID.randomUUID(),
                registrationType = RegistrationType.INDIVIDUAL, gender = Gender.MALE, scaleCategory = "RXD", paymentStatus = PaymentStatus.PENDING
            )
        )
        val page = PageImpl(registrations)

        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(true)
        `when`(registrationRepository.findRegistrationsByCompetitionId(competitionId, PaymentStatus.PENDING, pageable)).thenReturn(page)

        val result = registrationService.getRegistrations(competitionId, PaymentStatus.PENDING, pageable, userId)

        assertEquals(1, result.content.size)
        assertEquals(registrations.first().id, result.content.first().registration.id)
    }

    @Test
    fun `getRegistrations fails for non-organizer`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val pageable = PageRequest.of(0, 10)

        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(false)

        assertThrows(BusinessException::class.java) {
            registrationService.getRegistrations(competitionId, PaymentStatus.PENDING, pageable, userId)
        }
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
