package com.waytofit.user.adapter.`in`.web.dto

data class AuthTokenRequest(
    val code: String
)

data class AuthTokenResponse(
    val accessToken: String
)
