package com.waytofit.user.adapter.out.persistence.entity

import com.waytofit.global.persistence.BaseEntity
import com.waytofit.user.domain.AuthCode
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "auth_code")
class AuthCodeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val code: String,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val expiresAt: Instant,
) : BaseEntity() {

    fun toDomain(): AuthCode = AuthCode(
        code = code,
        userId = userId,
        expiresAt = expiresAt
    )

    companion object {
        fun fromDomain(domain: AuthCode): AuthCodeEntity = AuthCodeEntity(
            code = domain.code,
            userId = domain.userId,
            expiresAt = domain.expiresAt
        )
    }
}
