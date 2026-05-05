package com.waytofit.competition.application.port.`in`

import com.waytofit.competition.domain.CompetitionEvent
import com.waytofit.competition.domain.enums.EventType
import com.waytofit.competition.domain.enums.GenderCategory
import com.waytofit.competition.domain.enums.WeightUnit
import com.waytofit.competition.domain.enums.WodType
import java.time.Instant
import java.util.UUID

interface CompetitionEventCommandUseCase {
    fun createEvent(command: CreateEventCommand, userId: UUID): CompetitionEvent
    fun updateEvent(command: UpdateEventCommand, userId: UUID): CompetitionEvent
    fun deleteEvent(eventId: UUID, userId: UUID)
}

interface CompetitionEventQueryUseCase {
    fun getEventsByStageId(stageId: UUID, isOrganizer: Boolean): List<CompetitionEvent>
    fun getEvent(eventId: UUID): CompetitionEvent
    fun isOrganizer(competitionId: UUID, userId: UUID): Boolean
}

data class CreateEventCommand(
    val stageId: UUID,
    val competitionId: UUID,
    val name: String,
    val description: String,
    val eventType: EventType,
    val gender: GenderCategory,
    val wodType: WodType,
    val timeCap: Int? = null,
    val amrapDuration: Int? = null,
    val emomDuration: Int? = null,
    val weightUnit: WeightUnit? = null,
    val order: Int,
    val scaleCategories: List<String>,
    val releaseAt: Instant? = null,
    val submissionDeadline: Instant,
)

data class UpdateEventCommand(
    val id: UUID,
    val name: String?,
    val description: String?,
    val eventType: EventType?,
    val gender: GenderCategory?,
    val wodType: WodType?,
    val timeCap: Int?,
    val amrapDuration: Int?,
    val emomDuration: Int?,
    val weightUnit: WeightUnit?,
    val order: Int?,
    val scaleCategories: List<String>?,
    val releaseAt: Instant?,
    val submissionDeadline: Instant?,
)
