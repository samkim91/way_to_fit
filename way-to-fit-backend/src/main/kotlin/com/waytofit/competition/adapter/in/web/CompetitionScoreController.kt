package com.waytofit.competition.adapter.`in`.web

import com.waytofit.competition.adapter.`in`.web.dto.ReviewScoreRequest
import com.waytofit.competition.adapter.`in`.web.dto.ScoreResponse
import com.waytofit.competition.adapter.`in`.web.dto.SubmitScoreRequest
import com.waytofit.competition.application.port.`in`.*
import com.waytofit.competition.domain.enums.ScoreStatus
import com.waytofit.global.common.response.ApiResponse
import com.waytofit.global.security.CurrentUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Competition Score", description = "대회 기록 제출 및 판독 관련 API")
@RestController
@RequestMapping("/api/competitions/{competitionId}/events/{eventId}/scores")
class CompetitionScoreController(
    private val submitScoreUseCase: SubmitScoreUseCase,
    private val getScoreUseCase: GetScoreUseCase,
    private val organizerScoreQueryUseCase: OrganizerScoreQueryUseCase,
    private val reviewScoreUseCase: ReviewScoreUseCase,
) {

    @Operation(summary = "기록 제출 (또는 수정)")
    @PostMapping
    fun submitScore(
        @PathVariable competitionId: UUID,
        @PathVariable eventId: UUID,
        @RequestBody request: SubmitScoreRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<ScoreResponse> {
        val score = submitScoreUseCase.submitScore(request.toCommand(eventId), userId)
        return ApiResponse.success(ScoreResponse.fromDomain(score))
    }

    @Operation(summary = "기록 상세 조회")
    @GetMapping("/{registrationId}")
    fun getScore(
        @PathVariable competitionId: UUID,
        @PathVariable eventId: UUID,
        @PathVariable registrationId: UUID
    ): ApiResponse<ScoreResponse?> {
        val score = getScoreUseCase.getScore(eventId, registrationId)
        return ApiResponse.success(score?.let { ScoreResponse.fromDomain(it) })
    }

    @Operation(summary = "[주최자] 이벤트별 기록 목록 조회")
    @GetMapping("/all")
    fun getScoresByEvent(
        @PathVariable competitionId: UUID,
        @PathVariable eventId: UUID,
        @RequestParam(required = false) status: ScoreStatus?,
        @CurrentUserId userId: UUID
    ): ApiResponse<List<ScoreResponse>> {
        val scores = organizerScoreQueryUseCase.getScoresByEventId(eventId, status, userId)
        return ApiResponse.success(scores.map { ScoreResponse.fromDomain(it) })
    }

    @Operation(summary = "[주최자] 기록 판독 (상태 변경 및 수정)")
    @PatchMapping("/{scoreId}/review")
    fun reviewScore(
        @PathVariable competitionId: UUID,
        @PathVariable eventId: UUID,
        @PathVariable scoreId: UUID,
        @RequestBody request: ReviewScoreRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<ScoreResponse> {
        val score = reviewScoreUseCase.reviewScore(request.toCommand(scoreId), userId)
        return ApiResponse.success(ScoreResponse.fromDomain(score))
    }
}
