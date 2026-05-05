package com.waytofit.user.application.port.out

import com.waytofit.user.domain.RefreshToken
import java.time.Instant
import java.util.UUID

interface RefreshTokenPersistencePort {
    fun save(refreshToken: RefreshToken): RefreshToken
    fun findByToken(token: String): RefreshToken?
    fun deleteByToken(token: String)
    fun deleteByUserId(userId: UUID)
    fun deleteExpiredByUserId(userId: UUID, now: Instant)
}
