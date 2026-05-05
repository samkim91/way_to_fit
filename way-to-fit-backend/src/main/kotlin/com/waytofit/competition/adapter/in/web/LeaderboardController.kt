package com.waytofit.competition.adapter.`in`.web

import com.waytofit.competition.adapter.`in`.web.dto.EventLeaderboardResponse
import com.waytofit.competition.adapter.`in`.web.dto.OverallLeaderboardResponse
import com.waytofit.competition.adapter.`in`.web.dto.OverrideRankRequest
import com.waytofit.competition.application.port.`in`.*
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.global.common.response.ApiResponse
import com.waytofit.global.security.CurrentUserId
import com.waytofit.user.domain.enums.Gender
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Leaderboard", description = "대회 리더보드 관련 API")
@RestController
@RequestMapping("/api/competitions/{competitionId}")
class LeaderboardController(
    private val getEventLeaderboardUseCase: GetEventLeaderboardUseCase,
    private val getOverallLeaderboardUseCase: GetOverallLeaderboardUseCase,
    private val overrideRankUseCase: OverrideRankUseCase,
) {

    @Operation(summary = "이벤트별 리더보드 조회")
    @GetMapping("/events/{eventId}/leaderboard")
    fun getEventLeaderboard(
        @PathVariable competitionId: UUID,
        @PathVariable eventId: UUID,
        @RequestParam(required = false) gender: Gender?,
        @RequestParam(required = false) scaleCategory: String?
    ): ApiResponse<EventLeaderboardResponse> {
        val result = getEventLeaderboardUseCase.getEventLeaderboard(
            GetEventLeaderboardQuery(eventId, gender, scaleCategory)
        )
        return ApiResponse.success(EventLeaderboardResponse.fromResult(result))
    }

    @Operation(summary = "스테이지별 종합 리더보드 조회")
    @GetMapping("/stages/{stageId}/leaderboard")
    fun getOverallLeaderboard(
        @PathVariable competitionId: UUID,
        @PathVariable stageId: UUID,
        @RequestParam(required = false) registrationType: RegistrationType?,
        @RequestParam(required = false) gender: Gender?,
        @RequestParam(required = false) scaleCategory: String?
    ): ApiResponse<OverallLeaderboardResponse> {
        val result = getOverallLeaderboardUseCase.getOverallLeaderboard(
            GetOverallLeaderboardQuery(stageId, registrationType, gender, scaleCategory)
        )
        return ApiResponse.success(OverallLeaderboardResponse.fromResult(result))
    }

    @Operation(summary = "[주최자] 순위 수동 조정 (Override)")
    @PatchMapping("/leaderboard/{registrationId}/rank")
    fun overrideRank(
        @PathVariable competitionId: UUID,
        @PathVariable registrationId: UUID,
        @RequestBody request: OverrideRankRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<Unit> {
        overrideRankUseCase.overrideRank(request.toCommand(registrationId), userId)
        return ApiResponse.success()
    }
}
