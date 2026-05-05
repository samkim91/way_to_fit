package com.waytofit.competition.adapter.out.persistence.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.waytofit.competition.adapter.out.persistence.entity.CompetitionEntity
import com.waytofit.competition.adapter.out.persistence.entity.QCompetitionEntity.competitionEntity
import com.waytofit.competition.adapter.out.persistence.entity.QCompetitionOrganizerEntity.competitionOrganizerEntity
import com.waytofit.competition.domain.enums.CompetitionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import java.util.UUID

class CompetitionCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CompetitionCustomRepository {

    override fun findMyCompetitions(
        userId: UUID,
        statuses: List<CompetitionStatus>?,
        pageable: Pageable
    ): Page<CompetitionEntity> {
        val content = queryFactory.selectFrom(competitionEntity)
            .join(competitionOrganizerEntity).on(competitionEntity.id.eq(competitionOrganizerEntity.competitionId))
            .where(
                competitionOrganizerEntity.userId.eq(userId),
                statuses?.let { if (it.isNotEmpty()) competitionEntity.status.`in`(it) else null }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(competitionEntity.createdAt.desc())
            .fetch()

        val countQuery = queryFactory.select(competitionEntity.count())
            .from(competitionEntity)
            .join(competitionOrganizerEntity).on(competitionEntity.id.eq(competitionOrganizerEntity.competitionId))
            .where(
                competitionOrganizerEntity.userId.eq(userId),
                statuses?.let { if (it.isNotEmpty()) competitionEntity.status.`in`(it) else null }
            )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }
}
