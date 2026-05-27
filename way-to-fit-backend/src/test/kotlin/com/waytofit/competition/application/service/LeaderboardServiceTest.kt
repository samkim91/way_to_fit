package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.GetEventLeaderboardQuery
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
import com.waytofit.competition.domain.enums.GenderCategory
import com.waytofit.competition.domain.enums.WodType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.Instant
import java.util.UUID

class LeaderboardServiceTest {

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
    fun `getEventLeaderboard calculates rank correctly for FOR_TIME`() {
        val competitionId = UUID.randomUUID()
        val stageId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val event = CompetitionEvent(
            id = eventId, stageId = stageId, competitionId = competitionId, name = "WOD 1", description = "", eventType = EventType.INDIVIDUAL, 
            gender = GenderCategory.MIXED, wodType = WodType.FOR_TIME, order = 1, scaleCategories = emptyList(), 
            submissionDeadline = Instant.now()
        )
        
        val reg10Id = UUID.randomUUID()
        val reg11Id = UUID.randomUUID()
        val reg12Id = UUID.randomUUID()

        val details = listOf(
            CompetitionScoreDetails(
                score = CompetitionScore(id = UUID.randomUUID(), eventId = eventId, registrationId = reg10Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 300, videoUrl = "", status = ScoreStatus.APPROVED),
                registrationId = reg10Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "User10", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = listOf(UUID.randomUUID())
            ),
            CompetitionScoreDetails(
                score = CompetitionScore(id = UUID.randomUUID(), eventId = eventId, registrationId = reg11Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 250, videoUrl = "", status = ScoreStatus.APPROVED),
                registrationId = reg11Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "User11", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = listOf(UUID.randomUUID())
            ),
            CompetitionScoreDetails(
                score = CompetitionScore(id = UUID.randomUUID(), eventId = eventId, registrationId = reg12Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 300, videoUrl = "", status = ScoreStatus.APPROVED),
                registrationId = reg12Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "User12", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = listOf(UUID.randomUUID())
            )
        )

        `when`(eventRepository.findById(eventId)).thenReturn(event)
        `when`(scoreRepository.findScoresWithDetailsByEventId(eventId, null)).thenReturn(details)

        val result = leaderboardService.getEventLeaderboard(GetEventLeaderboardQuery(eventId, null, null))

        assertEquals(3, result.entries.size)
        assertEquals(1, result.entries[0].rank)
        assertEquals(reg11Id, result.entries[0].registrationId)
        assertEquals(2, result.entries[1].rank)
        assertEquals(reg10Id, result.entries[1].registrationId)
        assertEquals(2, result.entries[2].rank)
        assertEquals(reg12Id, result.entries[2].registrationId)
    }

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
    fun `getEventLeaderboard ranks independently per scaleCategory when no filter`() {
        val competitionId = UUID.randomUUID()
        val stageId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val event = CompetitionEvent(
            id = eventId, stageId = stageId, competitionId = competitionId, name = "WOD 1", description = "",
            eventType = EventType.INDIVIDUAL, gender = GenderCategory.MIXED, wodType = WodType.FOR_TIME,
            order = 1, scaleCategories = listOf("RXD", "SCALED"), submissionDeadline = Instant.now()
        )

        val rxd1Id = UUID.randomUUID()
        val rxd2Id = UUID.randomUUID()
        val scaled1Id = UUID.randomUUID()
        val scaled2Id = UUID.randomUUID()

        val details = listOf(
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = eventId, registrationId = rxd1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 100, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD1", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = eventId, registrationId = rxd2Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 200, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd2Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD2", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = eventId, registrationId = scaled1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 50, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = scaled1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "SCALED1", scaleCategory = "SCALED", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = eventId, registrationId = scaled2Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 300, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = scaled2Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "SCALED2", scaleCategory = "SCALED", gender = Gender.MALE, memberUserIds = emptyList())
        )

        `when`(eventRepository.findById(eventId)).thenReturn(event)
        `when`(scoreRepository.findScoresWithDetailsByEventId(eventId, null)).thenReturn(details)

        val result = leaderboardService.getEventLeaderboard(GetEventLeaderboardQuery(eventId, null, null))

        assertEquals(4, result.entries.size)
        // RXD 그룹: rxd1(100s)=1위, rxd2(200s)=2위
        assertEquals(1, result.entries.first { it.registrationId == rxd1Id }.rank)
        assertEquals(2, result.entries.first { it.registrationId == rxd2Id }.rank)
        // SCALED 그룹: scaled1(50s)=1위, scaled2(300s)=2위 (RXD 시간과 무관하게 독립 순위)
        assertEquals(1, result.entries.first { it.registrationId == scaled1Id }.rank)
        assertEquals(2, result.entries.first { it.registrationId == scaled2Id }.rank)
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

        // E1: rxd1(100s)=RXD 1위, rxd2(200s)=RXD 2위, scaled1(50s)=SCALED 1위
        val detailsE1 = listOf(
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = rxd1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 100, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD1", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = rxd2Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 200, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd2Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD2", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = scaled1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 50, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = scaled1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "SCALED1", scaleCategory = "SCALED", gender = Gender.MALE, memberUserIds = emptyList())
        )
        // E2: rxd2(80s)=RXD 1위, rxd1(150s)=RXD 2위, scaled1(120s)=SCALED 1위
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

        // RXD: rxd1(1+2=3pts)=2위, rxd2(2+1=3pts)... tie-breaker: E2 rxd2=1위 → rxd2 종합 1위, rxd1 종합 2위
        val rxd2Entry = result.entries.first { it.registrationId == rxd2Id }
        val rxd1Entry = result.entries.first { it.registrationId == rxd1Id }
        assertEquals(1, rxd2Entry.rank)
        assertEquals(2, rxd1Entry.rank)
        // SCALED: scaled1 단독 → 1위
        val scaled1Entry = result.entries.first { it.registrationId == scaled1Id }
        assertEquals(1, scaled1Entry.rank)
        // scaled1의 rank=1이 rxd 선수들과 독립 (RXD와 rank 비교 안 함)
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

        // E1: rxd1=1위, rxd2=2위, rxd3=3위
        val detailsE1 = listOf(
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = rxd1Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 100, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd1Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD1", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = rxd2Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 200, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd2Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD2", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList()),
            CompetitionScoreDetails(score = CompetitionScore(id = UUID.randomUUID(), eventId = event1Id, registrationId = rxd3Id, resultStatus = ResultStatus.COMPLETED, resultTimeSeconds = 300, videoUrl = "", status = ScoreStatus.APPROVED), registrationId = rxd3Id, registrationType = RegistrationType.INDIVIDUAL, participantName = "RXD3", scaleCategory = "RXD", gender = Gender.MALE, memberUserIds = emptyList())
        )
        // E2: rxd1, rxd2 제출 (rxd3 기권)
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
        // rxd3 기권 페널티 = RXD E2 참가자 수(2명) + 1 = 3점
        // rxd3 총점 = E1 3위 + E2 페널티 = 3 + 3 = 6점
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
