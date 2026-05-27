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

    private fun <T> any(type: Class<T>): T = org.mockito.ArgumentMatchers.any(type)
}
