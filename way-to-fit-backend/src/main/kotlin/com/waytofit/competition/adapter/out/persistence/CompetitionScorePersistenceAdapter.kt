package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionScoreEntity
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionScoreJpaRepository
import com.waytofit.competition.application.port.out.CompetitionScoreDetails
import com.waytofit.competition.application.port.out.CompetitionScoreRepository
import com.waytofit.competition.domain.CompetitionScore
import com.waytofit.competition.domain.enums.ScoreStatus
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CompetitionScorePersistenceAdapter(
    private val jpaRepository: CompetitionScoreJpaRepository,
) : CompetitionScoreRepository {

    override fun save(score: CompetitionScore): CompetitionScore {
        return jpaRepository.save(CompetitionScoreEntity.fromDomain(score)).toDomain()
    }

    override fun findById(id: UUID): CompetitionScore? {
        return jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findByEventIdAndRegistrationId(eventId: UUID, registrationId: UUID): CompetitionScore? {
        return jpaRepository.findByEventIdAndRegistrationId(eventId, registrationId)?.toDomain()
    }

    override fun existsByEventId(eventId: UUID): Boolean {
        return jpaRepository.existsByEventId(eventId)
    }

    override fun findScoresByEventId(eventId: UUID, status: ScoreStatus?): List<CompetitionScore> {
        val entities = if (status != null) {
            jpaRepository.findAllByEventIdAndStatus(eventId, status)
        } else {
            jpaRepository.findAllByEventId(eventId)
        }
        return entities.map { it.toDomain() }
    }

    override fun findScoresWithDetailsByEventId(eventId: UUID, status: ScoreStatus?): List<CompetitionScoreDetails> {
        return jpaRepository.findAllByEventIdWithDetails(eventId, status).map { entity ->
            val registration = entity.registration!!
            CompetitionScoreDetails(
                score = entity.toDomain(),
                registrationId = registration.id!!,
                registrationType = registration.registrationType,
                participantName = registration.teamName ?: registration.user?.name ?: "Unknown",
                scaleCategory = registration.scaleCategory,
                gender = registration.gender,
                memberUserIds = registration.teamMembers.map { it.userId },
                manualRank = registration.manualRank
            )
        }
    }
}
