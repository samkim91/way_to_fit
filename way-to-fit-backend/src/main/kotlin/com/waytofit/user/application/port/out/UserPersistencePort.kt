package com.waytofit.user.application.port.out

import com.waytofit.user.domain.User
import com.waytofit.user.domain.enums.OAuthProvider
import java.util.UUID

interface UserPersistencePort {
    fun findByOauthProviderAndOauthId(provider: OAuthProvider, oauthId: String): User?
    fun findById(id: UUID): User?
    fun save(user: User): User
    fun findAllByNameContaining(name: String): List<User>
}
