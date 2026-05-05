package com.waytofit.competition.adapter.out.persistence.entity

import com.waytofit.competition.domain.CompetitionTeamMember
import com.waytofit.competition.domain.enums.TeamRole
import com.waytofit.user.domain.enums.Gender
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(
    name = "competition_team_members",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["registration_id", "user_id"])
    ]
)
class CompetitionTeamMemberEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "registration_id", nullable = false)
    val registrationId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    val gender: Gender,

    @Enumerated(EnumType.STRING)
    @Column(name = "team_role", nullable = false)
    val teamRole: TeamRole,
) {
    fun toDomain(): CompetitionTeamMember = CompetitionTeamMember(
        id = id,
        registrationId = registrationId,
        userId = userId,
        gender = gender,
        teamRole = teamRole
    )

    companion object {
        fun fromDomain(domain: CompetitionTeamMember): CompetitionTeamMemberEntity = CompetitionTeamMemberEntity(
            id = domain.id,
            registrationId = domain.registrationId,
            userId = domain.userId,
            gender = domain.gender,
            teamRole = domain.teamRole
        )
    }
}
