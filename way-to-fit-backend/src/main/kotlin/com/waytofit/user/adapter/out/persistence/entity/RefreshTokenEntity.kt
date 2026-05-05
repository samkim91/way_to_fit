package com.waytofit.user.adapter.out.persistence.entity

import com.waytofit.global.domain.AuditInfo
import com.waytofit.global.persistence.BaseEntity
import com.waytofit.user.domain.RefreshToken
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "refresh_tokens",
    indexes = [Index(name = "idx_refresh_tokens_token", columnList = "refresh_token")],
)
class RefreshTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "refresh_token", nullable = false, length = 1024)
    val token: String,

    @Column(name = "device_info", length = 1024)
    val deviceInfo: String?,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,
) : BaseEntity() {
    fun toDomain(): RefreshToken = RefreshToken(
        id = id,
        userId = userId,
        token = token,
        deviceInfo = deviceInfo,
        expiresAt = expiresAt,
        audit = AuditInfo(
            createdAt = createdAt,
            createdBy = createdBy,
            lastModifiedAt = lastModifiedAt,
            lastModifiedBy = lastModifiedBy,
        ),
    )

    companion object {
        fun fromDomain(rt: RefreshToken): RefreshTokenEntity = RefreshTokenEntity(
            id = rt.id,
            userId = rt.userId,
            token = rt.token,
            deviceInfo = rt.deviceInfo,
            expiresAt = rt.expiresAt,
        )
    }
}
