package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionScoreEntity
import com.waytofit.competition.domain.enums.ScoreStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CompetitionScoreJpaRepository : JpaRepository<CompetitionScoreEntity, UUID>, CompetitionScoreCustomRepository {
    fun findByEventIdAndRegistrationId(eventId: UUID, registrationId: UUID): CompetitionScoreEntity?
    fun existsByEventId(eventId: UUID): Boolean
    fun findAllByEventId(eventId: UUID): List<CompetitionScoreEntity>
    fun findAllByEventIdAndStatus(eventId: UUID, status: ScoreStatus): List<CompetitionScoreEntity>
}
