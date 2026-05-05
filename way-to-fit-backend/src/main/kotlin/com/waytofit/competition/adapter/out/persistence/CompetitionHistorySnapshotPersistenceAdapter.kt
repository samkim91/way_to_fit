package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionHistorySnapshotEntity
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionHistorySnapshotJpaRepository
import com.waytofit.competition.application.port.out.CompetitionHistorySnapshotRepository
import com.waytofit.competition.domain.CompetitionHistorySnapshot
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CompetitionHistorySnapshotPersistenceAdapter(
    private val jpaRepository: CompetitionHistorySnapshotJpaRepository
) : CompetitionHistorySnapshotRepository {

    override fun saveAll(snapshots: List<CompetitionHistorySnapshot>) {
        jpaRepository.saveAll(snapshots.map { CompetitionHistorySnapshotEntity.fromDomain(it) })
    }

    override fun findAllByUserId(userId: UUID): List<CompetitionHistorySnapshot> {
        return jpaRepository.findAllByUserId(userId).map { it.toDomain() }
    }

    override fun existsByCompetitionId(competitionId: UUID): Boolean {
        return jpaRepository.existsByCompetitionId(competitionId)
    }
}
