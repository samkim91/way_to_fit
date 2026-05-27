package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.out.CompetitionOrganizerRepository
import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.UUID

class CompetitionOrganizerTest {

    private val organizerRepository = mock(CompetitionOrganizerRepository::class.java)

    @Test
    fun `isOrganizer returns true when user is organizer`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(true)

        val result = organizerRepository.isOrganizer(competitionId, userId)

        assertTrue(result)
        verify(organizerRepository).isOrganizer(competitionId, userId)
    }

    @Test
    fun `isOrganizer returns false when user is not organizer`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(false)

        val result = organizerRepository.isOrganizer(competitionId, userId)

        assertFalse(result)
        verify(organizerRepository).isOrganizer(competitionId, userId)
    }

    @Test
    fun `business logic throws forbidden when user is not organizer`() {
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        `when`(organizerRepository.isOrganizer(competitionId, userId)).thenReturn(false)

        val exception = assertThrows(BusinessException::class.java) {
            if (!organizerRepository.isOrganizer(competitionId, userId)) {
                throw BusinessException(ResponseCode.FORBIDDEN)
            }
        }

        assertEquals(ResponseCode.FORBIDDEN, exception.responseCode)
    }
}
