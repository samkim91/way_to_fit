package com.waytofit.user.adapter.out.persistence.repository

import com.waytofit.user.adapter.out.persistence.entity.UserEntity
import com.waytofit.user.domain.enums.OAuthProvider
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByOauthProviderAndOauthId(provider: OAuthProvider, oauthId: String): UserEntity?
    fun findAllByNameContaining(name: String): List<UserEntity>
}
