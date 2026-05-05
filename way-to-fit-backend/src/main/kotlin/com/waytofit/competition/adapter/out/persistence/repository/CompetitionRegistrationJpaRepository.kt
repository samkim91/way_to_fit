package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionRegistrationEntity
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.competition.domain.enums.RegistrationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface CompetitionRegistrationJpaRepository : JpaRepository<CompetitionRegistrationEntity, UUID> {
    fun findByCompetitionIdAndUserId(competitionId: UUID, userId: UUID): CompetitionRegistrationEntity?
    fun existsByCompetitionIdAndUserIdAndRegistrationType(
        competitionId: UUID,
        userId: UUID,
        registrationType: RegistrationType
    ): Boolean
    fun findAllByCompetitionIdAndPaymentStatus(competitionId: UUID, paymentStatus: PaymentStatus, pageable: Pageable): Page<CompetitionRegistrationEntity>
    fun findAllByCompetitionId(competitionId: UUID, pageable: Pageable): Page<CompetitionRegistrationEntity>
    fun findAllByUserId(userId: UUID): List<CompetitionRegistrationEntity>

    @Query("""
        SELECT r FROM CompetitionRegistrationEntity r 
        WHERE r.competitionId = :competitionId 
        AND EXISTS (
            SELECT 1 FROM CompetitionTeamMemberEntity m 
            WHERE m.registrationId = r.id AND m.userId = :userId
        )
    """)
    fun findAllByCompetitionIdAndMemberUserId(competitionId: UUID, userId: UUID): List<CompetitionRegistrationEntity>
}
