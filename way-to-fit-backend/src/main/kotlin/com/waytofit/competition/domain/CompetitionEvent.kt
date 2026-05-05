package com.waytofit.competition.domain

import com.waytofit.competition.domain.enums.EventType
import com.waytofit.global.domain.AuditInfo
import com.waytofit.competition.domain.enums.GenderCategory
import com.waytofit.competition.domain.enums.WeightUnit
import com.waytofit.competition.domain.enums.WodType
import java.time.Instant
import java.util.UUID

data class CompetitionEvent(
    val id: UUID? = null,
    val stageId: UUID,
    val competitionId: UUID,
    val name: String,
    val description: String,
    val eventType: EventType,
    val gender: GenderCategory,
    val wodType: WodType,
    val timeCap: Int? = null,
    val amrapDuration: Int? = null,
    val emomDuration: Int? = null,
    val weightUnit: WeightUnit? = null,
    val order: Int,
    val scaleCategories: List<String>,
    val releaseAt: Instant? = null,
    val submissionDeadline: Instant,
    val audit: AuditInfo = AuditInfo.empty(),
)
