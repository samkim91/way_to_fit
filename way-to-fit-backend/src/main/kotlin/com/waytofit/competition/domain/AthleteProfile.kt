package com.waytofit.competition.domain

import java.util.UUID

data class AthleteProfile(
    val id: UUID? = null,
    val userId: UUID,
    val boxId: UUID? = null,
    val biography: String? = null,
    val profileImageUrl: String? = null,
)
