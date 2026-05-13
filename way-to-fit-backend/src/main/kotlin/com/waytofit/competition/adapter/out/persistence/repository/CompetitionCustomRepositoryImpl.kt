package com.waytofit.competition.adapter.out.persistence.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.waytofit.competition.adapter.out.persistence.entity.CompetitionEntity
import com.waytofit.competition.adapter.out.persistence.entity.QCompetitionEntity.competitionEntity
import com.waytofit.competition.adapter.out.persistence.entity.QCompetitionOrganizerEntity.competitionOrganizerEntity
import com.waytofit.competition.domain.enums.CompetitionLifecycle
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import java.time.Instant
import java.util.UUID

class CompetitionCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CompetitionCustomRepository {

    override fun findMyCompetitions(
        userId: UUID,
        lifecycles: List<CompetitionLifecycle>?,
        now: Instant,
        pageable: Pageable
    ): Page<CompetitionEntity> {
        val lifecycleFilter = buildLifecycleFilter(lifecycles, now)

        val content = queryFactory.selectFrom(competitionEntity)
            .join(competitionOrganizerEntity).on(competitionEntity.id.eq(competitionOrganizerEntity.competitionId))
            .where(
                competitionOrganizerEntity.userId.eq(userId),
                lifecycleFilter
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
                lifecycleFilter
            )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    private fun buildLifecycleFilter(
        lifecycles: List<CompetitionLifecycle>?,
        now: Instant
    ): Predicate? {
        if (lifecycles.isNullOrEmpty()) return null

        val builder = BooleanBuilder()
        lifecycles.distinct().forEach { lifecycle ->
            builder.or(
                when (lifecycle) {
                    CompetitionLifecycle.PUBLISHED ->
                        competitionEntity.registrationStartAt.gt(now)
                    CompetitionLifecycle.REGISTRATION_OPEN ->
                        competitionEntity.registrationStartAt.loe(now)
                            .and(competitionEntity.registrationEndAt.goe(now))
                            .and(competitionEntity.startAt.gt(now))
                    CompetitionLifecycle.REGISTRATION_CLOSED ->
                        competitionEntity.registrationEndAt.lt(now)
                            .and(competitionEntity.startAt.gt(now))
                    CompetitionLifecycle.IN_PROGRESS ->
                        competitionEntity.startAt.loe(now)
                            .and(competitionEntity.endAt.goe(now))
                    CompetitionLifecycle.COMPLETED ->
                        competitionEntity.endAt.lt(now)
                }
            )
        }

        return builder
    }
}
