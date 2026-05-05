package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionTeamMemberEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface CompetitionTeamMemberJpaRepository : JpaRepository<CompetitionTeamMemberEntity, UUID> {
    fun findAllByRegistrationId(registrationId: UUID): List<CompetitionTeamMemberEntity>

    @Query("""
        SELECT COUNT(m) > 0
        FROM CompetitionTeamMemberEntity m
        JOIN CompetitionRegistrationEntity r ON m.registrationId = r.id
        WHERE r.competitionId = :competitionId AND m.userId = :userId
    """)
    fun existsByCompetitionIdAndUserId(competitionId: UUID, userId: UUID): Boolean

    fun findAllByUserId(userId: UUID): List<CompetitionTeamMemberEntity>
}
