package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.CompetitionScore
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.user.domain.enums.Gender
import java.util.UUID

data class CompetitionScoreDetails(
    val score: CompetitionScore,
    val registrationId: UUID,
    val registrationType: RegistrationType,
    val participantName: String,
    val scaleCategory: String,
    val gender: Gender,
    val memberUserIds: List<UUID>,
    val manualRank: Int? = null
)
