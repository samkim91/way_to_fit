package com.waytofit.user.application.port.`in`

import com.waytofit.user.domain.User
import com.waytofit.user.domain.enums.OAuthProvider

interface UserCommandUseCase {
    fun findOrCreate(
        oauthProvider: OAuthProvider,
        oauthId: String,
        email: String?,
        name: String,
    ): User
}
