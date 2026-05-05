package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionTeamMemberEntity
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionTeamMemberJpaRepository
import com.waytofit.competition.application.port.out.CompetitionTeamMemberRepository
import com.waytofit.competition.domain.CompetitionTeamMember
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CompetitionTeamMemberPersistenceAdapter(
    private val jpaRepository: CompetitionTeamMemberJpaRepository,
) : CompetitionTeamMemberRepository {

    override fun saveAll(members: List<CompetitionTeamMember>): List<CompetitionTeamMember> {
        val entities = members.map { CompetitionTeamMemberEntity.fromDomain(it) }
        return jpaRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun findByRegistrationId(registrationId: UUID): List<CompetitionTeamMember> {
        return jpaRepository.findAllByRegistrationId(registrationId).map { it.toDomain() }
    }

    override fun isUserAlreadyInAnyTeam(competitionId: UUID, userId: UUID): Boolean {
        return jpaRepository.existsByCompetitionIdAndUserId(competitionId, userId)
    }

    override fun findAllByUserId(userId: UUID): List<CompetitionTeamMember> {
        return jpaRepository.findAllByUserId(userId).map { it.toDomain() }
    }
}
