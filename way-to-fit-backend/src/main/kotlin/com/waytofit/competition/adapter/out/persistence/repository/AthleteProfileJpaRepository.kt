package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.AthleteProfileEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AthleteProfileJpaRepository : JpaRepository<AthleteProfileEntity, UUID> {
    fun findByUserId(userId: UUID): AthleteProfileEntity?
    fun existsByUserId(userId: UUID): Boolean
    fun findAllByUserIdIn(userIds: List<UUID>): List<AthleteProfileEntity>
}
