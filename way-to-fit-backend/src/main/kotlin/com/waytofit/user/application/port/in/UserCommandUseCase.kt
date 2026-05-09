package com.waytofit.user.application.port.`in`

import com.waytofit.user.domain.User
import com.waytofit.user.domain.enums.OAuthProvider
import com.waytofit.user.domain.enums.UserRole
import java.util.UUID

interface UserCommandUseCase {
    fun findOrCreate(
        oauthProvider: OAuthProvider,
        oauthId: String,
        email: String?,
        name: String,
    ): User

    fun updateRole(userId: UUID, role: UserRole): User
}
