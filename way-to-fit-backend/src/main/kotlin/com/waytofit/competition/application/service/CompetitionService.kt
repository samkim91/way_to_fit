package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.CompetitionCommandUseCase
import com.waytofit.competition.application.port.`in`.CompetitionQueryUseCase
import com.waytofit.competition.application.port.`in`.CreateCompetitionCommand
import com.waytofit.competition.application.port.`in`.UpdateCompetitionCommand
import com.waytofit.competition.application.port.out.CompetitionOrganizerRepository
import com.waytofit.competition.application.port.out.CompetitionRepository
import com.waytofit.competition.domain.BankInfo
import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.enums.CompetitionStatus
import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class CompetitionService(
    private val competitionRepository: CompetitionRepository,
    private val competitionOrganizerRepository: CompetitionOrganizerRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : CompetitionCommandUseCase, CompetitionQueryUseCase {

    override fun createCompetition(command: CreateCompetitionCommand, creatorId: UUID): Competition {
        val competition = Competition(
            name = command.name,
            description = command.description,
            bannerImageUrl = command.bannerImageUrl,
            startAt = command.startAt,
            endAt = command.endAt,
            registrationStartAt = command.registrationStartAt,
            registrationEndAt = command.registrationEndAt,
            status = CompetitionStatus.DRAFT,
            bankInfo = BankInfo(
                bankName = command.bankName,
                accountNumber = command.accountNumber,
                accountHolder = command.accountHolder,
                entryFee = command.entryFee
            )
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
            status = command.status ?: competition.status,
            bankInfo = competition.bankInfo.copy(
                bankName = command.bankName ?: competition.bankInfo.bankName,
                accountNumber = command.accountNumber ?: competition.bankInfo.accountNumber,
                accountHolder = command.accountHolder ?: competition.bankInfo.accountHolder,
                entryFee = command.entryFee ?: competition.bankInfo.entryFee
            )
        )

        val savedCompetition = competitionRepository.save(updatedCompetition)

        if (competition.status != CompetitionStatus.COMPLETED && savedCompetition.status == CompetitionStatus.COMPLETED) {
            eventPublisher.publishEvent(com.waytofit.competition.domain.event.CompetitionCompletedEvent(savedCompetition.id!!))
        }

        return savedCompetition
    }

    @Transactional(readOnly = true)
    override fun getCompetition(id: UUID): Competition {
        return competitionRepository.findById(id)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)
    }

    @Transactional(readOnly = true)
    override fun getCompetitions(pageable: Pageable): Page<Competition> {
        return competitionRepository.findAllExcludingDrafts(pageable)
    }

    @Transactional(readOnly = true)
    override fun getMyCompetitions(userId: UUID, statuses: List<CompetitionStatus>?, pageable: Pageable): Page<Competition> {
        return competitionRepository.findMyCompetitions(userId, statuses, pageable)
    }
}
