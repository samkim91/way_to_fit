package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.GetOverallLeaderboardQuery
import com.waytofit.competition.application.port.`in`.OverrideRankCommand
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.CompetitionEvent
import com.waytofit.competition.domain.CompetitionRegistration
import com.waytofit.competition.domain.CompetitionScore
import com.waytofit.competition.domain.CompetitionStage
import com.waytofit.competition.domain.enums.*
import com.waytofit.user.application.port.out.UserPersistencePort
import com.waytofit.user.domain.enums.Gender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.Instant
import java.util.UUID

class OverallLeaderboardTest {

    private val scoreRepository = mock(CompetitionScoreRepository::class.java)
    private val eventRepository = mock(CompetitionEventRepository::class.java)
    private val stageRepository = mock(CompetitionStageRepository::class.java)
    private val registrationRepository = mock(CompetitionRegistrationRepository::class.java)
    private val teamMemberRepository = mock(CompetitionTeamMemberRepository::class.java)
    private val organizerRepository = mock(CompetitionOrganizerRepository::class.java)
    private val userPersistencePort = mock(UserPersistencePort::class.java)
    private val leaderboardService = LeaderboardService(
        scoreRepository, eventRepository, stageRepository, registrationRepository, teamMemberRepository, organizerRepository, userPersistencePort
    )

