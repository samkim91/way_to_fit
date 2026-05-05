package com.waytofit.global.security.oauth2

import com.waytofit.user.domain.User
import org.springframework.security.oauth2.core.user.OAuth2User

class AuthenticatedOAuth2User(
    private val delegate: OAuth2User,
    val user: User,
) : OAuth2User by delegate {
    override fun getName(): String = user.id.toString()
}
