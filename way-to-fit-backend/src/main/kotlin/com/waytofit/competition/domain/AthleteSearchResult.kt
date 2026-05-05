package com.waytofit.competition.domain

import com.waytofit.user.domain.enums.Gender
import java.util.UUID

data class AthleteSearchResult(
    val userId: UUID,
    val name: String,
    val gender: Gender?,
    val profileImageUrl: String?,
    val boxId: UUID?,
    val boxName: String? = null,
)
