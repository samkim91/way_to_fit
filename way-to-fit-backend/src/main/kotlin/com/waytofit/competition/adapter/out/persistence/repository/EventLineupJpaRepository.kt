package com.waytofit.competition.adapter.out.persistence.repository

import com.waytofit.competition.adapter.out.persistence.entity.EventLineupEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EventLineupJpaRepository : JpaRepository<EventLineupEntity, UUID> {
    fun findByEventIdAndRegistrationId(eventId: UUID, registrationId: UUID): EventLineupEntity?
}
