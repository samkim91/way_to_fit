package com.waytofit.competition.adapter.`in`.web

import com.waytofit.competition.adapter.`in`.web.dto.EventLineupResponse
import com.waytofit.competition.adapter.`in`.web.dto.SetLineupRequest
import com.waytofit.competition.application.port.`in`.GetEventLineupUseCase
import com.waytofit.competition.application.port.`in`.SetEventLineupUseCase
import com.waytofit.global.common.response.ApiResponse
import com.waytofit.global.security.CurrentUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Event Lineup", description = "팀 이벤트별 출전 멤버(라인업) 관련 API")
@RestController
@RequestMapping("/api/competitions/{competitionId}/events/{eventId}/lineups/{registrationId}")
class EventLineupController(
    private val setEventLineupUseCase: SetEventLineupUseCase,
    private val getEventLineupUseCase: GetEventLineupUseCase,
) {

    @Operation(summary = "이벤트별 출전 멤버 설정 (Upsert)")
    @PutMapping
    fun setEventLineup(
        @PathVariable competitionId: UUID,
        @PathVariable eventId: UUID,
        @PathVariable registrationId: UUID,
        @RequestBody request: SetLineupRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<EventLineupResponse> {
        val lineup = setEventLineupUseCase.setEventLineup(request.toCommand(eventId, registrationId), userId)
        return ApiResponse.success(EventLineupResponse.fromDomain(lineup))
    }

    @Operation(summary = "이벤트별 출전 멤버 조회")
    @GetMapping
    fun getEventLineup(
        @PathVariable competitionId: UUID,
        @PathVariable eventId: UUID,
        @PathVariable registrationId: UUID
    ): ApiResponse<EventLineupResponse?> {
        val lineup = getEventLineupUseCase.getEventLineup(eventId, registrationId)
        return ApiResponse.success(lineup?.let { EventLineupResponse.fromDomain(it) })
    }
}
