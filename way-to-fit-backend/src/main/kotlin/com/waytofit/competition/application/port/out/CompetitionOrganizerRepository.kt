package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.CompetitionOrganizer
import java.util.UUID

interface CompetitionOrganizerRepository {
    fun isOrganizer(competitionId: UUID, userId: UUID): Boolean
    fun saveOrganizer(competitionId: UUID, userId: UUID): CompetitionOrganizer
    fun findCompetitionIdsByUserId(userId: UUID): List<UUID>
}
