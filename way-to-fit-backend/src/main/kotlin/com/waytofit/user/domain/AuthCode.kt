package com.waytofit.user.domain

import java.time.Instant
import java.util.UUID

data class AuthCode(
    val code: String,
    val userId: UUID,
    val expiresAt: Instant,
) {
    companion object {
        fun create(userId: UUID, expirationMinutes: Long = 3): AuthCode {
            return AuthCode(
                code = UUID.randomUUID().toString(),
                userId = userId,
                expiresAt = Instant.now().plusSeconds(expirationMinutes * 60)
            )
        }
    }

    fun isExpired(now: Instant): Boolean {
        return now.isAfter(expiresAt)
    }
}
