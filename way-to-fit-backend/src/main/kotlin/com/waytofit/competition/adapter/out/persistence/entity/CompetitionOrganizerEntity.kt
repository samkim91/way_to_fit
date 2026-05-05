package com.waytofit.competition.adapter.out.persistence.entity

import com.waytofit.competition.domain.CompetitionOrganizer
import com.waytofit.global.persistence.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(
    name = "competition_organizers",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["competition_id", "user_id"])
    ]
)
@SQLRestriction("deleted_at IS NULL")
class CompetitionOrganizerEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "competition_id", nullable = false)
    val competitionId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,
) : BaseEntity() {

    fun toDomain(): CompetitionOrganizer = CompetitionOrganizer(
        competitionId = competitionId,
        userId = userId,
    )

    companion object {
        fun fromDomain(domain: CompetitionOrganizer): CompetitionOrganizerEntity = CompetitionOrganizerEntity(
            competitionId = domain.competitionId,
            userId = domain.userId,
        )
    }
}
