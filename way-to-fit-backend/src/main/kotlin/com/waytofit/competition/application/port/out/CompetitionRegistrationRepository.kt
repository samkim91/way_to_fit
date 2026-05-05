package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.CompetitionRegistration
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.competition.domain.enums.RegistrationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CompetitionRegistrationRepository {
    fun save(registration: CompetitionRegistration): CompetitionRegistration
    fun findById(id: UUID): CompetitionRegistration?
    fun findByCompetitionIdAndUserId(competitionId: UUID, userId: UUID): CompetitionRegistration?
    fun existsByCompetitionIdAndUserIdAndType(
        competitionId: UUID,
        userId: UUID,
        type: RegistrationType
    ): Boolean
    fun findRegistrationsByCompetitionId(competitionId: UUID, paymentStatus: PaymentStatus?, pageable: Pageable): Page<CompetitionRegistration>
    fun findAllByUserId(userId: UUID): List<CompetitionRegistration>
    fun findAllByCompetitionIdAndMemberUserId(competitionId: UUID, userId: UUID): List<CompetitionRegistration>
}
