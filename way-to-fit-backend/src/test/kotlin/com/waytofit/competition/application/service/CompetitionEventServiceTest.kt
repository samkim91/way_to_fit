package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.CreateEventCommand
import com.waytofit.competition.application.port.`in`.UpdateEventCommand
import com.waytofit.competition.application.port.out.CompetitionEventRepository
import com.waytofit.competition.application.port.out.CompetitionOrganizerRepository
import com.waytofit.competition.application.port.out.CompetitionScoreRepository
import com.waytofit.competition.application.port.out.CompetitionStageRepository
import com.waytofit.competition.domain.CompetitionEvent
import com.waytofit.competition.domain.CompetitionStage
import com.waytofit.competition.domain.enums.*
import com.waytofit.global.error.BusinessException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.Instant
import java.util.UUID

class CompetitionEventServiceTest {

    private val eventRepository = mock(CompetitionEventRepository::class.java)
    private val stageRepository = mock(CompetitionStageRepository::class.java)
    private val organizerRepository = mock(CompetitionOrganizerRepository::class.java)
    private val scoreRepository = mock(CompetitionScoreRepository::class.java)
    private val eventService = CompetitionEventService(
        eventRepository, stageRepository, organizerRepository, scoreRepository
    )

    @Test
    fun `createEvent saves event if user is organizer`() {
        val stageId = UUID.randomUUID()
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = CreateEventCommand(
            stageId = stageId,
            competitionId = competitionId,
            name = "Event 1",
            description = "Desc",
            rulebook = "Rules",
            eventType = EventType.INDIVIDUAL,
            gender = GenderCategory.MEN,
            wodType = WodType.FOR_TIME,
            order = 1,
            scaleCategories = listOf("RXD"),
            submissionDeadline = Instant.now()
        )
        val stage = CompetitionStage(id = stageId, competitionId = competitionId, name = "Stage 1",
            stageType = StageType.QUALIFIER, stageFormat = StageFormat.ONLINE, startAt = Instant.now(), endAt = Instant.now())
        val event = CompetitionEvent(id = UUID.randomUUID(), stageId = stageId, competitionId = competitionId, name = "Event 1", description = "Desc", rulebook = "Rules",
            eventType = EventType.INDIVIDUAL, gender = GenderCategory.MEN, wodType = WodType.FOR_TIME,
            order = 1, scaleCategories = listOf("RXD"), submissionDeadline = command.submissionDeadline)

        `when`(stageRepository.findById(stageId)).thenReturn(stage)
        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(true)
        `when`(eventRepository.save(any(CompetitionEvent::class.java))).thenReturn(event)

        val result = eventService.createEvent(command, userId)

        assertEquals(event, result)
        verify(eventRepository).save(any(CompetitionEvent::class.java))
    }

    @Test
    fun `updateEvent fails if score exists and order is changed`() {
        val eventId = UUID.randomUUID()
        val stageId = UUID.randomUUID()
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val existingEvent = CompetitionEvent(id = eventId, stageId = stageId, competitionId = competitionId, name = "Old", description = "D", rulebook = "Rules",
            eventType = EventType.INDIVIDUAL, gender = GenderCategory.MEN, wodType = WodType.FOR_TIME,
            order = 1, scaleCategories = emptyList(), submissionDeadline = Instant.now())
        val command = UpdateEventCommand(id = eventId, name = null, description = null, rulebook = null,
            eventType = null, gender = null, wodType = null, timeCap = null, amrapDuration = null, emomDuration = null,
            weightUnit = null, order = 2, scaleCategories = null, releaseAt = null, submissionDeadline = null)
        val stage = CompetitionStage(id = stageId, competitionId = competitionId, name = "S", 
            stageType = StageType.QUALIFIER, stageFormat = StageFormat.ONLINE, startAt = Instant.now(), endAt = Instant.now())

        `when`(eventRepository.findById(eventId)).thenReturn(existingEvent)
        `when`(stageRepository.findById(stageId)).thenReturn(stage)
        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(true)
        `when`(scoreRepository.existsByEventId(eventId)).thenReturn(true)

        assertThrows(BusinessException::class.java) {
            eventService.updateEvent(command, userId)
        }
    }

    private fun <T> any(type: Class<T>): T = org.mockito.ArgumentMatchers.any(type)
}
