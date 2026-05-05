package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.CompetitionHistorySnapshot
import java.util.UUID

interface CompetitionHistorySnapshotRepository {
    fun saveAll(snapshots: List<CompetitionHistorySnapshot>)
    fun findAllByUserId(userId: UUID): List<CompetitionHistorySnapshot>
    fun existsByCompetitionId(competitionId: UUID): Boolean
}
