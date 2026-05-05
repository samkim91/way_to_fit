package com.waytofit.competition.domain

import com.waytofit.competition.domain.enums.StageFormat
import com.waytofit.competition.domain.enums.StageType
import com.waytofit.global.domain.AuditInfo
import java.time.Instant
import java.util.UUID

data class CompetitionStage(
    val id: UUID? = null,
    val competitionId: UUID,
    val name: String,
    val stageType: StageType,
    val stageFormat: StageFormat,
    val startAt: Instant,
    val endAt: Instant,
    val audit: AuditInfo = AuditInfo.empty(),
)
