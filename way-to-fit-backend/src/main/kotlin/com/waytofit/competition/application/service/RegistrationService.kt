package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.*
import com.waytofit.competition.application.port.out.CompetitionOrganizerRepository
import com.waytofit.competition.application.port.out.CompetitionRegistrationRepository
import com.waytofit.competition.application.port.out.CompetitionRepository
import com.waytofit.competition.application.port.out.CompetitionTeamMemberRepository
import com.waytofit.competition.application.port.out.EventLineupRepository
import com.waytofit.competition.domain.CompetitionRegistration
import com.waytofit.competition.domain.CompetitionTeamMember
import com.waytofit.competition.domain.EventLineup
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.competition.domain.enums.TeamRole
import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class RegistrationService(
    private val registrationRepository: CompetitionRegistrationRepository,
    private val competitionRepository: CompetitionRepository,
    private val teamMemberRepository: CompetitionTeamMemberRepository,
    private val eventLineupRepository: EventLineupRepository,
    private val organizerRepository: CompetitionOrganizerRepository,
    private val athleteProfileRepository: com.waytofit.competition.application.port.out.AthleteProfileRepository,
) : RegisterIndividualUseCase, RegisterTeamUseCase, GetRegistrationUseCase, SetEventLineupUseCase, GetEventLineupUseCase,
    OrganizerRegistrationQueryUseCase, OrganizerRegistrationCommandUseCase, SearchTeamMemberUseCase {

    override fun registerIndividual(command: RegisterIndividualCommand, userId: UUID): CompetitionRegistration {
        val competition = competitionRepository.findById(command.competitionId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        val now = Instant.now()
        if (now.isBefore(competition.registrationStartAt) || now.isAfter(competition.registrationEndAt)) {
            throw BusinessException(ResponseCode.INVALID_PARAMETER, "참가 신청 기간이 아닙니다.")
        }

        if (registrationRepository.existsByCompetitionIdAndUserIdAndType(
                command.competitionId, userId, RegistrationType.INDIVIDUAL
            )) {
            throw BusinessException(ResponseCode.INVALID_PARAMETER, "이미 해당 대회에 개인 참가 신청을 완료했습니다.")
        }

        val registration = CompetitionRegistration(
            competitionId = command.competitionId,
            userId = userId,
            registrationType = RegistrationType.INDIVIDUAL,
            gender = command.gender,
            scaleCategory = command.scaleCategory,
            paymentStatus = PaymentStatus.PENDING,
            paymentNote = command.paymentNote
        )

        val savedRegistration = registrationRepository.save(registration)

        teamMemberRepository.saveAll(listOf(
            CompetitionTeamMember(
                registrationId = savedRegistration.id!!,
                userId = userId,
                gender = command.gender,
                teamRole = TeamRole.LEADER
            )
        ))

        return savedRegistration
    }

    override fun registerTeam(command: RegisterTeamCommand, userId: UUID): CompetitionRegistration {
        val competition = competitionRepository.findById(command.competitionId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        val now = Instant.now()
        if (now.isBefore(competition.registrationStartAt) || now.isAfter(competition.registrationEndAt)) {
            throw BusinessException(ResponseCode.INVALID_PARAMETER, "참가 신청 기간이 아닙니다.")
        }

        val allMemberUserIds = command.members.map { it.userId }.distinct()
        allMemberUserIds.forEach { memberId ->
            if (teamMemberRepository.isUserAlreadyInAnyTeam(command.competitionId, memberId)) {
                throw BusinessException(ResponseCode.INVALID_PARAMETER, "이미 해당 대회에 참가 신청된 멤버가 포함되어 있습니다: $memberId")
            }
        }

        val leaderInput = command.members.find { it.userId == userId }
            ?: throw BusinessException(ResponseCode.INVALID_PARAMETER, "팀 신청자는 멤버 목록에 포함되어야 합니다.")

        val registration = CompetitionRegistration(
            competitionId = command.competitionId,
            userId = userId,
            registrationType = RegistrationType.TEAM,
            gender = leaderInput.gender,
            teamName = command.teamName,
            scaleCategory = command.scaleCategory,
            paymentStatus = PaymentStatus.PENDING,
            paymentNote = command.paymentNote
        )

        val savedRegistration = registrationRepository.save(registration)

        val teamMembers = command.members.map { memberInput ->
            CompetitionTeamMember(
                registrationId = savedRegistration.id!!,
                userId = memberInput.userId,
                gender = memberInput.gender,
                teamRole = if (memberInput.userId == userId) TeamRole.LEADER else TeamRole.MEMBER
            )
        }
        teamMemberRepository.saveAll(teamMembers)

        return savedRegistration
    }

    @Transactional(readOnly = true)
    override fun getMyRegistrations(competitionId: UUID, userId: UUID): List<CompetitionRegistration> {
        return registrationRepository.findAllByCompetitionIdAndMemberUserId(competitionId, userId)
    }

    override fun setEventLineup(command: SetLineupCommand, userId: UUID): EventLineup {
        val teamMembers = teamMemberRepository.findByRegistrationId(command.registrationId)
        if (teamMembers.isEmpty()) {
            throw BusinessException(ResponseCode.NOT_FOUND, "참가 신청 내역을 찾을 수 없습니다.")
        }

        val requesterMember = teamMembers.find { it.userId == userId }
        if (requesterMember == null || requesterMember.teamRole != TeamRole.LEADER) {
            throw BusinessException(ResponseCode.FORBIDDEN, "팀 대표자만 라인업을 설정할 수 있습니다.")
        }

        val memberUserIds = teamMembers.map { it.userId }.toSet()
        if (!memberUserIds.containsAll(command.participatingMemberIds)) {
            throw BusinessException(ResponseCode.INVALID_PARAMETER, "팀원 목록에 없는 사용자가 포함되어 있습니다.")
        }

        val existingLineup = eventLineupRepository.findByEventIdAndRegistrationId(command.eventId, command.registrationId)
        val lineup = (existingLineup?.copy(participatingMemberIds = command.participatingMemberIds)
            ?: EventLineup(
                eventId = command.eventId,
                registrationId = command.registrationId,
                participatingMemberIds = command.participatingMemberIds
            ))

        return eventLineupRepository.save(lineup)
    }

    @Transactional(readOnly = true)
    override fun getEventLineup(eventId: UUID, registrationId: UUID): EventLineup? {
        return eventLineupRepository.findByEventIdAndRegistrationId(eventId, registrationId)
    }

    @Transactional(readOnly = true)
    override fun getRegistrations(
        competitionId: UUID,
        paymentStatus: PaymentStatus?,
        pageable: Pageable,
        userId: UUID
    ): Page<CompetitionRegistration> {
        if (!organizerRepository.isOrganizer(competitionId, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }
        return registrationRepository.findRegistrationsByCompetitionId(competitionId, paymentStatus, pageable)
    }

    override fun updatePaymentStatus(command: UpdatePaymentStatusCommand, userId: UUID): CompetitionRegistration {
        val registration = registrationRepository.findById(command.registrationId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        if (!organizerRepository.isOrganizer(registration.competitionId, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }

        val updatedRegistration = registration.copy(
            paymentStatus = command.paymentStatus,
            paymentNote = command.paymentNote ?: registration.paymentNote
        )
        return registrationRepository.save(updatedRegistration)
    }

    @Transactional(readOnly = true)
    override fun searchPotentialMembers(competitionId: UUID, name: String): List<com.waytofit.competition.domain.AthleteSearchResult> {
        val results = athleteProfileRepository.searchAthletesByName(name)
        if (results.isEmpty()) return emptyList()

        return results.filter { result ->
            !teamMemberRepository.isUserAlreadyInAnyTeam(competitionId, result.userId)
        }
    }
}
