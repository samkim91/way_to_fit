package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.UpdateAthleteProfileCommand
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.AthleteProfile
import com.waytofit.global.error.BusinessException
import com.waytofit.user.application.port.out.UserPersistencePort
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.UUID

class AthleteProfileServiceTest {

    private val athleteProfileRepository = mock(AthleteProfileRepository::class.java)
    private val competitionRepository = mock(CompetitionRepository::class.java)
    private val competitionRegistrationRepository = mock(CompetitionRegistrationRepository::class.java)
    private val competitionTeamMemberRepository = mock(CompetitionTeamMemberRepository::class.java)
    private val stageRepository = mock(CompetitionStageRepository::class.java)
    private val eventRepository = mock(CompetitionEventRepository::class.java)
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
    fun `createProfileIfNotExists saves a new profile if it doesn't exist`() {
        val userId = UUID.randomUUID()
        `when`(athleteProfileRepository.existsByUserId(userId)).thenReturn(false)

        athleteProfileService.createProfileIfNotExists(userId)

        verify(athleteProfileRepository).save(any(AthleteProfile::class.java))
    }

    @Test
    fun `createProfileIfNotExists does not save if profile already exists`() {
        val userId = UUID.randomUUID()
        `when`(athleteProfileRepository.existsByUserId(userId)).thenReturn(true)

        athleteProfileService.createProfileIfNotExists(userId)

        verify(athleteProfileRepository, never()).save(any(AthleteProfile::class.java))
    }

    @Test
    fun `updateAthleteProfile updates biography and other fields`() {
        val userId = UUID.randomUUID()
        val boxId = UUID.randomUUID()
        val existingProfile = AthleteProfile(id = UUID.randomUUID(), userId = userId)
        val command = UpdateAthleteProfileCommand(boxId = boxId, biography = "CrossFit Lover", profileImageUrl = "http://image.url")

        `when`(athleteProfileRepository.findByUserId(userId)).thenReturn(existingProfile)
        `when`(athleteProfileRepository.save(any(AthleteProfile::class.java))).thenAnswer { it.arguments[0] }

        val result = athleteProfileService.updateAthleteProfile(userId, command)

        assertEquals("CrossFit Lover", result.biography)
        assertEquals(boxId, result.boxId)
        assertEquals("http://image.url", result.profileImageUrl)
    }

    @Test
    fun `getAthleteProfile throws exception if profile not found`() {
        val userId = UUID.randomUUID()
        `when`(athleteProfileRepository.findByUserId(userId)).thenReturn(null)

        assertThrows(BusinessException::class.java) {
            athleteProfileService.getAthleteProfile(userId)
        }
    }

    private fun <T> any(type: Class<T>): T = org.mockito.ArgumentMatchers.any(type)
}
