package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.ReviewScoreCommand
import com.waytofit.competition.application.port.`in`.SubmitScoreCommand
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.CompetitionEvent
import com.waytofit.competition.domain.CompetitionRegistration
import com.waytofit.competition.domain.CompetitionScore
import com.waytofit.competition.domain.CompetitionStage
import com.waytofit.competition.domain.CompetitionTeamMember
import com.waytofit.competition.domain.enums.*
import com.waytofit.competition.domain.event.ScoreReviewedEvent
import com.waytofit.global.error.BusinessException
import com.waytofit.user.domain.enums.Gender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.util.UUID

class CompetitionScoreServiceTest {

    private val scoreRepository = mock(CompetitionScoreRepository::class.java)
    private val eventRepository = mock(CompetitionEventRepository::class.java)
    private val stageRepository = mock(CompetitionStageRepository::class.java)
    private val registrationRepository = mock(CompetitionRegistrationRepository::class.java)
    private val teamMemberRepository = mock(CompetitionTeamMemberRepository::class.java)
    private val organizerRepository = mock(CompetitionOrganizerRepository::class.java)
    private val eventPublisher = mock(ApplicationEventPublisher::class.java)
    private val scoreService = ScoreService(
        scoreRepository, eventRepository, stageRepository, registrationRepository, teamMemberRepository, organizerRepository, eventPublisher
    )

    @Test
    fun `submitScore succeeds for valid request`() {
        val competitionId = UUID.randomUUID()
        val stageId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val regId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = SubmitScoreCommand(eventId, regId, ResultStatus.COMPLETED, 300, null, null, null, null, "https://vimeo.com/123456")
        
        val event = CompetitionEvent(id = eventId, stageId = stageId, competitionId = competitionId, name = "E", description = "D", eventType = EventType.INDIVIDUAL, 
            gender = GenderCategory.MEN, wodType = WodType.FOR_TIME, order = 1, scaleCategories = emptyList(), 
            submissionDeadline = Instant.now().plusSeconds(3600))
        val registration = CompetitionRegistration(id = regId, competitionId = competitionId, userId = userId, registrationType = RegistrationType.INDIVIDUAL, 
            gender = Gender.MALE, scaleCategory = "RXD", paymentStatus = PaymentStatus.CONFIRMED)
        val members = listOf(CompetitionTeamMember(id = UUID.randomUUID(), registrationId = regId, userId = userId, gender = Gender.MALE, teamRole = TeamRole.LEADER))

        `when`(eventRepository.findById(eventId)).thenReturn(event)
        `when`(registrationRepository.findById(regId)).thenReturn(registration)
        `when`(teamMemberRepository.findByRegistrationId(regId)).thenReturn(members)
        `when`(scoreRepository.findByEventIdAndRegistrationId(eventId, regId)).thenReturn(null)
        `when`(scoreRepository.save(anyObject())).thenAnswer { it.arguments[0] }

        val result = scoreService.submitScore(command, userId)

        assertEquals(eventId, result.eventId)
        assertEquals(regId, result.registrationId)
        assertEquals("https://vimeo.com/123456", result.videoUrl)
    }

    @Test
    fun `submitScore rejects invalid video url`() {
        val competitionId = UUID.randomUUID()
        val stageId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val regId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = SubmitScoreCommand(eventId, regId, ResultStatus.COMPLETED, 300, null, null, null, null, "not-a-url")

        val event = CompetitionEvent(id = eventId, stageId = stageId, competitionId = competitionId, name = "E", description = "D", eventType = EventType.INDIVIDUAL,
            gender = GenderCategory.MEN, wodType = WodType.FOR_TIME, order = 1, scaleCategories = emptyList(),
            submissionDeadline = Instant.now().plusSeconds(3600))
        val registration = CompetitionRegistration(id = regId, competitionId = competitionId, userId = userId, registrationType = RegistrationType.INDIVIDUAL,
            gender = Gender.MALE, scaleCategory = "RXD", paymentStatus = PaymentStatus.CONFIRMED)
        val members = listOf(CompetitionTeamMember(id = UUID.randomUUID(), registrationId = regId, userId = userId, gender = Gender.MALE, teamRole = TeamRole.LEADER))

        `when`(eventRepository.findById(eventId)).thenReturn(event)
        `when`(registrationRepository.findById(regId)).thenReturn(registration)
        `when`(teamMemberRepository.findByRegistrationId(regId)).thenReturn(members)

        assertThrows(BusinessException::class.java) {
            scoreService.submitScore(command, userId)
        }
    }



    private fun <T> any(type: Class<T>): T = ArgumentMatchers.any(type)
    private fun <T> anyObject(): T = ArgumentMatchers.any()
}
