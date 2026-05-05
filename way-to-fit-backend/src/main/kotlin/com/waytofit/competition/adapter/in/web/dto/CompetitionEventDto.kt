package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.domain.CompetitionEvent
import com.waytofit.competition.domain.enums.EventType
import com.waytofit.competition.domain.enums.GenderCategory
import com.waytofit.competition.domain.enums.WodType
import com.waytofit.competition.domain.enums.WeightUnit
import java.time.Instant
import java.util.UUID

data class CreateEventRequest(
    val name: String,
    val description: String,
    val eventType: EventType,
    val wodType: WodType,
    val order: Int,
    val scaleCategories: List<String>,
    val submissionDeadline: Instant,
    val gender: GenderCategory,
    val releaseAt: Instant?,
    val timeCap: Int?,
    val amrapDuration: Int?,
    val emomDuration: Int?,
    val weightUnit: WeightUnit?,
) {
    fun toCommand(competitionId: UUID, stageId: UUID) = com.waytofit.competition.application.port.`in`.CreateEventCommand(
        competitionId = competitionId,
        stageId = stageId,
        name = name,
        description = description,
        eventType = eventType,
        wodType = wodType,
        order = order,
        scaleCategories = scaleCategories,
        submissionDeadline = submissionDeadline,
        gender = gender,
        releaseAt = releaseAt,
        timeCap = timeCap,
        amrapDuration = amrapDuration,
        emomDuration = emomDuration,
        weightUnit = weightUnit
    )
}

data class UpdateEventRequest(
    val name: String?,
    val description: String?,
    val eventType: EventType?,
    val wodType: WodType?,
    val order: Int?,
    val scaleCategories: List<String>?,
    val submissionDeadline: Instant?,
    val gender: GenderCategory?,
    val releaseAt: Instant?,
    val timeCap: Int?,
    val amrapDuration: Int?,
    val emomDuration: Int?,
    val weightUnit: WeightUnit?,
) {
    fun toCommand(id: UUID) = com.waytofit.competition.application.port.`in`.UpdateEventCommand(
        id = id,
        name = name,
        description = description,
        eventType = eventType,
        wodType = wodType,
        order = order,
        scaleCategories = scaleCategories,
        submissionDeadline = submissionDeadline,
        gender = gender,
        releaseAt = releaseAt,
        timeCap = timeCap,
        amrapDuration = amrapDuration,
        emomDuration = emomDuration,
        weightUnit = weightUnit
    )
}

data class CompetitionEventResponse(
    val id: UUID,
    val stageId: UUID,
    val competitionId: UUID,
    val name: String,
    val description: String,
    val eventType: EventType,
    val wodType: WodType,
    val order: Int,
    val scaleCategories: List<String>,
    val submissionDeadline: Instant,
    val gender: GenderCategory,
    val releaseAt: Instant?,
    val timeCap: Int?,
    val amrapDuration: Int?,
    val emomDuration: Int?,
    val weightUnit: WeightUnit?,
) {
    companion object {
        fun fromDomain(event: CompetitionEvent) = CompetitionEventResponse(
            id = event.id!!,
            stageId = event.stageId,
            competitionId = event.competitionId,
            name = event.name,
            description = event.description,
            eventType = event.eventType,
            wodType = event.wodType,
            order = event.order,
            scaleCategories = event.scaleCategories,
            submissionDeadline = event.submissionDeadline,
            gender = event.gender,
            releaseAt = event.releaseAt,
            timeCap = event.timeCap,
            amrapDuration = event.amrapDuration,
            emomDuration = event.emomDuration,
            weightUnit = event.weightUnit
        )
    }
}
