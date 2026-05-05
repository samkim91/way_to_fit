package com.waytofit.user.adapter.out.persistence

import com.waytofit.user.adapter.out.persistence.entity.RefreshTokenEntity
import com.waytofit.user.adapter.out.persistence.repository.RefreshTokenJpaRepository
import com.waytofit.user.application.port.out.RefreshTokenPersistencePort
import com.waytofit.user.domain.RefreshToken
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class RefreshTokenPersistenceAdapter(
    private val refreshTokenJpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenPersistencePort {

    override fun save(refreshToken: RefreshToken): RefreshToken =
        refreshTokenJpaRepository.save(RefreshTokenEntity.fromDomain(refreshToken)).toDomain()

    override fun findByToken(token: String): RefreshToken? =
        refreshTokenJpaRepository.findByToken(token)?.toDomain()

    override fun deleteByToken(token: String) =
        refreshTokenJpaRepository.deleteByToken(token)

    override fun deleteByUserId(userId: UUID) =
        refreshTokenJpaRepository.deleteByUserId(userId)

    override fun deleteExpiredByUserId(userId: UUID, now: Instant) =
        refreshTokenJpaRepository.deleteByUserIdAndExpiresAtLessThan(userId, now)
}
