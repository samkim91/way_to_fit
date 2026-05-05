package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.enums.CompetitionStatus
import java.time.Instant
import java.util.UUID

data class CreateCompetitionRequest(
    val name: String,
    val description: String,
    val startAt: Instant,
    val endAt: Instant,
    val registrationStartAt: Instant,
    val registrationEndAt: Instant,
    val bankName: String,
    val accountNumber: String,
    val accountHolder: String,
    val entryFee: Int,
    val bannerImageUrl: String?,
) {
    fun toCommand() = com.waytofit.competition.application.port.`in`.CreateCompetitionCommand(
        name = name,
        description = description,
        startAt = startAt,
        endAt = endAt,
        registrationStartAt = registrationStartAt,
        registrationEndAt = registrationEndAt,
        bankName = bankName,
        accountNumber = accountNumber,
        accountHolder = accountHolder,
        entryFee = entryFee,
        bannerImageUrl = bannerImageUrl
    )
}

data class UpdateCompetitionRequest(
    val name: String?,
    val description: String?,
    val startAt: Instant?,
    val endAt: Instant?,
    val registrationStartAt: Instant?,
    val registrationEndAt: Instant?,
    val status: CompetitionStatus?,
    val bankName: String?,
    val accountNumber: String?,
    val accountHolder: String?,
    val entryFee: Int?,
    val bannerImageUrl: String?,
) {
    fun toCommand(id: UUID) = com.waytofit.competition.application.port.`in`.UpdateCompetitionCommand(
        id = id,
        name = name,
        description = description,
        startAt = startAt,
        endAt = endAt,
        registrationStartAt = registrationStartAt,
        registrationEndAt = registrationEndAt,
        status = status,
        bankName = bankName,
        accountNumber = accountNumber,
        accountHolder = accountHolder,
        entryFee = entryFee,
        bannerImageUrl = bannerImageUrl
    )
}

data class CompetitionResponse(
    val id: UUID,
    val name: String,
    val description: String,
    val startAt: Instant,
    val endAt: Instant,
    val registrationStartAt: Instant,
    val registrationEndAt: Instant,
    val status: CompetitionStatus,
    val bankName: String,
    val accountNumber: String,
    val accountHolder: String,
    val entryFee: Int,
    val bannerImageUrl: String?,
    val createdAt: Instant?,
) {
    companion object {
        fun fromDomain(competition: Competition) = CompetitionResponse(
            id = competition.id!!,
            name = competition.name,
            description = competition.description,
            startAt = competition.startAt,
            endAt = competition.endAt,
            registrationStartAt = competition.registrationStartAt,
            registrationEndAt = competition.registrationEndAt,
            status = competition.status,
            bankName = competition.bankInfo.bankName,
            accountNumber = competition.bankInfo.accountNumber,
            accountHolder = competition.bankInfo.accountHolder,
            entryFee = competition.bankInfo.entryFee,
            bannerImageUrl = competition.bannerImageUrl,
            createdAt = competition.audit.createdAt
        )
    }
}
