package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.UpdateAthleteProfileCommand
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.AthleteProfile
import com.waytofit.global.error.BusinessException
import com.waytofit.user.domain.User
import com.waytofit.user.domain.enums.Gender
import com.waytofit.user.domain.enums.OAuthProvider
import com.waytofit.user.domain.enums.UserRole
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
    fun `updateAthleteProfile updates biography and profile image`() {
        val userId = UUID.randomUUID()
        val existingProfile = AthleteProfile(id = UUID.randomUUID(), userId = userId)
        val existingUser = User(
            id = userId,
            oauthProvider = OAuthProvider.GOOGLE,
            oauthId = "oauth-athlete",
            email = "athlete@test.com",
            name = "Athlete",
            phone = null,
            gender = Gender.MALE,
            role = UserRole.USER,
        )
        val command = UpdateAthleteProfileCommand(biography = "CrossFit Lover", profileImageUrl = "http://image.url")

        `when`(athleteProfileRepository.findByUserId(userId)).thenReturn(existingProfile)
        `when`(athleteProfileRepository.save(any(AthleteProfile::class.java))).thenAnswer { it.arguments[0] }
        `when`(userPersistencePort.findById(userId)).thenReturn(existingUser)

        val result = athleteProfileService.updateAthleteProfile(userId, command)

        assertEquals("CrossFit Lover", result.biography)
        assertEquals("http://image.url", result.profileImageUrl)
        assertEquals("Athlete", result.name)
        assertEquals(Gender.MALE, result.gender)
    }

    @Test
    fun `updateAthleteProfile updates gender through user persistence`() {
        val userId = UUID.randomUUID()
        val existingProfile = AthleteProfile(id = UUID.randomUUID(), userId = userId)
        val existingUser = User(
            id = userId,
            oauthProvider = OAuthProvider.GOOGLE,
            oauthId = "oauth-athlete",
            email = "athlete@test.com",
            name = "Athlete",
            phone = null,
            gender = Gender.MALE,
            role = UserRole.USER,
        )
        val updatedUser = existingUser.copy(gender = Gender.FEMALE)
        val command = UpdateAthleteProfileCommand(
            biography = null,
            profileImageUrl = null,
            gender = Gender.FEMALE,
        )

        `when`(athleteProfileRepository.findByUserId(userId)).thenReturn(existingProfile)
        `when`(athleteProfileRepository.save(any(AthleteProfile::class.java))).thenAnswer { it.arguments[0] }
        `when`(userPersistencePort.findById(userId)).thenReturn(existingUser)
        `when`(userPersistencePort.save(any(User::class.java))).thenReturn(updatedUser)

        val result = athleteProfileService.updateAthleteProfile(userId, command)

        assertEquals(Gender.FEMALE, result.gender)
        verify(userPersistencePort).save(existingUser.copy(gender = Gender.FEMALE))
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
