package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.CompetitionTeamMember
import java.util.UUID

interface CompetitionTeamMemberRepository {
    fun saveAll(members: List<CompetitionTeamMember>): List<CompetitionTeamMember>
    fun findByRegistrationId(registrationId: UUID): List<CompetitionTeamMember>
    fun isUserAlreadyInAnyTeam(competitionId: UUID, userId: UUID): Boolean
    fun findAllByUserId(userId: UUID): List<CompetitionTeamMember>
}
