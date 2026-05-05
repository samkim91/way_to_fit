package com.waytofit.user.application.port.`in`

import com.waytofit.user.domain.User

interface AuthCommandUseCase {
    fun generateAuthCode(user: User): String
    fun exchangeToken(code: String, deviceInfo: String?): Pair<String, String>
    fun login(user: User, deviceInfo: String?): Pair<String, String>
    fun reissue(refreshToken: String, deviceInfo: String?): Pair<String, String>
    fun logout(refreshToken: String)
}
