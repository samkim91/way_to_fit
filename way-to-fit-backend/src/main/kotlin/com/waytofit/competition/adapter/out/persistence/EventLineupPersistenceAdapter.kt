package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.EventLineupEntity
import com.waytofit.competition.adapter.out.persistence.repository.EventLineupJpaRepository
import com.waytofit.competition.application.port.out.EventLineupRepository
import com.waytofit.competition.domain.EventLineup
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EventLineupPersistenceAdapter(
    private val jpaRepository: EventLineupJpaRepository,
) : EventLineupRepository {

    override fun save(lineup: EventLineup): EventLineup {
        return jpaRepository.save(EventLineupEntity.fromDomain(lineup)).toDomain()
    }

    override fun findByEventIdAndRegistrationId(eventId: UUID, registrationId: UUID): EventLineup? {
        return jpaRepository.findByEventIdAndRegistrationId(eventId, registrationId)?.toDomain()
    }
}
