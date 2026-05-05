package com.waytofit.competition.adapter.`in`.web

import com.waytofit.competition.adapter.`in`.web.dto.AthleteCompetitionHistoryResponse
import com.waytofit.competition.adapter.`in`.web.dto.AthleteProfileResponse
import com.waytofit.competition.adapter.`in`.web.dto.UpdateAthleteProfileRequest
import com.waytofit.competition.application.port.`in`.GetAthleteCompetitionHistoryUseCase
import com.waytofit.competition.application.port.`in`.GetAthleteProfileUseCase
import com.waytofit.competition.application.port.`in`.UpdateAthleteProfileUseCase
import com.waytofit.global.common.response.ApiResponse
import com.waytofit.global.security.CurrentUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Athlete Profile", description = "선수 프로필 관련 API")
@RestController
@RequestMapping("/api/athletes")
class AthleteProfileController(
    private val getAthleteProfileUseCase: GetAthleteProfileUseCase,
    private val updateAthleteProfileUseCase: UpdateAthleteProfileUseCase,
    private val getAthleteCompetitionHistoryUseCase: GetAthleteCompetitionHistoryUseCase,
) {

    @Operation(summary = "내 프로필 수정")
    @PatchMapping("/me")
    fun updateMyProfile(
        @CurrentUserId userId: UUID,
        @RequestBody request: UpdateAthleteProfileRequest
    ): ApiResponse<AthleteProfileResponse> {
        val result = updateAthleteProfileUseCase.updateAthleteProfile(userId, request.toCommand())
        return ApiResponse.success(AthleteProfileResponse.fromDomain(result))
    }

    @Operation(summary = "선수 프로필 조회")
    @GetMapping("/{userId}")
    fun getAthleteProfile(
        @PathVariable userId: UUID
    ): ApiResponse<AthleteProfileResponse> {
        val result = getAthleteProfileUseCase.getAthleteProfile(userId)
        return ApiResponse.success(AthleteProfileResponse.fromDomain(result))
    }

    @Operation(summary = "선수 대회 참가 이력 조회")
    @GetMapping("/{userId}/competitions")
    fun getAthleteCompetitionHistory(
        @PathVariable userId: UUID
    ): ApiResponse<AthleteCompetitionHistoryResponse> {
        val result = getAthleteCompetitionHistoryUseCase.getAthleteCompetitionHistory(userId)
        return ApiResponse.success(result)
    }
}
