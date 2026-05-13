package com.waytofit.competition.application.port.`in`

import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.enums.CompetitionLifecycle
import com.waytofit.competition.domain.enums.CompetitionVisibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

interface CompetitionCommandUseCase {
    fun createCompetition(command: CreateCompetitionCommand, creatorId: UUID): Competition
    fun updateCompetition(command: UpdateCompetitionCommand, userId: UUID): Competition
}

interface CompetitionQueryUseCase {
    fun getCompetition(id: UUID, userId: UUID?): Competition
    fun getCompetitions(pageable: Pageable): Page<Competition>
    fun getMyCompetitions(userId: UUID, lifecycles: List<CompetitionLifecycle>?, pageable: Pageable): Page<Competition>
}

data class CreateCompetitionCommand(
    val name: String,
    val description: String,
    val bannerImageUrl: String? = null,
    val startAt: Instant,
    val endAt: Instant,
    val registrationStartAt: Instant,
    val registrationEndAt: Instant,
    val bankName: String,
    val accountNumber: String,
    val accountHolder: String,
    val entryFee: Int = 0,
)

data class UpdateCompetitionCommand(
    val id: UUID,
    val name: String?,
    val description: String?,
    val bannerImageUrl: String?,
    val startAt: Instant?,
    val endAt: Instant?,
    val registrationStartAt: Instant?,
    val registrationEndAt: Instant?,
    val visibility: CompetitionVisibility?,
    val bankName: String?,
    val accountNumber: String?,
    val accountHolder: String?,
    val entryFee: Int?,
)
