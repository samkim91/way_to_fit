package com.waytofit.competition.domain

import com.waytofit.competition.domain.enums.CompetitionStatus
import com.waytofit.global.domain.AuditInfo
import java.time.Instant
import java.util.UUID

data class Competition(
    val id: UUID? = null,
    val name: String,
    val description: String,
    val bannerImageUrl: String? = null,
    val startAt: Instant,
    val endAt: Instant,
    val registrationStartAt: Instant,
    val registrationEndAt: Instant,
    val status: CompetitionStatus,
    val bankInfo: BankInfo,
    val audit: AuditInfo = AuditInfo.empty(),
)
