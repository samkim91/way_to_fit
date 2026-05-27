package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.ReviewScoreCommand
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.CompetitionEvent
import com.waytofit.competition.domain.CompetitionScore
import com.waytofit.competition.domain.CompetitionStage
import com.waytofit.competition.domain.enums.*
import com.waytofit.competition.domain.event.ScoreReviewedEvent
import com.waytofit.global.error.BusinessException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.util.UUID

class ScoreReviewTest {

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
    fun `reviewScore succeeds and publishes event if user is organizer`() {
        val scoreId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val stageId = UUID.randomUUID()
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = ReviewScoreCommand(scoreId, ScoreStatus.APPROVED, null, null, null, null, null, "Good job")

        val score = CompetitionScore(id = scoreId, eventId = eventId, registrationId = UUID.randomUUID(), resultStatus = ResultStatus.COMPLETED, 
            videoUrl = "url", status = ScoreStatus.SUBMITTED)
        val event = CompetitionEvent(id = eventId, stageId = stageId, competitionId = competitionId, name = "E", description = "D", eventType = EventType.INDIVIDUAL, 
            gender = GenderCategory.MEN, wodType = WodType.FOR_TIME, order = 1, scaleCategories = emptyList(), 
            submissionDeadline = Instant.now())
        val stage = CompetitionStage(id = stageId, competitionId = competitionId, name = "S", 
            stageType = StageType.QUALIFIER, stageFormat = StageFormat.ONLINE, startAt = Instant.now(), endAt = Instant.now())

        `when`(scoreRepository.findById(scoreId)).thenReturn(score)
        `when`(eventRepository.findById(eventId)).thenReturn(event)
        `when`(stageRepository.findById(stageId)).thenReturn(stage)
        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(true)
        `when`(scoreRepository.save(anyObject())).thenAnswer { it.arguments[0] }

        val result = scoreService.reviewScore(command, userId)

        assertEquals(ScoreStatus.APPROVED, result.status)
        assertEquals("Good job", result.reviewerNote)
        verify(eventPublisher).publishEvent(any(ScoreReviewedEvent::class.java))
    }

    @Test
    fun `reviewScore fails if user is not organizer`() {
        val scoreId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val stageId = UUID.randomUUID()
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = ReviewScoreCommand(scoreId, ScoreStatus.APPROVED, null, null, null, null, null, "Good job")

        val score = CompetitionScore(id = scoreId, eventId = eventId, registrationId = UUID.randomUUID(), resultStatus = ResultStatus.COMPLETED, 
            videoUrl = "url", status = ScoreStatus.SUBMITTED)
        val event = CompetitionEvent(id = eventId, stageId = stageId, competitionId = competitionId, name = "E", description = "D", eventType = EventType.INDIVIDUAL, 
            gender = GenderCategory.MEN, wodType = WodType.FOR_TIME, order = 1, scaleCategories = emptyList(), 
            submissionDeadline = Instant.now())
        val stage = CompetitionStage(id = stageId, competitionId = competitionId, name = "S", 
            stageType = StageType.QUALIFIER, stageFormat = StageFormat.ONLINE, startAt = Instant.now(), endAt = Instant.now())

        `when`(scoreRepository.findById(scoreId)).thenReturn(score)
        `when`(eventRepository.findById(eventId)).thenReturn(event)
        `when`(stageRepository.findById(stageId)).thenReturn(stage)
        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(false)

        assertThrows(BusinessException::class.java) {
            scoreService.reviewScore(command, userId)
        }
    }

    private fun <T> any(type: Class<T>): T = ArgumentMatchers.any(type)
    private fun <T> anyObject(): T = ArgumentMatchers.any()
}
