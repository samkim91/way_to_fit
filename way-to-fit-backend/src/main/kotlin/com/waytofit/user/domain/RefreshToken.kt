package com.waytofit.user.domain

import com.waytofit.global.domain.AuditInfo
import java.time.Instant
import java.util.UUID

data class RefreshToken(
    val id: Long?,
    val userId: UUID,
    val token: String,
    val deviceInfo: String?,
    val expiresAt: Instant,
    val audit: AuditInfo = AuditInfo.empty(),
) {
    companion object {
        fun create(
            userId: UUID,
            token: String,
            deviceInfo: String?,
            expiresAt: Instant,
        ) = RefreshToken(
            id = null,
            userId = userId,
            token = token,
            deviceInfo = deviceInfo,
            expiresAt = expiresAt,
        )
    }

    fun isExpired(now: Instant): Boolean = expiresAt.isBefore(now)
}
