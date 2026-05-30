package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.domain.CompetitionRegistration
import com.waytofit.competition.domain.CompetitionTeamMember
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.user.domain.enums.Gender
import java.time.Instant
import java.util.UUID

data class RegisterIndividualRequest(
    val scaleCategory: String,
    val gender: Gender,
    val paymentNote: String? = null,
) {
    fun toCommand(competitionId: UUID) = com.waytofit.competition.application.port.`in`.RegisterIndividualCommand(
        competitionId = competitionId,
        scaleCategory = scaleCategory,
        gender = gender,
        paymentNote = paymentNote
    )
}

data class TeamMemberInput(
    val userId: UUID,
    val gender: Gender,
)

data class RegisterTeamRequest(
    val teamName: String,
    val scaleCategory: String,
    val members: List<TeamMemberInput>,
    val paymentNote: String? = null,
) {
    fun toCommand(competitionId: UUID) = com.waytofit.competition.application.port.`in`.RegisterTeamCommand(
        competitionId = competitionId,
        teamName = teamName,
        scaleCategory = scaleCategory,
        members = members.map { com.waytofit.competition.application.port.`in`.TeamMemberInput(it.userId, it.gender) },
        paymentNote = paymentNote
    )
}

data class UpdatePaymentStatusRequest(
    val paymentStatus: PaymentStatus,
) {
    fun toCommand(registrationId: UUID) = com.waytofit.competition.application.port.`in`.UpdatePaymentStatusCommand(
        registrationId = registrationId,
        paymentStatus = paymentStatus
    )
}

data class TeamMemberResponse(
    val userId: UUID,
    val gender: Gender,
) {
    companion object {
        fun fromDomain(domain: CompetitionTeamMember) = TeamMemberResponse(
            userId = domain.userId,
            gender = domain.gender
        )
    }
}

data class RegistrationResponse(
    val id: UUID,
    val competitionId: UUID,
    val userId: UUID,
    val athleteName: String?,
    val registrationType: RegistrationType,
    val teamName: String?,
    val scaleCategory: String,
    val paymentStatus: PaymentStatus,
    val gender: Gender,
    val paymentNote: String?,
    val members: List<TeamMemberResponse>?,
    val createdAt: Instant?,
) {
    companion object {
        fun fromDomain(
            domain: CompetitionRegistration,
            members: List<CompetitionTeamMember>? = null,
            athleteName: String? = null,
        ) = RegistrationResponse(
            id = domain.id!!,
            competitionId = domain.competitionId,
            userId = domain.userId,
            athleteName = athleteName,
            registrationType = domain.registrationType,
            teamName = domain.teamName,
            scaleCategory = domain.scaleCategory,
            paymentStatus = domain.paymentStatus,
            gender = domain.gender,
            paymentNote = domain.paymentNote,
            members = members?.map { TeamMemberResponse.fromDomain(it) },
            createdAt = domain.audit.createdAt
        )
    }
}
