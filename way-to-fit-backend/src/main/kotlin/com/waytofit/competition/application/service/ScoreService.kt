package com.waytofit.competition.application.service

import com.waytofit.competition.application.port.`in`.*
import com.waytofit.competition.application.port.out.*
import com.waytofit.competition.domain.CompetitionScore
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.competition.domain.enums.ScoreStatus
import com.waytofit.competition.domain.event.ScoreReviewedEvent
import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import com.waytofit.competition.domain.enums.ResultStatus
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class ScoreService(
    private val scoreRepository: CompetitionScoreRepository,
    private val eventRepository: CompetitionEventRepository,
    private val stageRepository: CompetitionStageRepository,
    private val registrationRepository: CompetitionRegistrationRepository,
    private val teamMemberRepository: CompetitionTeamMemberRepository,
    private val organizerRepository: CompetitionOrganizerRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : SubmitScoreUseCase, GetScoreUseCase, OrganizerScoreQueryUseCase, ReviewScoreUseCase {

    private val youtubeRegex = Regex("^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+$")

    override fun submitScore(command: SubmitScoreCommand, userId: UUID): CompetitionScore {
        val event = eventRepository.findById(command.eventId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND, "이벤트를 찾을 수 없습니다.")

        if (Instant.now().isAfter(event.submissionDeadline)) {
            throw BusinessException(ResponseCode.INVALID_PARAMETER, "기록 제출 기한이 지났습니다.")
        }

        val registration = registrationRepository.findById(command.registrationId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND, "참가 신청 내역을 찾을 수 없습니다.")

        if (registration.paymentStatus != PaymentStatus.CONFIRMED) {
            throw BusinessException(ResponseCode.FORBIDDEN, "결제 승인 후 기록을 제출할 수 있습니다.")
        }

        val teamMembers = teamMemberRepository.findByRegistrationId(command.registrationId)
        if (teamMembers.none { it.userId == userId }) {
            throw BusinessException(ResponseCode.FORBIDDEN, "기록을 제출할 권한이 없습니다.")
        }

        if (!youtubeRegex.matches(command.videoUrl)) {
            throw BusinessException(ResponseCode.INVALID_PARAMETER, "올바른 YouTube URL 형식이 아닙니다.")
        }

        val existingScore = scoreRepository.findByEventIdAndRegistrationId(command.eventId, command.registrationId)
        val score = (existingScore?.copy(
            resultStatus = command.resultStatus,
            resultTimeSeconds = command.resultTimeSeconds,
            resultRounds = command.resultRounds,
            resultReps = command.resultReps,
            resultWeight = command.resultWeight,
            resultCustom = command.resultCustom,
            videoUrl = command.videoUrl,
            status = ScoreStatus.SUBMITTED
        ) ?: CompetitionScore(
            eventId = command.eventId,
            registrationId = command.registrationId,
            resultStatus = command.resultStatus,
            resultTimeSeconds = command.resultTimeSeconds,
            resultRounds = command.resultRounds,
            resultReps = command.resultReps,
            resultWeight = command.resultWeight,
            resultCustom = command.resultCustom,
            videoUrl = command.videoUrl,
            status = ScoreStatus.SUBMITTED
        ))

        return scoreRepository.save(score)
    }

    @Transactional(readOnly = true)
    override fun getScore(eventId: UUID, registrationId: UUID): CompetitionScore? {
        return scoreRepository.findByEventIdAndRegistrationId(eventId, registrationId)
    }

    @Transactional(readOnly = true)
    override fun getScoresByEventId(eventId: UUID, status: ScoreStatus?, userId: UUID): List<CompetitionScore> {
        val event = eventRepository.findById(eventId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        if (!organizerRepository.isOrganizer(event.competitionId, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }

        return scoreRepository.findScoresByEventId(eventId, status)
    }

    override fun reviewScore(command: ReviewScoreCommand, userId: UUID): CompetitionScore {
        val score = scoreRepository.findById(command.scoreId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        val event = eventRepository.findById(score.eventId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        if (!organizerRepository.isOrganizer(event.competitionId, userId)) {
            throw BusinessException(ResponseCode.FORBIDDEN)
        }

        val updatedScore = score.copy(
            status = command.status,
            resultTimeSeconds = command.adjustedResultTimeSeconds ?: score.resultTimeSeconds,
            resultRounds = command.adjustedResultRounds ?: score.resultRounds,
            resultReps = command.adjustedResultReps ?: score.resultReps,
            resultWeight = command.adjustedResultWeight ?: score.resultWeight,
            resultCustom = command.adjustedResultCustom ?: score.resultCustom,
            reviewerNote = command.reviewerNote,
            adjustedBy = if (command.status == ScoreStatus.ADJUSTED) userId else score.adjustedBy
        )

        val savedScore = scoreRepository.save(updatedScore)

        val stage = stageRepository.findById(event.stageId)
            ?: throw BusinessException(ResponseCode.NOT_FOUND)

        eventPublisher.publishEvent(
            ScoreReviewedEvent(
                competitionId = event.competitionId,
                stageId = stage.id!!,
                eventId = event.id!!,
                scoreId = savedScore.id!!,
                status = savedScore.status
            )
        )

        return savedScore
    }
}
