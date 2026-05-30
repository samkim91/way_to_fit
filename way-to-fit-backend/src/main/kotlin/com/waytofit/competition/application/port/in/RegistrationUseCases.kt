package com.waytofit.competition.application.port.`in`

import com.waytofit.competition.domain.CompetitionRegistration
import com.waytofit.competition.domain.EventLineup
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.user.domain.enums.Gender
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface RegisterIndividualUseCase {
    fun registerIndividual(command: RegisterIndividualCommand, userId: UUID): CompetitionRegistration
}

interface RegisterTeamUseCase {
    fun registerTeam(command: RegisterTeamCommand, userId: UUID): CompetitionRegistration
}

interface GetRegistrationUseCase {
    fun getMyRegistrations(competitionId: UUID, userId: UUID): List<CompetitionRegistration>
}

interface SetEventLineupUseCase {
    fun setEventLineup(command: SetLineupCommand, userId: UUID): EventLineup
}

interface GetEventLineupUseCase {
    fun getEventLineup(eventId: UUID, registrationId: UUID): EventLineup?
}

data class RegistrationWithName(
    val registration: CompetitionRegistration,
    val athleteName: String?,
)

interface OrganizerRegistrationQueryUseCase {
    fun getRegistrations(competitionId: UUID, paymentStatus: PaymentStatus?, pageable: Pageable, userId: UUID): Page<RegistrationWithName>
}

interface OrganizerRegistrationCommandUseCase {
    fun updatePaymentStatus(command: UpdatePaymentStatusCommand, userId: UUID): CompetitionRegistration
}

interface SearchTeamMemberUseCase {
    fun searchPotentialMembers(competitionId: UUID, name: String): List<com.waytofit.competition.domain.AthleteSearchResult>
}

data class RegisterIndividualCommand(
    val competitionId: UUID,
    val gender: Gender,
    val scaleCategory: String,
    val paymentNote: String? = null,
)

data class TeamMemberInput(
    val userId: UUID,
    val gender: Gender,
)

data class RegisterTeamCommand(
    val competitionId: UUID,
    val teamName: String,
    val scaleCategory: String,
    val members: List<TeamMemberInput>,
    val paymentNote: String? = null,
)

data class SetLineupCommand(
    val eventId: UUID,
    val registrationId: UUID,
    val participatingMemberIds: List<UUID>,
)

data class UpdatePaymentStatusCommand(
    val registrationId: UUID,
    val paymentStatus: PaymentStatus,
    val paymentNote: String? = null,
)
