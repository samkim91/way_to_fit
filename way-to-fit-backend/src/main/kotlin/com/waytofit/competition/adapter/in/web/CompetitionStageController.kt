package com.waytofit.competition.adapter.`in`.web

import com.waytofit.competition.adapter.`in`.web.dto.*
import com.waytofit.competition.application.port.`in`.CompetitionStageCommandUseCase
import com.waytofit.competition.application.port.`in`.CompetitionStageQueryUseCase
import com.waytofit.competition.application.port.`in`.SelectFinalistsCommand
import com.waytofit.global.common.response.ApiResponse
import com.waytofit.global.security.CurrentUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Competition Stage", description = "대회 스테이지 관련 API")
@RestController
@RequestMapping("/api/competitions/{competitionId}/stages")
class CompetitionStageController(
    private val stageCommandUseCase: CompetitionStageCommandUseCase,
    private val stageQueryUseCase: CompetitionStageQueryUseCase,
) {

    @Operation(summary = "스테이지 생성")
    @PostMapping
    fun createStage(
        @PathVariable competitionId: UUID,
        @RequestBody request: CreateStageRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<CompetitionStageResponse> {
        val stage = stageCommandUseCase.createStage(request.toCommand(competitionId), userId)
        return ApiResponse.success(CompetitionStageResponse.fromDomain(stage))
    }

    @Operation(summary = "스테이지 수정")
    @PatchMapping("/{stageId}")
    fun updateStage(
        @PathVariable competitionId: UUID,
        @PathVariable stageId: UUID,
        @RequestBody request: UpdateStageRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<CompetitionStageResponse> {
        val stage = stageCommandUseCase.updateStage(request.toCommand(stageId), userId)
        return ApiResponse.success(CompetitionStageResponse.fromDomain(stage))
    }

    @Operation(summary = "대회별 스테이지 목록 조회")
    @GetMapping
    fun getStages(@PathVariable competitionId: UUID): ApiResponse<List<CompetitionStageResponse>> {
        val stages = stageQueryUseCase.getStagesByCompetitionId(competitionId)
        return ApiResponse.success(stages.map { CompetitionStageResponse.fromDomain(it) })
    }

    @Operation(summary = "본선 진출자 선별 저장")
    @PostMapping("/{stageId}/finalists")
    fun selectFinalists(
        @PathVariable competitionId: UUID,
        @PathVariable stageId: UUID,
        @RequestBody request: SelectFinalistsRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<Unit> {
        stageCommandUseCase.selectFinalists(
            SelectFinalistsCommand(stageId, request.registrationIds),
            userId
        )
        return ApiResponse.success()
    }

    @Operation(summary = "본선 진출자 ID 목록 조회")
    @GetMapping("/{stageId}/finalists")
    fun getFinalists(
        @PathVariable competitionId: UUID,
        @PathVariable stageId: UUID
    ): ApiResponse<List<UUID>> {
        val registrationIds = stageQueryUseCase.getFinalistRegistrationIds(stageId)
        return ApiResponse.success(registrationIds)
    }
}
