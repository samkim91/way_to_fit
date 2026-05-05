package com.waytofit.competition.domain

import com.waytofit.global.domain.AuditInfo
import java.util.UUID

data class EventLineup(
    val id: UUID? = null,
    val eventId: UUID,
    val registrationId: UUID,
    val participatingMemberIds: List<UUID>,
    val audit: AuditInfo = AuditInfo.empty(),
)
