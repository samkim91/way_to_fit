package com.waytofit.global.security.oauth2

data class OAuthUserInfo(
    val oauthId: String,
    val email: String?,
    val name: String,
)
