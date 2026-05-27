package com.waytofit.competition.application.service

import com.waytofit.competition.adapter.`in`.web.dto.AthleteCompetitionHistoryResponse
import com.waytofit.competition.adapter.`in`.web.dto.CompetitionHistoryItem
import com.waytofit.competition.adapter.`in`.web.dto.EventScoreItem
import com.waytofit.competition.application.port.`in`.*
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.AthleteProfile
import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import com.waytofit.user.application.port.out.UserPersistencePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class AthleteProfileService(
    private val athleteProfileRepository: AthleteProfileRepository,
    private val competitionRepository: CompetitionRepository,
    private val competitionRegistrationRepository: CompetitionRegistrationRepository,
    private val competitionTeamMemberRepository: CompetitionTeamMemberRepository,
    private val stageRepository: CompetitionStageRepository,
    private val eventRepository: CompetitionEventRepository,
    private val leaderboardService: LeaderboardService,
    private val userPersistencePort: UserPersistencePort,
    private val snapshotRepository: CompetitionHistorySnapshotRepository,
) : GetAthleteProfileUseCase, UpdateAthleteProfileUseCase, CreateAthleteProfileUseCase, GetAthleteCompetitionHistoryUseCase {

    @Transactional(readOnly = true)
    override fun getAthleteProfile(userId: UUID): AthleteProfile {
        val profile = athleteProfileRepository.findByUserId(userId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND, "선수 프로필을 찾을 수 없습니다.")
        val user = userPersistencePort.findById(userId)
        return profile.copy(name = user?.name ?: "")
    }

    override fun updateAthleteProfile(userId: UUID, command: UpdateAthleteProfileCommand): AthleteProfile {
        val profile = athleteProfileRepository.findByUserId(userId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND, "선수 프로필을 찾을 수 없습니다.")

        val updatedProfile = profile.copy(
            biography = command.biography,
            profileImageUrl = command.profileImageUrl
        )

        val saved = athleteProfileRepository.save(updatedProfile)
        val user = userPersistencePort.findById(userId)
        return saved.copy(name = user?.name ?: "")
    }

    override fun createProfileIfNotExists(userId: UUID) {
        if (!athleteProfileRepository.existsByUserId(userId)) {
            athleteProfileRepository.save(
                AthleteProfile(
                    userId = userId
                )
            )
        }
    }

    @Transactional(readOnly = true)
    override fun getAthleteCompetitionHistory(userId: UUID): AthleteCompetitionHistoryResponse {
        val snapshots = snapshotRepository.findAllByUserId(userId)
        
        val items = snapshots.map { snapshot ->
            CompetitionHistoryItem(
                competitionId = snapshot.competitionId,
                name = snapshot.competitionName,
                bannerImageUrl = snapshot.bannerImageUrl,
                endAt = snapshot.competitionEndAt,
                registrationId = snapshot.registrationId,
                registrationType = snapshot.registrationType,
                scaleCategory = snapshot.scaleCategory,
                overallRank = snapshot.overallRank,
                totalPoints = snapshot.totalPoints,
                eventScores = snapshot.eventScores.map { eventScore ->
                    EventScoreItem(
                        eventId = eventScore.eventId,
                        eventName = eventScore.eventName,
                        rank = eventScore.rank,
                        resultStatus = eventScore.resultStatus,
                        resultTimeSeconds = eventScore.resultTimeSeconds,
                        resultRounds = eventScore.resultRounds,
                        resultReps = eventScore.resultReps,
                        resultWeight = eventScore.resultWeight,
                        resultCustom = eventScore.resultCustom
                    )
                }
            )
        }.sortedByDescending { it.endAt }

        return AthleteCompetitionHistoryResponse(userId, items)
    }
}
