package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.domain.EventLineup
import java.util.UUID

data class SetLineupRequest(
    val participatingMemberIds: List<UUID>,
) {
    fun toCommand(eventId: UUID, registrationId: UUID) = com.waytofit.competition.application.port.`in`.SetLineupCommand(
        eventId = eventId,
        registrationId = registrationId,
        participatingMemberIds = participatingMemberIds
    )
}

data class EventLineupResponse(
    val id: UUID,
    val eventId: UUID,
    val registrationId: UUID,
    val participatingMemberIds: List<UUID>,
) {
    companion object {
        fun fromDomain(domain: EventLineup) = EventLineupResponse(
            id = domain.id!!,
            eventId = domain.eventId,
            registrationId = domain.registrationId,
            participatingMemberIds = domain.participatingMemberIds
        )
    }
}
