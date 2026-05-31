package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.domain.AthleteProfile
import com.waytofit.user.domain.enums.Gender
import java.util.UUID

data class AthleteProfileResponse(
    val id: UUID?,
    val userId: UUID,
    val name: String,
    val biography: String?,
    val profileImageUrl: String?,
    val gender: Gender?,
) {
    companion object {
        fun fromDomain(domain: AthleteProfile) = AthleteProfileResponse(
            id = domain.id,
            userId = domain.userId,
            name = domain.name,
            biography = domain.biography,
            profileImageUrl = domain.profileImageUrl,
            gender = domain.gender,
        )
    }
}

data class AthleteSearchResponse(
    val userId: UUID,
    val name: String,
    val gender: Gender?,
    val profileImageUrl: String?,
)

data class UpdateAthleteProfileRequest(
    val biography: String?,
    val profileImageUrl: String?,
    val gender: Gender? = null,
) {
    fun toCommand() = com.waytofit.competition.application.port.`in`.UpdateAthleteProfileCommand(
        biography = biography,
        profileImageUrl = profileImageUrl,
        gender = gender,
    )
}
