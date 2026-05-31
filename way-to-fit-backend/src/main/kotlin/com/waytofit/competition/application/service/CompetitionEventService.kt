package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.*
import com.waytofit.competition.application.port.out.CompetitionEventRepository
import com.waytofit.competition.application.port.out.CompetitionOrganizerRepository
import com.waytofit.competition.application.port.out.CompetitionScoreRepository
import com.waytofit.competition.application.port.out.CompetitionStageRepository
import com.waytofit.competition.domain.CompetitionEvent
import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class CompetitionEventService(
    private val eventRepository: CompetitionEventRepository,
    private val stageRepository: CompetitionStageRepository,
    private val organizerRepository: CompetitionOrganizerRepository,
    private val scoreRepository: CompetitionScoreRepository,
) : CompetitionEventCommandUseCase, CompetitionEventQueryUseCase {

    override fun createEvent(command: CreateEventCommand, userId: UUID): CompetitionEvent {
        if (!organizerRepository.isOrganizer(command.competitionId, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }

        val event = CompetitionEvent(
            stageId = command.stageId,
            competitionId = command.competitionId,
            name = command.name,
            description = command.description,
            rulebook = command.rulebook,
            eventType = command.eventType,
            gender = command.gender,
            wodType = command.wodType,
            timeCap = command.timeCap,
            amrapDuration = command.amrapDuration,
            emomDuration = command.emomDuration,
            weightUnit = command.weightUnit,
            order = command.order,
            scaleCategories = command.scaleCategories.map { it.trim() }.filter { it.isNotEmpty() },
            releaseAt = command.releaseAt,
            submissionDeadline = command.submissionDeadline
        )
        return eventRepository.save(event)
    }

    override fun updateEvent(command: UpdateEventCommand, userId: UUID): CompetitionEvent {
        val event = eventRepository.findById(command.id)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        if (!organizerRepository.isOrganizer(event.competitionId, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }

        if (command.order != null && command.order != event.order) {
            if (scoreRepository.existsByEventId(event.id!!)) {
                throw BusinessException(ResponseCode.WOD_TYPE_CHANGE_BLOCKED, "기록이 존재하는 이벤트의 순서는 변경할 수 없습니다.")
            }
        }

        val updatedEvent = event.copy(
            name = command.name ?: event.name,
            description = command.description ?: event.description,
            rulebook = command.rulebook ?: event.rulebook,
            eventType = command.eventType ?: event.eventType,
            gender = command.gender ?: event.gender,
            wodType = command.wodType ?: event.wodType,
            timeCap = if (command.timeCap != null) command.timeCap else event.timeCap,
            amrapDuration = if (command.amrapDuration != null) command.amrapDuration else event.amrapDuration,
            emomDuration = if (command.emomDuration != null) command.emomDuration else event.emomDuration,
            weightUnit = if (command.weightUnit != null) command.weightUnit else event.weightUnit,
            order = command.order ?: event.order,
            scaleCategories = command.scaleCategories?.map { it.trim() }?.filter { it.isNotEmpty() } ?: event.scaleCategories,
            releaseAt = if (command.releaseAt != null) command.releaseAt else event.releaseAt,
            submissionDeadline = command.submissionDeadline ?: event.submissionDeadline
        )
        return eventRepository.save(updatedEvent)
    }

    override fun deleteEvent(eventId: UUID, userId: UUID) {
        val event = eventRepository.findById(eventId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        if (!organizerRepository.isOrganizer(event.competitionId, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }

        if (scoreRepository.existsByEventId(eventId)) {
            throw BusinessException(ResponseCode.WOD_TYPE_CHANGE_BLOCKED, "기록이 존재하는 이벤트는 삭제할 수 없습니다.")
        }

        eventRepository.delete(eventId)
    }

    @Transactional(readOnly = true)
    override fun getEventsByStageId(stageId: UUID, isOrganizer: Boolean): List<CompetitionEvent> {
        return if (isOrganizer) {
            eventRepository.findAllByStageId(stageId)
        } else {
            eventRepository.findPublishedByStageId(stageId)
        }
    }

    @Transactional(readOnly = true)
    override fun getEvent(eventId: UUID): CompetitionEvent {
        return eventRepository.findById(eventId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)
    }

    @Transactional(readOnly = true)
    override fun isOrganizer(competitionId: UUID, userId: UUID): Boolean {
        return organizerRepository.isOrganizer(competitionId, userId)
    }
}
