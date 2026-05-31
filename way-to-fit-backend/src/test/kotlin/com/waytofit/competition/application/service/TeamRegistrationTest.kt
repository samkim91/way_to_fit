package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.RegisterTeamCommand
import com.waytofit.competition.application.port.`in`.TeamMemberInput
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.BankInfo
import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.CompetitionRegistration
import com.waytofit.competition.domain.enums.CompetitionVisibility
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.competition.domain.enums.TeamRole
import com.waytofit.global.error.BusinessException
import com.waytofit.user.domain.enums.Gender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.time.Instant
import java.util.UUID

class TeamRegistrationTest {

    private val registrationRepository = mock(CompetitionRegistrationRepository::class.java)
    private val competitionRepository = mock(CompetitionRepository::class.java)
    private val teamMemberRepository = mock(CompetitionTeamMemberRepository::class.java)
    private val eventLineupRepository = mock(EventLineupRepository::class.java)
    private val organizerRepository = mock(CompetitionOrganizerRepository::class.java)
    private val athleteProfileRepository = mock(AthleteProfileRepository::class.java)
    private val userQueryPort = mock(UserQueryPort::class.java)
    private val registrationService = RegistrationService(
        registrationRepository, competitionRepository, teamMemberRepository, eventLineupRepository, organizerRepository, athleteProfileRepository, userQueryPort
    )

    @Test
    fun `registerTeam succeeds for valid request`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val member2Id = UUID.randomUUID()
        val command = RegisterTeamCommand(
            competitionId = competitionId,
            teamName = "Team A",
            scaleCategory = "RXD",
            members = listOf(
                TeamMemberInput(userId, Gender.MALE),
                TeamMemberInput(member2Id, Gender.FEMALE)
            )
        )
        val now = Instant.now()
        val competition = Competition(
            id = competitionId, name = "Test", description = "Desc",
            startAt = now.plusSeconds(3600), endAt = now.plusSeconds(7200),
            registrationStartAt = now.minusSeconds(3600), registrationEndAt = now.plusSeconds(3600),
            visibility = CompetitionVisibility.PUBLIC, bankInfo = BankInfo.empty()
        )

        `when`(competitionRepository.findById(competitionId)).thenReturn(competition)
        `when`(teamMemberRepository.isUserAlreadyInAnyTeam(competitionId, userId)).thenReturn(false)
        `when`(teamMemberRepository.isUserAlreadyInAnyTeam(competitionId, member2Id)).thenReturn(false)
        `when`(registrationRepository.save(anyObject())).thenAnswer { it.arguments[0].let { r -> (r as CompetitionRegistration).copy(id = UUID.randomUUID()) } }

        val result = registrationService.registerTeam(command, userId)

        assertEquals(competitionId, result.competitionId)
        assertEquals(userId, result.userId)
        assertEquals("Team A", result.teamName)
        assertEquals("RXD", result.scaleCategory)
        assertEquals(RegistrationType.TEAM, result.registrationType)
        verify(teamMemberRepository).saveAll(anyObject())
    }

    @Test
    fun `registerTeam fails if registration period is not valid`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = RegisterTeamCommand(
            competitionId = competitionId,
            teamName = "Team A",
            scaleCategory = "RXD",
            members = listOf(TeamMemberInput(userId, Gender.MALE))
        )
        val now = Instant.now()
        val competition = Competition(
            id = competitionId, name = "Test", description = "Desc",
            startAt = now.plusSeconds(3600), endAt = now.plusSeconds(7200),
            registrationStartAt = now.plusSeconds(1800), registrationEndAt = now.plusSeconds(3600),
            visibility = CompetitionVisibility.PUBLIC, bankInfo = BankInfo.empty()
        )

        `when`(competitionRepository.findById(competitionId)).thenReturn(competition)

        assertThrows(BusinessException::class.java) {
            registrationService.registerTeam(command, userId)
        }
    }

    @Test
    fun `registerTeam fails if a member is already in another team`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val member2Id = UUID.randomUUID()
        val command = RegisterTeamCommand(
            competitionId = competitionId,
            teamName = "Team A",
            scaleCategory = "RXD",
            members = listOf(
                TeamMemberInput(userId, Gender.MALE),
                TeamMemberInput(member2Id, Gender.FEMALE)
            )
        )
        val now = Instant.now()
        val competition = Competition(
            id = competitionId, name = "Test", description = "Desc",
            startAt = now.plusSeconds(3600), endAt = now.plusSeconds(7200),
            registrationStartAt = now.minusSeconds(3600), registrationEndAt = now.plusSeconds(3600),
            visibility = CompetitionVisibility.PUBLIC, bankInfo = BankInfo.empty()
        )

        `when`(competitionRepository.findById(competitionId)).thenReturn(competition)
        `when`(teamMemberRepository.isUserAlreadyInAnyTeam(competitionId, userId)).thenReturn(false)
        `when`(teamMemberRepository.isUserAlreadyInAnyTeam(competitionId, member2Id)).thenReturn(true)

        assertThrows(BusinessException::class.java) {
            registrationService.registerTeam(command, userId)
        }
    }

    @Test
    fun `registerTeam fails if leader is not included in members`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val member2Id = UUID.randomUUID()
        val command = RegisterTeamCommand(
            competitionId = competitionId,
            teamName = "Team A",
            scaleCategory = "RXD",
            members = listOf(
                TeamMemberInput(member2Id, Gender.FEMALE)
            )
        )
        val now = Instant.now()
        val competition = Competition(
            id = competitionId, name = "Test", description = "Desc",
            startAt = now.plusSeconds(3600), endAt = now.plusSeconds(7200),
            registrationStartAt = now.minusSeconds(3600), registrationEndAt = now.plusSeconds(3600),
            visibility = CompetitionVisibility.PUBLIC, bankInfo = BankInfo.empty()
        )

        `when`(competitionRepository.findById(competitionId)).thenReturn(competition)

        assertThrows(BusinessException::class.java) {
            registrationService.registerTeam(command, userId)
        }
    }

    private fun <T> anyObject(): T = ArgumentMatchers.any()
}
