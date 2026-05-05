package com.waytofit.global.security.jwt

import com.waytofit.user.domain.enums.UserRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @param:Value($$"${jwt.secret}") private val secretKey: String,
    @param:Value($$"${jwt.access-token-expiration}") private val accessTokenExpiration: Long,
    @param:Value($$"${jwt.refresh-token-expiration}") private val refreshTokenExpiration: Long,
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun generateAccessToken(
        userId: String,
        email: String?,
        name: String,
        role: UserRole,
    ): String {
        val extraClaims = mapOf(
            "email" to email,
            "name" to name,
            "role" to role.name,
        )
        return buildToken(userId, extraClaims, accessTokenExpiration)
    }

    fun generateRefreshToken(userId: String): String =
        buildToken(userId, emptyMap(), refreshTokenExpiration)

    fun validateToken(token: String): Boolean =
        runCatching { getClaims(token) }.isSuccess

    fun getSubject(token: String): String =
        getClaims(token).subject

    fun getRole(token: String): String? =
        getClaims(token).get("role", String::class.java)

    fun getExpirationAsInstant(token: String): Instant =
        getClaims(token).expiration.toInstant()

    private fun buildToken(subject: String, extraClaims: Map<String, Any?>, expiration: Long): String {
        val now = Date()
        return Jwts.builder()
            .subject(subject)
            .claims(extraClaims.filterValues { it != null })
            .issuedAt(now)
            .expiration(Date(now.time + expiration))
            .signWith(key)
            .compact()
    }

    fun getClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
