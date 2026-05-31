package com.waytofit.competition.adapter.`in`.web.dto

import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.enums.CompetitionLifecycle
import com.waytofit.competition.domain.enums.CompetitionVisibility
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
    val scaleCategories: List<String> = emptyList(),
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
        bannerImageUrl = bannerImageUrl,
        scaleCategories = scaleCategories,
    )
}

data class UpdateCompetitionRequest(
    val name: String?,
    val description: String?,
    val startAt: Instant?,
    val endAt: Instant?,
    val registrationStartAt: Instant?,
    val registrationEndAt: Instant?,
    val visibility: CompetitionVisibility?,
    val bankName: String?,
    val accountNumber: String?,
    val accountHolder: String?,
    val entryFee: Int?,
    val bannerImageUrl: String?,
    val scaleCategories: List<String>? = null,
) {
    fun toCommand(id: UUID) = com.waytofit.competition.application.port.`in`.UpdateCompetitionCommand(
        id = id,
        name = name,
        description = description,
        startAt = startAt,
        endAt = endAt,
        registrationStartAt = registrationStartAt,
        registrationEndAt = registrationEndAt,
        visibility = visibility,
        bankName = bankName,
        accountNumber = accountNumber,
        accountHolder = accountHolder,
        entryFee = entryFee,
        bannerImageUrl = bannerImageUrl,
        scaleCategories = scaleCategories,
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
    val visibility: CompetitionVisibility,
    val lifecycle: CompetitionLifecycle,
    val bankName: String,
    val accountNumber: String,
    val accountHolder: String,
    val entryFee: Int,
    val bannerImageUrl: String?,
    val createdAt: Instant?,
    val scaleCategories: List<String>,
) {
    companion object {
        fun fromDomain(competition: Competition, now: Instant) = CompetitionResponse(
            id = competition.id!!,
            name = competition.name,
            description = competition.description,
            startAt = competition.startAt,
            endAt = competition.endAt,
            registrationStartAt = competition.registrationStartAt,
            registrationEndAt = competition.registrationEndAt,
            visibility = competition.visibility,
            lifecycle = competition.lifecycleAt(now),
            bankName = competition.bankInfo.bankName,
            accountNumber = competition.bankInfo.accountNumber,
            accountHolder = competition.bankInfo.accountHolder,
            entryFee = competition.bankInfo.entryFee,
            bannerImageUrl = competition.bannerImageUrl,
            createdAt = competition.audit.createdAt,
            scaleCategories = competition.scaleCategories,
        )
    }
}
