package com.waytofit.competition.application.port.`in`

import com.waytofit.competition.domain.AthleteProfile
import java.util.UUID

interface GetAthleteProfileUseCase {
    fun getAthleteProfile(userId: UUID): AthleteProfile
}

interface UpdateAthleteProfileUseCase {
    fun updateAthleteProfile(userId: UUID, command: UpdateAthleteProfileCommand): AthleteProfile
}

interface CreateAthleteProfileUseCase {
    fun createProfileIfNotExists(userId: UUID)
}

data class UpdateAthleteProfileCommand(
    val biography: String?,
    val profileImageUrl: String?,
)
