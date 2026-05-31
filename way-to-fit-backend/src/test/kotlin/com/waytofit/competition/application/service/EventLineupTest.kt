package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.SetLineupCommand
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.CompetitionTeamMember
import com.waytofit.competition.domain.EventLineup
import com.waytofit.competition.domain.enums.TeamRole
import com.waytofit.global.error.BusinessException
import com.waytofit.user.domain.enums.Gender
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.util.UUID

class EventLineupTest {

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
    fun `setEventLineup succeeds for leader with valid members`() {
        val eventId = UUID.randomUUID()
        val registrationId = UUID.randomUUID()
        val leaderUserId = UUID.randomUUID()
        val memberUserId = UUID.randomUUID()

        val teamMembers = listOf(
            CompetitionTeamMember(id = UUID.randomUUID(), registrationId = registrationId, userId = leaderUserId, gender = Gender.MALE, teamRole = TeamRole.LEADER),
            CompetitionTeamMember(id = UUID.randomUUID(), registrationId = registrationId, userId = memberUserId, gender = Gender.FEMALE, teamRole = TeamRole.MEMBER)
        )
        val command = SetLineupCommand(eventId, registrationId, listOf(leaderUserId, memberUserId))

        `when`(teamMemberRepository.findByRegistrationId(registrationId)).thenReturn(teamMembers)
        `when`(eventLineupRepository.findByEventIdAndRegistrationId(eventId, registrationId)).thenReturn(null)
        `when`(eventLineupRepository.save(anyObject())).thenAnswer { it.arguments[0] }

        val result = registrationService.setEventLineup(command, leaderUserId)

        assertEquals(eventId, result.eventId)
        assertEquals(registrationId, result.registrationId)
        assertEquals(2, result.participatingMemberIds.size)
        assertTrue(result.participatingMemberIds.containsAll(listOf(leaderUserId, memberUserId)))
    }

    @Test
    fun `setEventLineup fails if registration team members empty`() {
        val eventId = UUID.randomUUID()
        val registrationId = UUID.randomUUID()
        val leaderUserId = UUID.randomUUID()
        val command = SetLineupCommand(eventId, registrationId, listOf(leaderUserId))

        `when`(teamMemberRepository.findByRegistrationId(registrationId)).thenReturn(emptyList())

        assertThrows(BusinessException::class.java) {
            registrationService.setEventLineup(command, leaderUserId)
        }
    }

    @Test
    fun `setEventLineup fails if requester is not team leader`() {
        val eventId = UUID.randomUUID()
        val registrationId = UUID.randomUUID()
        val leaderUserId = UUID.randomUUID()
        val memberUserId = UUID.randomUUID()

        val teamMembers = listOf(
            CompetitionTeamMember(id = UUID.randomUUID(), registrationId = registrationId, userId = leaderUserId, gender = Gender.MALE, teamRole = TeamRole.LEADER),
            CompetitionTeamMember(id = UUID.randomUUID(), registrationId = registrationId, userId = memberUserId, gender = Gender.FEMALE, teamRole = TeamRole.MEMBER)
        )
        val command = SetLineupCommand(eventId, registrationId, listOf(memberUserId))

        `when`(teamMemberRepository.findByRegistrationId(registrationId)).thenReturn(teamMembers)

        assertThrows(BusinessException::class.java) {
            registrationService.setEventLineup(command, memberUserId)
        }
    }

    @Test
    fun `setEventLineup fails if lineup contains non-team member`() {
        val eventId = UUID.randomUUID()
        val registrationId = UUID.randomUUID()
        val leaderUserId = UUID.randomUUID()
        val otherUserId = UUID.randomUUID()

        val teamMembers = listOf(
            CompetitionTeamMember(id = UUID.randomUUID(), registrationId = registrationId, userId = leaderUserId, gender = Gender.MALE, teamRole = TeamRole.LEADER)
        )
        val command = SetLineupCommand(eventId, registrationId, listOf(leaderUserId, otherUserId))

        `when`(teamMemberRepository.findByRegistrationId(registrationId)).thenReturn(teamMembers)

        assertThrows(BusinessException::class.java) {
            registrationService.setEventLineup(command, leaderUserId)
        }
    }

    @Test
    fun `setEventLineup overwrites existing lineup`() {
        val eventId = UUID.randomUUID()
        val registrationId = UUID.randomUUID()
        val leaderUserId = UUID.randomUUID()
        val memberUserId = UUID.randomUUID()

        val teamMembers = listOf(
            CompetitionTeamMember(id = UUID.randomUUID(), registrationId = registrationId, userId = leaderUserId, gender = Gender.MALE, teamRole = TeamRole.LEADER),
            CompetitionTeamMember(id = UUID.randomUUID(), registrationId = registrationId, userId = memberUserId, gender = Gender.FEMALE, teamRole = TeamRole.MEMBER)
        )
        val command = SetLineupCommand(eventId, registrationId, listOf(leaderUserId))
        val existingLineup = EventLineup(
            eventId = eventId,
            registrationId = registrationId,
            participatingMemberIds = listOf(leaderUserId, memberUserId)
        )

        `when`(teamMemberRepository.findByRegistrationId(registrationId)).thenReturn(teamMembers)
        `when`(eventLineupRepository.findByEventIdAndRegistrationId(eventId, registrationId)).thenReturn(existingLineup)
        `when`(eventLineupRepository.save(anyObject())).thenAnswer { it.arguments[0] }

        val result = registrationService.setEventLineup(command, leaderUserId)

        assertEquals(eventId, result.eventId)
        assertEquals(registrationId, result.registrationId)
        assertEquals(1, result.participatingMemberIds.size)
        assertEquals(leaderUserId, result.participatingMemberIds.first())
    }

    @Test
    fun `getEventLineup returns lineup if exists`() {
        val eventId = UUID.randomUUID()
        val registrationId = UUID.randomUUID()
        val lineup = EventLineup(
            eventId = eventId,
            registrationId = registrationId,
            participatingMemberIds = listOf(UUID.randomUUID())
        )

        `when`(eventLineupRepository.findByEventIdAndRegistrationId(eventId, registrationId)).thenReturn(lineup)

        val result = registrationService.getEventLineup(eventId, registrationId)

        assertNotNull(result)
        assertEquals(eventId, result?.eventId)
        assertEquals(registrationId, result?.registrationId)
    }

    private fun <T> anyObject(): T = ArgumentMatchers.any()
}
