package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.CompetitionCommandUseCase
import com.waytofit.competition.application.port.`in`.CompetitionQueryUseCase
import com.waytofit.competition.application.port.`in`.CreateCompetitionCommand
import com.waytofit.competition.application.port.`in`.UpdateCompetitionCommand
import com.waytofit.competition.application.port.out.CompetitionOrganizerRepository
import com.waytofit.competition.application.port.out.CompetitionRepository
import com.waytofit.competition.domain.BankInfo
import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.enums.CompetitionLifecycle
import com.waytofit.competition.domain.enums.CompetitionVisibility
import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class CompetitionService(
    private val competitionRepository: CompetitionRepository,
    private val competitionOrganizerRepository: CompetitionOrganizerRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val clock: Clock,
) : CompetitionCommandUseCase, CompetitionQueryUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun createCompetition(command: CreateCompetitionCommand, creatorId: UUID): Competition {
        val competition = Competition(
            name = command.name,
            description = command.description,
            bannerImageUrl = command.bannerImageUrl,
            startAt = command.startAt,
            endAt = command.endAt,
            registrationStartAt = command.registrationStartAt,
            registrationEndAt = command.registrationEndAt,
            visibility = CompetitionVisibility.PRIVATE,
            bankInfo = BankInfo(
                bankName = command.bankName,
                accountNumber = command.accountNumber,
                accountHolder = command.accountHolder,
                entryFee = command.entryFee
            ),
            scaleCategories = command.scaleCategories,
        )
        val savedCompetition = competitionRepository.save(competition)
        competitionOrganizerRepository.saveOrganizer(savedCompetition.id!!, creatorId)
        return savedCompetition
    }

    override fun updateCompetition(command: UpdateCompetitionCommand, userId: UUID): Competition {
        val competition = competitionRepository.findById(command.id)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        if (!competitionOrganizerRepository.isOrganizer(competition.id!!, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }

        val updatedCompetition = competition.copy(
            name = command.name ?: competition.name,
            description = command.description ?: competition.description,
            bannerImageUrl = if (command.bannerImageUrl != null) command.bannerImageUrl else competition.bannerImageUrl,
            startAt = command.startAt ?: competition.startAt,
            endAt = command.endAt ?: competition.endAt,
            registrationStartAt = command.registrationStartAt ?: competition.registrationStartAt,
            registrationEndAt = command.registrationEndAt ?: competition.registrationEndAt,
            visibility = command.visibility ?: competition.visibility,
            bankInfo = competition.bankInfo.copy(
                bankName = command.bankName ?: competition.bankInfo.bankName,
                accountNumber = command.accountNumber ?: competition.bankInfo.accountNumber,
                accountHolder = command.accountHolder ?: competition.bankInfo.accountHolder,
                entryFee = command.entryFee ?: competition.bankInfo.entryFee
            ),
            scaleCategories = command.scaleCategories ?: competition.scaleCategories,
        )

        val savedCompetition = competitionRepository.save(updatedCompetition)

        val now = Instant.now(clock)
        if (competition.lifecycleAt(now) != CompetitionLifecycle.COMPLETED &&
            savedCompetition.lifecycleAt(now) == CompetitionLifecycle.COMPLETED
        ) {
            eventPublisher.publishEvent(com.waytofit.competition.domain.event.CompetitionCompletedEvent(savedCompetition.id!!))
        }

        return savedCompetition
    }

    @Transactional(readOnly = true)
    override fun getCompetition(id: UUID, userId: UUID?): Competition {
        val competition = competitionRepository.findById(id)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        if (competition.isPubliclyVisible()) {
            return competition
        }

        if (userId != null && competitionOrganizerRepository.isOrganizer(competition.id!!, userId)) {
            return competition
        }

        throw BusinessException(ResponseCode.NOT_FOUND)
    }

    @Transactional(readOnly = true)
    override fun getCompetitions(pageable: Pageable): Page<Competition> {
        return competitionRepository.findAllPublic(pageable)
    }

    @Transactional(readOnly = true)
    override fun getMyCompetitions(userId: UUID, lifecycles: List<CompetitionLifecycle>?, pageable: Pageable): Page<Competition> {
        log.debug("Loading my competitions: userId={}, lifecycles={}, pageable={}", userId, lifecycles, pageable)
        val page = competitionRepository.findMyCompetitions(userId, lifecycles, Instant.now(clock), pageable)
        log.debug(
            "Loaded my competitions: userId={}, lifecycles={}, page={}, size={}, contentSize={}, totalElements={}",
            userId,
            lifecycles,
            pageable.pageNumber,
            pageable.pageSize,
            page.content.size,
            page.totalElements
        )
        return page
    }
}
