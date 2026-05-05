package com.waytofit.user.domain

import com.waytofit.global.domain.AuditInfo
import com.waytofit.user.domain.enums.Gender
import com.waytofit.user.domain.enums.OAuthProvider
import com.waytofit.user.domain.enums.UserRole
import java.util.UUID

data class User(
    val id: UUID?,
    val oauthProvider: OAuthProvider,
    val oauthId: String,
    val email: String?,
    val name: String,
    val phone: String?,
    val gender: Gender? = null,
    val role: UserRole,
    val audit: AuditInfo = AuditInfo.empty(),
) {
    companion object {
        fun create(
            oauthProvider: OAuthProvider,
            oauthId: String,
            email: String?,
            name: String,
        ) = User(
            id = null,
            oauthProvider = oauthProvider,
            oauthId = oauthId,
            email = email,
            name = name,
            phone = null,
            gender = null,
            role = UserRole.USER,
        )
    }
}
