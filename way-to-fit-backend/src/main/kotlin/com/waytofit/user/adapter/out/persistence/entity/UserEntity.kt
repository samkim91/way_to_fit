package com.waytofit.user.adapter.out.persistence.entity

import com.waytofit.global.domain.AuditInfo
import com.waytofit.global.persistence.BaseEntity
import com.waytofit.user.domain.User
import com.waytofit.user.domain.enums.Gender
import com.waytofit.user.domain.enums.OAuthProvider
import com.waytofit.user.domain.enums.UserRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.SQLRestriction
import java.util.UUID

@Entity
@Table(
    name = "users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["oauth_provider", "oauth_id"])],
)
@SQLRestriction("deleted_at IS NULL")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    val oauthProvider: OAuthProvider,

    @Column(name = "oauth_id", nullable = false)
    val oauthId: String,

    @Column(name = "email")
    val email: String?,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "phone")
    val phone: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    val gender: Gender? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: UserRole,
) : BaseEntity() {
    fun toDomain(): User = User(
        id = id,
        oauthProvider = oauthProvider,
        oauthId = oauthId,
        email = email,
        name = name,
        phone = phone,
        gender = gender,
        role = role,
        audit = AuditInfo(
            createdAt = createdAt,
            createdBy = createdBy,
            lastModifiedAt = lastModifiedAt,
            lastModifiedBy = lastModifiedBy,
        ),
    )

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity(
            id = user.id,
            oauthProvider = user.oauthProvider,
            oauthId = user.oauthId,
            email = user.email,
            name = user.name,
            phone = user.phone,
            gender = user.gender,
            role = user.role,
        )
    }
}
