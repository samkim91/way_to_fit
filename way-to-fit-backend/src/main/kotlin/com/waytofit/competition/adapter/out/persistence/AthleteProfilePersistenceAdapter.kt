package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.AthleteProfileEntity
import com.waytofit.competition.adapter.out.persistence.repository.AthleteProfileJpaRepository
import com.waytofit.competition.application.port.out.AthleteProfileRepository
import com.waytofit.competition.domain.AthleteProfile
import com.waytofit.competition.domain.AthleteSearchResult
import com.waytofit.user.application.port.out.UserPersistencePort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AthleteProfilePersistenceAdapter(
    private val athleteProfileJpaRepository: AthleteProfileJpaRepository,
    private val userPersistencePort: UserPersistencePort,
) : AthleteProfileRepository {

    override fun findByUserId(userId: UUID): AthleteProfile? {
        return athleteProfileJpaRepository.findByUserId(userId)?.toDomain()
    }

    override fun save(athleteProfile: AthleteProfile): AthleteProfile {
        val entity = AthleteProfileEntity.fromDomain(athleteProfile)
        return athleteProfileJpaRepository.save(entity).toDomain()
    }

    override fun existsByUserId(userId: UUID): Boolean {
        return athleteProfileJpaRepository.existsByUserId(userId)
    }

    override fun searchAthletesByName(name: String): List<AthleteSearchResult> {
        val users = userPersistencePort.findAllByNameContaining(name)
        if (users.isEmpty()) return emptyList()

        val userIds = users.mapNotNull { it.id }
        val profiles = athleteProfileJpaRepository.findAllByUserIdIn(userIds)
        val profileMap = profiles.associateBy { it.userId }

        return users.mapNotNull { user ->
            val profile = profileMap[user.id] ?: return@mapNotNull null
            AthleteSearchResult(
                userId = user.id!!,
                name = user.name,
                gender = user.gender,
                profileImageUrl = profile.profileImageUrl,
                boxId = profile.boxId
            )
        }
    }
}