    @Test
    fun `getOverallLeaderboard sums ranks correctly`() {
        val competitionId = UUID.randomUUID()
        val stageId = UUID.randomUUID()
        val event1Id = UUID.randomUUID()
        val event2Id = UUID.randomUUID()
        val reg1Id = UUID.randomUUID()
        val reg2Id = UUID.randomUUID()

        val stage = CompetitionStage(id = stageId, competitionId = competitionId, name = "S", stageType = StageType.QUALIFIER, stageFormat = StageFormat.ONLINE, startAt = Instant.now(), endAt = Instant.now())
        val events = listOf(
            CompetitionEvent(id = event1Id, stageId = stageId, competitionId = competitionId, name = "E1", description = "", eventType = EventType.INDIVIDUAL, gender = GenderCategory.MIXED, wodType = WodType.FOR_TIME, order = 1, scaleCategories = emptyList(), submissionDeadline = Instant.now()),
            CompetitionEvent(id = event2Id, stageId = stageId, competitionId = competitionId, name = "E2", description = "", eventType = EventType.INDIVIDUAL, gender = GenderCategory.MIXED, wodType = WodType.FOR_TIME, order = 2, scaleCategories = emptyList(), submissionDeadline = Instant.now())
        )

        val detailsE1 = listOf(
            CompetitionScoreDetails(
                score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = reg1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 100, videoUrl = "", status = ScoreStatus.APPROVED),
                registrationId = reg1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "U1", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = listOf(UUID.randomUUID())
            ),
            CompetitionScoreDetails(
                score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = reg2Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 200, videoUrl = "", status = ScoreStatus.APPROVED),
                registrationId = reg2Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "U2", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = listOf(UUID.randomUUID())
            )
        )
        val detailsE2 = listOf(
            CompetitionScoreDetails(
                score = CompetitionScore(id = UUID.randomUUID(), eventId = event2Id, registrationId = reg1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 200, videoUrl = "", status = ScoreStatus.APPROVED),
                registrationId = reg1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "U1", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = listOf(UUID.randomUUID())
            ),
            CompetitionScoreDetails(
                score = CompetitionScore(id = UUID.randomUUID(), eventId = event2Id, registrationId = reg2Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 100, videoUrl = "", status = ScoreStatus.APPROVED),
                registrationId = reg2Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "U2", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = listOf(UUID.randomUUID())
            )
        )

        `when`(stageRepository.findById(stageId)).thenReturn(stage)
        `when`(eventRepository.findAllByStageId(stageId)).thenReturn(events)
        `when`(eventRepository.findById(event1Id)).thenReturn(events[0])
        `when`(eventRepository.findById(event2Id)).thenReturn(events[1])
        `when`(scoreRepository.findScoresWithDetailsByEventId(event1Id, null)).thenReturn(detailsE1)
        `when`(scoreRepository.findScoresWithDetailsByEventId(event2Id, null)).thenReturn(detailsE2)

        val result = leaderboardService.getOverallLeaderboard(GetOverallLeaderboardQuery(stageId, null, null, null))

        assertEquals(2, result.entries.size)
        assertEquals(reg2Id, result.entries[0].registrationId)
        assertEquals(1, result.entries[0].rank)
        assertEquals(reg1Id, result.entries[1].registrationId)
        assertEquals(2, result.entries[1].rank)
    }

    @Test
    fun `getOverallLeaderboard ranks independently per scaleCategory`() {
        val competitionId = UUID.randomUUID()
        val stageId = UUID.randomUUID()
        val event1Id = UUID.randomUUID()
        val event2Id = UUID.randomUUID()
        val rxd1Id = UUID.randomUUID()
        val rxd2Id = UUID.randomUUID()
        val scaled1Id = UUID.randomUUID()

        val stage = CompetitionStage(id = stageId, competitionId = competitionId, name = "S", stageType = StageType.QUALIFIER, stageFormat = StageFormat.ONLINE, startAt = Instant.now(), endAt = Instant.now())
        val events = listOf(
            CompetitionEvent(id = event1Id, stageId = stageId, competitionId = competitionId, name = "E1", description = "", eventType = EventType.INDIVIDUAL, gender = GenderCategory.MIXED, wodType = WodType.FOR_TIME, order = 1, scaleCategories = listOf("RXD", "SCALED"), submissionDeadline = Instant.now()),
            CompetitionEvent(id = event2Id, stageId = stageId, competitionId = competitionId, name = "E2", description = "", eventType = EventType.INDIVIDUAL, gender = GenderCategory.MIXED, wodType = WodType.FOR_TIME, order = 2, scaleCategories = listOf("RXD", "SCALED"), submissionDeadline = Instant.now())
        )

        val detailsE1 = listOf(
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = rxd1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 100, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD1", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = rxd2Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 200, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd2Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD2", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = scaled1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 50, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = scaled1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "SCALED1", scaleCategory = "SCALED", gender = Gender.MALE, memberUserIds = emptyList())
        )
        val detailsE2 = listOf(
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event2Id, registrationId = rxd2Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 80, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd2Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD2", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event2Id, registrationId = rxd1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 150, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD1", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event2Id, registrationId = scaled1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 120, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = scaled1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "SCALED1", scaleCategory = "SCALED", gender = Gender.MALE, memberUserIds = emptyList())
        )

        `when`(stageRepository.findById(stageId)).thenReturn(stage)
        `when`(eventRepository.findAllByStageId(stageId)).thenReturn(events)
        `when`(eventRepository.findById(event1Id)).thenReturn(events[0])
        `when`(eventRepository.findById(event2Id)).thenReturn(events[1])
        `when`(scoreRepository.findScoresWithDetailsByEventId(event1Id, null)).thenReturn(detailsE1)
        `when`(scoreRepository.findScoresWithDetailsByEventId(event2Id, null)).thenReturn(detailsE2)

        val result = leaderboardService.getOverallLeaderboard(GetOverallLeaderboardQuery(stageId, null, null, null))

        val rxd2Entry = result.entries.first { it.registrationId == rxd2Id }
        val rxd1Entry = result.entries.first { it.registrationId == rxd1Id }
        assertEquals(1, rxd2Entry.rank)
        assertEquals(2, rxd1Entry.rank)
        val scaled1Entry = result.entries.first { it.registrationId == scaled1Id }
        assertEquals(1, scaled1Entry.rank)
        assertEquals(3, result.entries.size)
    }

    @Test
    fun `getOverallLeaderboard penalty uses same scaleCategory count`() {
        val competitionId = UUID.randomUUID()
        val stageId = UUID.randomUUID()
        val event1Id = UUID.randomUUID()
        val event2Id = UUID.randomUUID()
        val rxd1Id = UUID.randomUUID()
        val rxd2Id = UUID.randomUUID()
        val rxd3Id = UUID.randomUUID()

        val stage = CompetitionStage(id = stageId, competitionId = competitionId, name = "S", stageType = StageType.QUALIFIER, stageFormat = StageFormat.ONLINE, startAt = Instant.now(), endAt = Instant.now())
        val events = listOf(
            CompetitionEvent(id = event1Id, stageId = stageId, competitionId = competitionId, name = "E1", description = "", eventType = EventType.INDIVIDUAL, gender = GenderCategory.MIXED, wodType = WodType.FOR_TIME, order = 1, scaleCategories = listOf("RXD"), submissionDeadline = Instant.now()),
            CompetitionEvent(id = event2Id, stageId = stageId, competitionId = competitionId, name = "E2", description = "", eventType = EventType.INDIVIDUAL, gender = GenderCategory.MIXED, wodType = WodType.FOR_TIME, order = 2, scaleCategories = listOf("RXD"), submissionDeadline = Instant.now())
        )

        val detailsE1 = listOf(
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = rxd1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 100, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD1", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = rxd2Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 200, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd2Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD2", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = rxd3Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 300, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd3Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD3", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList())
        )
        val detailsE2 = listOf(
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event2Id, registrationId = rxd1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 100, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD1", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event2Id, registrationId = rxd2Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 200, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd2Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD2", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList())
        )

        `when`(stageRepository.findById(stageId)).thenReturn(stage)
        `when`(eventRepository.findAllByStageId(stageId)).thenReturn(events)
        `when`(eventRepository.findById(event1Id)).thenReturn(events[0])
        `when`(eventRepository.findById(event2Id)).thenReturn(events[1])
        `when`(scoreRepository.findScoresWithDetailsByEventId(event1Id, null)).thenReturn(detailsE1)
        `when`(scoreRepository.findScoresWithDetailsByEventId(event2Id, null)).thenReturn(detailsE2)

        val result = leaderboardService.getOverallLeaderboard(GetOverallLeaderboardQuery(stageId, null, null, null))

        val rxd3Entry = result.entries.first { it.registrationId == rxd3Id }
        assertEquals(6, rxd3Entry.totalPoints)
    }

    @Test
    fun `overrideRank updates manualRank`() {
        val competitionId = UUID.randomUUID()
        val regId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = OverrideRankCommand(regId, 5)
        val registration = CompetitionRegistration(id = regId, competitionId = competitionId, userId = UUID.randomUUID(), registrationType = RegistrationType.INDIVIDUAL, gender = Gender.MALE, scaleCategory = "RXD", paymentStatus = PaymentStatus.CONFIRMED)

        `when`(registrationRepository.findById(regId)).thenReturn(registration)
        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(true)

        leaderboardService.overrideRank(command, userId)

        verify(registrationRepository).save(any(CompetitionRegistration::class.java))
    }

    private fun <T> any(type: Class<T>): T = org.mockito.ArgumentMatchers.any(type)
}
