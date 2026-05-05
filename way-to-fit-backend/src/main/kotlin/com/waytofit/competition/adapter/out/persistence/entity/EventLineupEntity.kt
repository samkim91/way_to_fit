package com.waytofit.competition.adapter.out.persistence.entity

import com.waytofit.competition.domain.EventLineup
import com.waytofit.global.domain.AuditInfo
import com.waytofit.global.persistence.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(
    name = "event_lineups",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["event_id", "registration_id"])
    ]
)
@SQLRestriction("deleted_at IS NULL")
class EventLineupEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "event_id", nullable = false)
    val eventId: UUID,

    @Column(name = "registration_id", nullable = false)
    val registrationId: UUID,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "event_lineup_members",
        joinColumns = [JoinColumn(name = "lineup_id")]
    )
    @Column(name = "user_id")
    val participatingMemberIds: List<UUID> = emptyList(),
) : BaseEntity() {

    fun toDomain(): EventLineup = EventLineup(
        id = id,
        eventId = eventId,
        registrationId = registrationId,
        participatingMemberIds = participatingMemberIds,
        audit = AuditInfo(
            createdAt = createdAt,
            createdBy = createdBy,
            lastModifiedAt = lastModifiedAt,
            lastModifiedBy = lastModifiedBy
        )
    )

    companion object {
        fun fromDomain(domain: EventLineup): EventLineupEntity = EventLineupEntity(
            id = domain.id,
            eventId = domain.eventId,
            registrationId = domain.registrationId,
            participatingMemberIds = domain.participatingMemberIds
        )
    }
}
