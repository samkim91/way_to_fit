package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.CompetitionEvent
import java.util.UUID

interface CompetitionEventRepository {
    fun save(event: CompetitionEvent): CompetitionEvent
    fun findById(id: UUID): CompetitionEvent?
    fun findAllByStageId(stageId: UUID): List<CompetitionEvent>
    fun findPublishedByStageId(stageId: UUID): List<CompetitionEvent>
    fun delete(id: UUID)
}
