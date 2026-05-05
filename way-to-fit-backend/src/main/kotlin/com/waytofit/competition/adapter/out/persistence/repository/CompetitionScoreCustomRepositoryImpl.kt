package com.waytofit.competition.adapter.out.persistence.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.waytofit.competition.adapter.out.persistence.entity.CompetitionScoreEntity
import com.waytofit.competition.adapter.out.persistence.entity.QCompetitionRegistrationEntity.competitionRegistrationEntity
import com.waytofit.competition.adapter.out.persistence.entity.QCompetitionScoreEntity.competitionScoreEntity
import com.waytofit.competition.adapter.out.persistence.entity.QCompetitionTeamMemberEntity.competitionTeamMemberEntity
import com.waytofit.competition.domain.enums.ScoreStatus
import com.waytofit.user.adapter.out.persistence.entity.QUserEntity.userEntity
import java.util.UUID

class CompetitionScoreCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CompetitionScoreCustomRepository {

    override fun findAllByEventIdWithDetails(eventId: UUID, status: ScoreStatus?): List<CompetitionScoreEntity> {
        return queryFactory.selectFrom(competitionScoreEntity)
            .join(competitionScoreEntity.registration, competitionRegistrationEntity).fetchJoin()
            .leftJoin(competitionRegistrationEntity.user, userEntity).fetchJoin()
            .leftJoin(competitionRegistrationEntity.teamMembers, competitionTeamMemberEntity).fetchJoin()
            .where(
                competitionScoreEntity.eventId.eq(eventId),
                status?.let { competitionScoreEntity.status.eq(it) }
            )
            .distinct()
            .fetch()
    }
}
