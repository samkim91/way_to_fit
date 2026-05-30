package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.application.port.out.UserQueryPort
import com.waytofit.user.adapter.out.persistence.repository.UserJpaRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserQueryPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository,
) : UserQueryPort {

    override fun findNamesByIds(ids: Collection<UUID>): Map<UUID, String> {
        if (ids.isEmpty()) return emptyMap()
        return userJpaRepository.findAllById(ids)
            .mapNotNull { entity -> entity.id?.let { it to entity.name } }
            .toMap()
    }
}
