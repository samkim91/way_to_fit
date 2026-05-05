package com.waytofit.user.adapter.out.persistence

import com.waytofit.user.adapter.out.persistence.entity.UserEntity
import com.waytofit.user.adapter.out.persistence.repository.UserJpaRepository
import com.waytofit.user.application.port.out.UserPersistencePort
import com.waytofit.user.domain.User
import com.waytofit.user.domain.enums.OAuthProvider
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository,
) : UserPersistencePort {

    override fun findByOauthProviderAndOauthId(provider: OAuthProvider, oauthId: String): User? =
        userJpaRepository.findByOauthProviderAndOauthId(provider, oauthId)?.toDomain()

    override fun findById(id: UUID): User? =
        userJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(user: User): User =
        userJpaRepository.save(UserEntity.fromDomain(user)).toDomain()

    override fun findAllByNameContaining(name: String): List<User> =
        userJpaRepository.findAllByNameContaining(name).map { it.toDomain() }
}
