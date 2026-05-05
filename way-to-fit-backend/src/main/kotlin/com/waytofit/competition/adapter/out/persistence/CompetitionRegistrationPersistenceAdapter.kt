package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionRegistrationEntity
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionRegistrationJpaRepository
import com.waytofit.competition.application.port.out.CompetitionRegistrationRepository
import com.waytofit.competition.domain.CompetitionRegistration
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.competition.domain.enums.RegistrationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CompetitionRegistrationPersistenceAdapter(
    private val jpaRepository: CompetitionRegistrationJpaRepository,
) : CompetitionRegistrationRepository {

    override fun save(registration: CompetitionRegistration): CompetitionRegistration {
        return jpaRepository.save(CompetitionRegistrationEntity.fromDomain(registration)).toDomain()
    }

    override fun findById(id: UUID): CompetitionRegistration? {
        return jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findByCompetitionIdAndUserId(competitionId: UUID, userId: UUID): CompetitionRegistration? {
        return jpaRepository.findByCompetitionIdAndUserId(competitionId, userId)?.toDomain()
    }

    override fun existsByCompetitionIdAndUserIdAndType(
        competitionId: UUID,
        userId: UUID,
        type: RegistrationType
    ): Boolean {
        return jpaRepository.existsByCompetitionIdAndUserIdAndRegistrationType(competitionId, userId, type)
    }

    override fun findRegistrationsByCompetitionId(
        competitionId: UUID,
        paymentStatus: PaymentStatus?,
        pageable: Pageable
    ): Page<CompetitionRegistration> {
        val entities = if (paymentStatus != null) {
            jpaRepository.findAllByCompetitionIdAndPaymentStatus(competitionId, paymentStatus, pageable)
        } else {
            jpaRepository.findAllByCompetitionId(competitionId, pageable)
        }
        return entities.map { it.toDomain() }
    }

    override fun findAllByUserId(userId: UUID): List<CompetitionRegistration> {
        return jpaRepository.findAllByUserId(userId).map { it.toDomain() }
    }

    override fun findAllByCompetitionIdAndMemberUserId(competitionId: UUID, userId: UUID): List<CompetitionRegistration> {
        return jpaRepository.findAllByCompetitionIdAndMemberUserId(competitionId, userId).map { it.toDomain() }
    }
}
