package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.out.AthleteProfileRepository
import com.waytofit.competition.application.port.out.CompetitionHistorySnapshotRepository
import com.waytofit.competition.domain.CompetitionHistorySnapshot
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.user.application.port.out.UserPersistencePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.Instant
import java.util.UUID

class AthleteHistoryTest {

    private val athleteProfileRepository = mock(AthleteProfileRepository::class.java)
    private val competitionRepository = mock(com.waytofit.competition.application.port.out.CompetitionRepository::class.java)
    private val competitionRegistrationRepository = mock(com.waytofit.competition.application.port.out.CompetitionRegistrationRepository::class.java)
    private val competitionTeamMemberRepository = mock(com.waytofit.competition.application.port.out.CompetitionTeamMemberRepository::class.java)
    private val stageRepository = mock(com.waytofit.competition.application.port.out.CompetitionStageRepository::class.java)
    private val eventRepository = mock(com.waytofit.competition.application.port.out.CompetitionEventRepository::class.java)
    private val leaderboardService = mock(LeaderboardService::class.java)
    private val userPersistencePort = mock(UserPersistencePort::class.java)
    private val snapshotRepository = mock(CompetitionHistorySnapshotRepository::class.java)

    private val athleteProfileService = AthleteProfileService(
        athleteProfileRepository,
        competitionRepository,
        competitionRegistrationRepository,
        competitionTeamMemberRepository,
        stageRepository,
        eventRepository,
        leaderboardService,
        userPersistencePort,
        snapshotRepository
    )

    @Test
    fun `getAthleteCompetitionHistory returns competition history from snapshots`() {
        val userId = UUID.randomUUID()
        val snapshot = CompetitionHistorySnapshot(
            competitionId = UUID.randomUUID(),
            userId = userId,
            registrationId = UUID.randomUUID(),
            registrationType = RegistrationType.INDIVIDUAL,
            scaleCategory = "RXD",
            competitionName = "Test Competition",
            bannerImageUrl = "banner",
            competitionEndAt = Instant.now(),
            overallRank = 1,
            totalPoints = 10,
            eventScores = emptyList()
        )

        `when`(snapshotRepository.findAllByUserId(userId)).thenReturn(listOf(snapshot))

        val result = athleteProfileService.getAthleteCompetitionHistory(userId)

        assertEquals(1, result.items.size)
        assertEquals("Test Competition", result.items[0].name)
        assertEquals(1, result.items[0].overallRank)
    }
}
