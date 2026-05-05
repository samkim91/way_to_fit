package com.waytofit.competition.domain

import com.waytofit.competition.domain.enums.TeamRole
import com.waytofit.user.domain.enums.Gender
import java.util.UUID

data class CompetitionTeamMember(
    val id: UUID? = null,
    val registrationId: UUID,
    val userId: UUID,
    val gender: Gender,
    val teamRole: TeamRole,
)
