package com.waytofit.competition.adapter.`in`.web

import com.waytofit.competition.adapter.`in`.web.dto.CompetitionEventResponse
import com.waytofit.competition.adapter.`in`.web.dto.CreateEventRequest
import com.waytofit.competition.adapter.`in`.web.dto.UpdateEventRequest
import com.waytofit.competition.application.port.`in`.CompetitionEventCommandUseCase
import com.waytofit.competition.application.port.`in`.CompetitionEventQueryUseCase
import com.waytofit.global.common.response.ApiResponse
import com.waytofit.global.security.CurrentUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Competition Event", description = "대회 이벤트(WOD) 관련 API")
@RestController
@RequestMapping("/api/competitions/{competitionId}/stages/{stageId}/events")
class CompetitionEventController(
    private val eventCommandUseCase: CompetitionEventCommandUseCase,
    private val eventQueryUseCase: CompetitionEventQueryUseCase,
) {

    @Operation(summary = "이벤트 생성")
    @PostMapping
    fun createEvent(
        @PathVariable competitionId: UUID,
        @PathVariable stageId: UUID,
        @RequestBody request: CreateEventRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<CompetitionEventResponse> {
        val event = eventCommandUseCase.createEvent(request.toCommand(competitionId, stageId), userId)
        return ApiResponse.success(CompetitionEventResponse.fromDomain(event))
    }

    @Operation(summary = "스테이지별 이벤트 목록 조회")
    @GetMapping
    fun getEvents(
        @PathVariable competitionId: UUID,
        @PathVariable stageId: UUID,
        @CurrentUserId userId: UUID?
    ): ApiResponse<List<CompetitionEventResponse>> {
        val events = if (userId != null && eventQueryUseCase.isOrganizer(competitionId, userId)) {
            eventQueryUseCase.getEventsByStageId(stageId, true)
        } else {
            eventQueryUseCase.getEventsByStageId(stageId, false)
        }
        return ApiResponse.success(events.map { CompetitionEventResponse.fromDomain(it) })
    }

    @Operation(summary = "이벤트 수정")
    @PatchMapping("/{eventId}")
    fun updateEvent(
        @PathVariable competitionId: UUID,
        @PathVariable stageId: UUID,
        @PathVariable eventId: UUID,
        @RequestBody request: UpdateEventRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<CompetitionEventResponse> {
        val event = eventCommandUseCase.updateEvent(request.toCommand(eventId), userId)
        return ApiResponse.success(CompetitionEventResponse.fromDomain(event))
    }

    @Operation(summary = "이벤트 삭제")
    @DeleteMapping("/{eventId}")
    fun deleteEvent(
        @PathVariable competitionId: UUID,
        @PathVariable stageId: UUID,
        @PathVariable eventId: UUID,
        @CurrentUserId userId: UUID
    ): ApiResponse<Unit> {
        eventCommandUseCase.deleteEvent(eventId, userId)
        return ApiResponse.success()
    }
}
