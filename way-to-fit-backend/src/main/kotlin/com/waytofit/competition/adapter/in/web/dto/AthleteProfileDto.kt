package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.domain.AthleteProfile
import java.util.UUID

data class AthleteProfileResponse(
    val id: UUID?,
    val userId: UUID,
    val boxId: UUID?,
    val biography: String?,
    val profileImageUrl: String?,
) {
    companion object {
        fun fromDomain(domain: AthleteProfile) = AthleteProfileResponse(
            id = domain.id,
            userId = domain.userId,
            boxId = domain.boxId,
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
    val boxId: UUID?,
    val boxName: String? = null,
)

data class UpdateAthleteProfileRequest(
    val boxId: UUID?,
    val biography: String?,
    val profileImageUrl: String?,
) {
    fun toCommand() = com.waytofit.competition.application.port.`in`.UpdateAthleteProfileCommand(
        boxId = boxId,
        biography = biography,
        profileImageUrl = profileImageUrl
    )
}
