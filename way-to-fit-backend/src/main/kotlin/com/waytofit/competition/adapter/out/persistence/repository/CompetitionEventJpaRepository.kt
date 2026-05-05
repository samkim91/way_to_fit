package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface CompetitionEventJpaRepository : JpaRepository<CompetitionEventEntity, UUID> {
    fun findAllByStageIdOrderByOrderAsc(stageId: UUID): List<CompetitionEventEntity>

    @Query("SELECT e FROM CompetitionEventEntity e WHERE e.stageId = :stageId AND (e.releaseAt IS NULL OR e.releaseAt <= :now) ORDER BY e.order ASC")
    fun findPublishedByStageIdOrderByOrderAsc(stageId: UUID, now: Instant): List<CompetitionEventEntity>
}
