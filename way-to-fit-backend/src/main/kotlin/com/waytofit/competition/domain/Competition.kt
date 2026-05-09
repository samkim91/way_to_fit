package com.waytofit.competition.domain

import com.waytofit.competition.domain.enums.CompetitionStatus
import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.domain.AuditInfo
import com.waytofit.global.error.BusinessException
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
) {
    init {
        if (!registrationStartAt.isBefore(registrationEndAt)) {
            throw BusinessException(ResponseCode.COMPETITION_REGISTRATION_DATE_INVALID)
        }
        if (endAt.isBefore(startAt)) {
            throw BusinessException(ResponseCode.COMPETITION_PERIOD_INVALID)
        }
        if (endAt.isBefore(registrationEndAt)) {
            throw BusinessException(ResponseCode.COMPETITION_REGISTRATION_END_INVALID)
        }
    }
}
