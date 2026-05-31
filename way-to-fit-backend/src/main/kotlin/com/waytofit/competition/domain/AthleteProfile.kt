package com.waytofit.competition.domain

import com.waytofit.user.domain.enums.Gender
import java.util.UUID

data class AthleteProfile(
    val id: UUID? = null,
    val userId: UUID,
    val name: String = "",
    val biography: String? = null,
    val profileImageUrl: String? = null,
    val gender: Gender? = null,
)
