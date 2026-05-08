package com.waytofit.competition.adapter.`in`.web

import com.waytofit.competition.adapter.`in`.web.dto.CompetitionResponse
import com.waytofit.competition.adapter.`in`.web.dto.CreateCompetitionRequest
import com.waytofit.competition.adapter.`in`.web.dto.UpdateCompetitionRequest
import com.waytofit.competition.application.port.`in`.CompetitionCommandUseCase
import com.waytofit.competition.application.port.`in`.CompetitionQueryUseCase
import com.waytofit.competition.domain.enums.CompetitionStatus
import com.waytofit.global.common.response.ApiResponse
import com.waytofit.global.security.CurrentUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Competition", description = "대회 관련 API")
@RestController
@RequestMapping("/api/competitions")
class CompetitionController(
    private val competitionCommandUseCase: CompetitionCommandUseCase,
    private val competitionQueryUseCase: CompetitionQueryUseCase,
) {

    @Operation(summary = "대회 생성 (DRAFT)")
    @PostMapping
    fun createCompetition(
        @RequestBody request: CreateCompetitionRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<CompetitionResponse> {
        val competition = competitionCommandUseCase.createCompetition(request.toCommand(), userId)
        return ApiResponse.success(CompetitionResponse.fromDomain(competition))
    }

    @Operation(summary = "대회 수정")
    @PatchMapping("/{id}")
    fun updateCompetition(
        @PathVariable id: UUID,
        @RequestBody request: UpdateCompetitionRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<CompetitionResponse> {
        val competition = competitionCommandUseCase.updateCompetition(request.toCommand(id), userId)
        return ApiResponse.success(CompetitionResponse.fromDomain(competition))
    }

    @Operation(summary = "내 대회 목록 조회 (DRAFT 포함)")
    @GetMapping("/my")
    fun getMyCompetitions(
        @CurrentUserId userId: UUID,
        @RequestParam(required = false) statuses: List<CompetitionStatus>?,
        pageable: Pageable
    ): ApiResponse<Page<CompetitionResponse>> {
        val competitions = competitionQueryUseCase.getMyCompetitions(userId, statuses, pageable)
        return ApiResponse.success(competitions.map { CompetitionResponse.fromDomain(it) })
    }

    @Operation(summary = "대회 상세 조회")
    @GetMapping("/{id}")
    fun getCompetition(@PathVariable id: UUID): ApiResponse<CompetitionResponse> {
        val competition = competitionQueryUseCase.getCompetition(id)
        return ApiResponse.success(CompetitionResponse.fromDomain(competition))
    }

    @Operation(summary = "대회 목록 조회 (DRAFT 제외)")
    @GetMapping
    fun getCompetitions(pageable: Pageable): ApiResponse<Page<CompetitionResponse>> {
        val competitions = competitionQueryUseCase.getCompetitions(pageable)
        return ApiResponse.success(competitions.map { CompetitionResponse.fromDomain(it) })
    }
}
