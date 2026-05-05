package com.waytofit.competition.domain

import com.waytofit.competition.domain.enums.ScoreStatus
import com.waytofit.global.domain.AuditInfo
import com.waytofit.competition.domain.enums.ResultStatus
import java.math.BigDecimal
import java.util.UUID

data class CompetitionScore(
    val id: UUID? = null,
    val eventId: UUID,
    val registrationId: UUID,
    val resultStatus: ResultStatus,
    val resultTimeSeconds: Int? = null,
    val resultRounds: Int? = null,
    val resultReps: Int? = null,
    val resultWeight: BigDecimal? = null,
    val resultCustom: String? = null,
    val videoUrl: String,
    val status: ScoreStatus,
    val reviewerNote: String? = null,
    val adjustedBy: UUID? = null,
    val audit: AuditInfo = AuditInfo.empty(),
)
