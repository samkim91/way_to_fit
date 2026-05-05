package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.EventLineup
import java.util.UUID

interface EventLineupRepository {
    fun save(lineup: EventLineup): EventLineup
    fun findByEventIdAndRegistrationId(eventId: UUID, registrationId: UUID): EventLineup?
}
