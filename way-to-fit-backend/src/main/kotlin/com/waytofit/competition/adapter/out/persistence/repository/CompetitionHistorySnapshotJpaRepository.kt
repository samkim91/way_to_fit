package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionHistorySnapshotEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CompetitionHistorySnapshotJpaRepository : JpaRepository<CompetitionHistorySnapshotEntity, UUID> {
    fun findAllByUserId(userId: UUID): List<CompetitionHistorySnapshotEntity>
    fun existsByCompetitionId(competitionId: UUID): Boolean
}
