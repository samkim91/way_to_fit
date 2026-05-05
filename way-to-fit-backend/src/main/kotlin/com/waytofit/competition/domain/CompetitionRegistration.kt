package com.waytofit.competition.domain

import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.global.domain.AuditInfo
import com.waytofit.user.domain.enums.Gender
import java.util.UUID

data class CompetitionRegistration(
    val id: UUID? = null,
    val competitionId: UUID,
    val userId: UUID,
    val registrationType: RegistrationType,
    val gender: Gender,
    val teamName: String? = null,
    val scaleCategory: String,
    val paymentStatus: PaymentStatus,
    val paymentNote: String? = null,
    val manualRank: Int? = null,
    val audit: AuditInfo = AuditInfo.empty(),
)
