package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.GetEventLeaderboardQuery
import com.waytofit.competition.application.port.`in`.GetOverallLeaderboardQuery
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.CompetitionHistorySnapshot
import com.waytofit.competition.domain.EventScoreSnapshot
import com.waytofit.competition.domain.enums.CompetitionLifecycle
import com.waytofit.competition.domain.enums.StageType
import com.waytofit.competition.domain.event.CompetitionCompletedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Component
class CompetitionSnapshotEventListener(
    private val competitionRepository: CompetitionRepository,
    private val registrationRepository: CompetitionRegistrationRepository,
    private val teamMemberRepository: CompetitionTeamMemberRepository,
    private val stageRepository: CompetitionStageRepository,
    private val eventRepository: CompetitionEventRepository,
    private val leaderboardService: LeaderboardService,
    private val snapshotRepository: CompetitionHistorySnapshotRepository,
    private val userPersistencePort: com.waytofit.user.application.port.out.UserPersistencePort,
    private val clock: Clock,
) {

    @Async
    @EventListener
    @Transactional
    fun handleCompetitionCompletedEvent(event: CompetitionCompletedEvent) {
        if (snapshotRepository.existsByCompetitionId(event.competitionId)) return

        val competition = competitionRepository.findById(event.competitionId) ?: return
        if (competition.lifecycleAt(Instant.now(clock)) != CompetitionLifecycle.COMPLETED) return

        val registrations = registrationRepository.findRegistrationsByCompetitionId(competition.id!!, null, org.springframework.data.domain.Pageable.unpaged()).content
        if (registrations.isEmpty()) return

        val stages = stageRepository.findAllByCompetitionId(competition.id!!)
        val finalStage = stages.find { it.stageType == StageType.FINAL } ?: stages.maxByOrNull { it.endAt } ?: return

        val categories = registrations.map { it.scaleCategory }.distinct()
        val genders = registrations.flatMap { reg ->
            teamMemberRepository.findByRegistrationId(reg.id!!).mapNotNull { member ->
                userPersistencePort.findById(member.userId)?.gender
            }
        }.distinct()

        val snapshots = mutableListOf<CompetitionHistorySnapshot>()

        for (category in categories) {
            for (gender in genders) {
                // Calculate Overall Leaderboard for this category and gender
                val overallResult = leaderboardService.getOverallLeaderboard(
                    GetOverallLeaderboardQuery(finalStage.id!!, null, gender, category)
                )

                // Calculate Event Leaderboards
                val eventResults = stages.flatMap { stage ->
                    eventRepository.findAllByStageId(stage.id!!).map { event ->
                        event.id!! to leaderboardService.getEventLeaderboard(
                            GetEventLeaderboardQuery(event.id!!, gender, category)
                        )
                    }
                }.toMap()

                overallResult.entries.forEach { overallEntry ->
                    val registration = registrations.find { it.id == overallEntry.registrationId } ?: return@forEach
                    
                    val eventScoreSnapshots = eventResults.mapNotNull { (eventId, res) ->
                        val eventEntry = res.entries.find { it.registrationId == registration.id } ?: return@mapNotNull null
                        val eventName = eventRepository.findById(eventId)?.name ?: "Unknown"
                        
                        EventScoreSnapshot(
                            eventId = eventId,
                            eventName = eventName,
                            rank = eventEntry.rank,
                            resultStatus = eventEntry.resultStatus,
                            resultTimeSeconds = eventEntry.resultTimeSeconds,
                            resultRounds = eventEntry.resultRounds,
                            resultReps = eventEntry.resultReps,
                            resultWeight = eventEntry.resultWeight,
                            resultCustom = eventEntry.resultCustom
                        )
                    }

                    // Create snapshots for all team members (or individual)
                    overallEntry.memberIds.forEach { memberId ->
                        snapshots.add(
                            CompetitionHistorySnapshot(
                                competitionId = competition.id!!,
                                userId = memberId,
                                registrationId = registration.id!!,
                                registrationType = registration.registrationType,
                                scaleCategory = registration.scaleCategory,
                                competitionName = competition.name,
                                bannerImageUrl = competition.bannerImageUrl,
                                competitionEndAt = competition.endAt,
                                overallRank = overallEntry.rank,
                                totalPoints = overallEntry.totalPoints,
                                eventScores = eventScoreSnapshots
                            )
                        )
                    }
                }
            }
        }

        snapshotRepository.saveAll(snapshots)
    }
}
