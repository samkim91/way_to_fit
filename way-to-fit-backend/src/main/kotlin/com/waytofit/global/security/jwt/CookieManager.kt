package com.waytofit.global.security.jwt

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CookieManager(
    @param:Value("\${jwt.refresh-token-expiration}") private val refreshTokenExpiration: Long,
    @param:Value("\${jwt.cookie-secure:true}") private val cookieSecure: Boolean,
) {
    companion object {
        const val REFRESH_TOKEN_NAME = "refresh_token"
        const val AUTH_PATH = "/api/auth"
    }

    private val refreshTokenMaxAge = (refreshTokenExpiration / 1000).toInt()

    fun setRefreshTokenCookie(response: HttpServletResponse, token: String) {
        addCookie(response, REFRESH_TOKEN_NAME, token, AUTH_PATH, refreshTokenMaxAge)
    }

    fun clearRefreshTokenCookie(response: HttpServletResponse) {
        addCookie(response, REFRESH_TOKEN_NAME, "", AUTH_PATH, 0)
    }

    private fun addCookie(
        response: HttpServletResponse,
        name: String,
        value: String,
        path: String,
        maxAge: Int
    ) {
        val cookie = Cookie(name, value).apply {
            isHttpOnly = true
            secure = cookieSecure
            this.path = path
            this.maxAge = maxAge
        }
        response.addCookie(cookie)
    }
}

