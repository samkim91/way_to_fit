package com.waytofit.user.adapter.out.persistence.repository

import com.waytofit.user.adapter.out.persistence.entity.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenEntity, Long> {
    fun findByToken(token: String): RefreshTokenEntity?

    @Modifying(clearAutomatically = true)
    fun deleteByToken(token: String)

    @Modifying(clearAutomatically = true)
    fun deleteByUserId(userId: UUID)

    @Modifying(clearAutomatically = true)
    fun deleteByUserIdAndExpiresAtLessThan(userId: UUID, now: Instant)
}
