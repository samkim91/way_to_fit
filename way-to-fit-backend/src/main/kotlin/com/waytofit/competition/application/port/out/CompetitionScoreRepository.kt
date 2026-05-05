package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.CompetitionScore
import com.waytofit.competition.domain.enums.ScoreStatus
import java.util.UUID

interface CompetitionScoreRepository {
    fun save(score: CompetitionScore): CompetitionScore
    fun findById(id: UUID): CompetitionScore?
    fun findByEventIdAndRegistrationId(eventId: UUID, registrationId: UUID): CompetitionScore?
    fun existsByEventId(eventId: UUID): Boolean
    fun findScoresByEventId(eventId: UUID, status: ScoreStatus?): List<CompetitionScore>
    fun findScoresWithDetailsByEventId(eventId: UUID, status: ScoreStatus?): List<CompetitionScoreDetails>
}
