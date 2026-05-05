package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.*
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.CompetitionScore
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.competition.domain.enums.ScoreStatus
import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import com.waytofit.user.application.port.out.UserPersistencePort
import com.waytofit.competition.domain.enums.ResultStatus
import com.waytofit.competition.domain.enums.WodType
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional(readOnly = true)
class LeaderboardService(
    private val scoreRepository: CompetitionScoreRepository,
    private val eventRepository: CompetitionEventRepository,
    private val stageRepository: CompetitionStageRepository,
    private val registrationRepository: CompetitionRegistrationRepository,
    private val teamMemberRepository: CompetitionTeamMemberRepository,
    private val organizerRepository: CompetitionOrganizerRepository,
    private val userPersistencePort: UserPersistencePort,
) : GetEventLeaderboardUseCase, GetOverallLeaderboardUseCase, OverrideRankUseCase {

    @Cacheable(value = ["eventLeaderboard"], key = "#query.eventId.toString() + #query.gender + #query.scaleCategory")
    override fun getEventLeaderboard(query: GetEventLeaderboardQuery): EventLeaderboardResult {
        val event = eventRepository.findById(query.eventId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND, "이벤트를 찾을 수 없습니다.")

        val detailedScores = scoreRepository.findScoresWithDetailsByEventId(query.eventId, null)
            .filter { it.score.status == ScoreStatus.APPROVED || it.score.status == ScoreStatus.ADJUSTED }

        val validEntries = detailedScores.filter { scoreDetails ->
            val scaleMatch = query.scaleCategory == null || scoreDetails.scaleCategory == query.scaleCategory
            val genderMatch = query.gender == null || scoreDetails.gender == query.gender
            scaleMatch && genderMatch
        }.map { scoreDetails ->
            ScoreEntryData(
                score = scoreDetails.score,
                registrationId = scoreDetails.registrationId,
                registrationType = scoreDetails.registrationType,
                participantName = scoreDetails.participantName,
                scaleCategory = scoreDetails.scaleCategory,
                memberUserIds = scoreDetails.memberUserIds,
                manualRank = scoreDetails.manualRank
            )
        }

        val comparator = getComparator(event.wodType)
        val sortedEntries = validEntries.sortedWith(comparator)

        val finalEntries = mutableListOf<LeaderboardEntry>()
        var currentRank = 1
        var previousEntry: ScoreEntryData? = null
        var rankOffset = 0

        for (entry in sortedEntries) {
            if (previousEntry != null) {
                if (comparator.compare(previousEntry, entry) != 0) {
                    currentRank += rankOffset
                    rankOffset = 1
                } else {
                    rankOffset++
                }
            } else {
                rankOffset = 1
            }

            finalEntries.add(
                LeaderboardEntry(
                    rank = currentRank,
                    registrationId = entry.registrationId,
                    registrationType = entry.registrationType,
                    participantName = entry.participantName,
                    scaleCategory = entry.scaleCategory,
                    resultStatus = entry.score.resultStatus,
                    resultTimeSeconds = entry.score.resultTimeSeconds,
                    resultRounds = entry.score.resultRounds,
                    resultReps = entry.score.resultReps,
                    resultWeight = entry.score.resultWeight,
                    resultCustom = entry.score.resultCustom,
                    videoUrl = entry.score.videoUrl,
                    memberIds = entry.memberUserIds,
                    manualRank = entry.manualRank
                )
            )
            previousEntry = entry
        }

        return EventLeaderboardResult(eventId = query.eventId, entries = finalEntries)
    }

    @Cacheable(value = ["overallLeaderboard"], key = "#query.stageId.toString() + #query.registrationType + #query.gender + #query.scaleCategory")
    override fun getOverallLeaderboard(query: GetOverallLeaderboardQuery): OverallLeaderboardResult {
        stageRepository.findById(query.stageId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        val events = eventRepository.findAllByStageId(query.stageId).sortedBy { it.order }
        if (events.isEmpty()) return OverallLeaderboardResult(query.stageId, emptyList())

        val eventResults = events.map { event ->
            event.id!! to getEventLeaderboard(GetEventLeaderboardQuery(event.id, query.gender, query.scaleCategory))
        }.toMap()

        val allRegistrationEntries = eventResults.values.flatMap { res -> res.entries }
            .filter { query.registrationType == null || it.registrationType == query.registrationType }
            .groupBy { it.registrationId }

        val overallEntries = mutableListOf<OverallEntryData>()

        for ((regId, entries) in allRegistrationEntries) {
            val firstEntry = entries.first()
            var totalPoints = 0
            val eventRanks = mutableMapOf<UUID, Int>()

            for (event in events) {
                val eventRes = eventResults[event.id!!]
                val entry = eventRes?.entries?.find { it.registrationId == regId }

                val rank = entry?.rank ?: (eventRes?.entries?.size ?: 0) + 1
                totalPoints += rank
                eventRanks[event.id] = rank
            }

            overallEntries.add(
                OverallEntryData(
                    registrationId = regId,
                    participantName = firstEntry.participantName,
                    scaleCategory = firstEntry.scaleCategory,
                    totalPoints = totalPoints,
                    eventRanks = eventRanks,
                    manualRank = firstEntry.manualRank,
                    memberIds = firstEntry.memberIds
                )
            )
        }

        val lastEventId = events.last().id!!
        val sortedEntries = overallEntries.sortedWith(compareBy<OverallEntryData> { it.totalPoints }
            .thenBy { it.eventRanks[lastEventId] ?: Int.MAX_VALUE })

        val finalEntries = mutableListOf<OverallLeaderboardEntry>()
        var currentRank = 1
        var previousEntry: OverallEntryData? = null
        var rankOffset = 0

        for (entry in sortedEntries) {
            if (previousEntry != null) {
                if (previousEntry.totalPoints != entry.totalPoints ||
                    previousEntry.eventRanks[lastEventId] != entry.eventRanks[lastEventId]) {
                    currentRank += rankOffset
                    rankOffset = 1
                } else {
                    rankOffset++
                }
            } else {
                rankOffset = 1
            }

            finalEntries.add(
                OverallLeaderboardEntry(
                    rank = entry.manualRank ?: currentRank,
                    registrationId = entry.registrationId,
                    participantName = entry.participantName,
                    scaleCategory = entry.scaleCategory,
                    totalPoints = entry.totalPoints,
                    eventRanks = entry.eventRanks,
                    manualRank = entry.manualRank,
                    memberIds = entry.memberIds
                )
            )
            previousEntry = entry
        }

        return OverallLeaderboardResult(stageId = query.stageId, entries = finalEntries.sortedBy { it.rank })
    }

    @Transactional
    override fun overrideRank(command: OverrideRankCommand, userId: UUID) {
        val registration = registrationRepository.findById(command.registrationId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        if (!organizerRepository.isOrganizer(registration.competitionId, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }

        val updatedRegistration = registration.copy(manualRank = command.manualRank)
        registrationRepository.save(updatedRegistration)
    }

    private fun getComparator(wodType: WodType): Comparator<ScoreEntryData> {
        return Comparator { a, b ->
            when (wodType) {
                WodType.FOR_TIME -> {
                    val timeA = if (a.score.resultStatus == ResultStatus.DNF) Int.MAX_VALUE else a.score.resultTimeSeconds ?: Int.MAX_VALUE
                    val timeB = if (b.score.resultStatus == ResultStatus.DNF) Int.MAX_VALUE else b.score.resultTimeSeconds ?: Int.MAX_VALUE
                    timeA.compareTo(timeB)
                }
                WodType.AMRAP -> {
                    val roundA = a.score.resultRounds ?: 0
                    val repA = a.score.resultReps ?: 0
                    val roundB = b.score.resultRounds ?: 0
                    val repB = b.score.resultReps ?: 0
                    if (roundA != roundB) roundB.compareTo(roundA) else repB.compareTo(repA)
                }
                WodType.MAX_WEIGHT -> {
                    val weightA = a.score.resultWeight ?: BigDecimal.ZERO
                    val weightB = b.score.resultWeight ?: BigDecimal.ZERO
                    weightB.compareTo(weightA)
                }
                else -> 0
            }
        }
    }

    private data class ScoreEntryData(
        val score: CompetitionScore,
        val registrationId: UUID,
        val registrationType: RegistrationType,
        val participantName: String,
        val scaleCategory: String,
        val memberUserIds: List<UUID>,
        val manualRank: Int?
    )

    private data class OverallEntryData(
        val registrationId: UUID,
        val participantName: String,
        val scaleCategory: String,
        val totalPoints: Int,
        val eventRanks: Map<UUID, Int>,
        val manualRank: Int?,
        val memberIds: List<UUID>
    )
}
