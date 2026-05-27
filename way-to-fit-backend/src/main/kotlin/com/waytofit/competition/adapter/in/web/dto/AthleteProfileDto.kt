package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.domain.AthleteProfile
import java.util.UUID

data class AthleteProfileResponse(
    val id: UUID?,
    val userId: UUID,
    val biography: String?,
    val profileImageUrl: String?,
) {
    companion object {
        fun fromDomain(domain: AthleteProfile) = AthleteProfileResponse(
            id = domain.id,
            userId = domain.userId,
            biography = domain.biography,
            profileImageUrl = domain.profileImageUrl
        )
    }
}

data class AthleteSearchResponse(
    val userId: UUID,
    val name: String,
    val gender: com.waytofit.user.domain.enums.Gender?,
    val profileImageUrl: String?,
)

data class UpdateAthleteProfileRequest(
    val biography: String?,
    val profileImageUrl: String?,
) {
    fun toCommand() = com.waytofit.competition.application.port.`in`.UpdateAthleteProfileCommand(
        biography = biography,
        profileImageUrl = profileImageUrl
    )
}
