package com.waytofit.competition.adapter.`in`.web

import com.waytofit.competition.adapter.`in`.web.dto.*
import com.waytofit.competition.application.port.`in`.*
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.global.common.response.ApiResponse
import com.waytofit.global.security.CurrentUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "Competition Registration", description = "대회 참가 신청 관련 API")
@RestController
@RequestMapping("/api/competitions/{competitionId}/registrations")
class CompetitionRegistrationController(
    private val registerIndividualUseCase: RegisterIndividualUseCase,
    private val registerTeamUseCase: RegisterTeamUseCase,
    private val getRegistrationUseCase: GetRegistrationUseCase,
    private val organizerRegistrationQueryUseCase: OrganizerRegistrationQueryUseCase,
    private val organizerRegistrationCommandUseCase: OrganizerRegistrationCommandUseCase,
    private val searchTeamMemberUseCase: SearchTeamMemberUseCase,
) {

    @Operation(summary = "개인 참가 신청")
    @PostMapping
    fun registerIndividual(
        @PathVariable competitionId: UUID,
        @RequestBody request: RegisterIndividualRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<RegistrationResponse> {
        val registration = registerIndividualUseCase.registerIndividual(request.toCommand(competitionId), userId)
        return ApiResponse.success(RegistrationResponse.fromDomain(registration))
    }

    @Operation(summary = "팀 참가 신청")
    @PostMapping("/team")
    fun registerTeam(
        @PathVariable competitionId: UUID,
        @RequestBody request: RegisterTeamRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<RegistrationResponse> {
        val registration = registerTeamUseCase.registerTeam(request.toCommand(competitionId), userId)
        return ApiResponse.success(RegistrationResponse.fromDomain(registration))
    }

    @Operation(summary = "내 참가 신청 내역 조회")
    @GetMapping("/me")
    fun getMyRegistration(
        @PathVariable competitionId: UUID,
        @CurrentUserId userId: UUID
    ): ApiResponse<List<RegistrationResponse>> {
        val registrations = getRegistrationUseCase.getMyRegistrations(competitionId, userId)
        return ApiResponse.success(registrations.map { RegistrationResponse.fromDomain(it) })
    }

    @Operation(summary = "팀원 검색 (대회 미참가자 대상)")
    @GetMapping("/search-athletes")
    fun searchPotentialMembers(
        @PathVariable competitionId: UUID,
        @RequestParam name: String,
        @CurrentUserId userId: UUID
    ): ApiResponse<List<AthleteSearchResponse>> {
        val results = searchTeamMemberUseCase.searchPotentialMembers(competitionId, name)
        return ApiResponse.success(results.map { result ->
            AthleteSearchResponse(
                userId = result.userId,
                name = result.name,
                gender = result.gender,
                profileImageUrl = result.profileImageUrl
            )
        })
    }

    @Operation(summary = "[주최자] 신청 목록 조회 (결제 상태 필터링)")
    @GetMapping
    fun getRegistrations(
        @PathVariable competitionId: UUID,
        @RequestParam(required = false) paymentStatus: PaymentStatus?,
        pageable: Pageable,
        @CurrentUserId userId: UUID
    ): ApiResponse<Page<RegistrationResponse>> {
        val registrations = organizerRegistrationQueryUseCase.getRegistrations(competitionId, paymentStatus, pageable, userId)
        return ApiResponse.success(registrations.map { RegistrationResponse.fromDomain(it) })
    }

    @Operation(summary = "[주최자] 결제 상태 변경 (승인/거절)")
    @PatchMapping("/{registrationId}/payment")
    fun updatePaymentStatus(
        @PathVariable competitionId: UUID,
        @PathVariable registrationId: UUID,
        @RequestBody request: UpdatePaymentStatusRequest,
        @CurrentUserId userId: UUID
    ): ApiResponse<RegistrationResponse> {
        val registration = organizerRegistrationCommandUseCase.updatePaymentStatus(request.toCommand(registrationId), userId)
        return ApiResponse.success(RegistrationResponse.fromDomain(registration))
    }
}
