package com.waytofit.competition.adapter.out.persistence.entity

import com.waytofit.competition.domain.CompetitionRegistration
import com.waytofit.competition.domain.enums.PaymentStatus
import com.waytofit.competition.domain.enums.RegistrationType
import com.waytofit.global.domain.AuditInfo
import com.waytofit.global.persistence.BaseEntity
import com.waytofit.user.domain.enums.Gender
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(
    name = "competition_registrations",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["competition_id", "user_id", "registration_type"])
    ]
)
@SQLRestriction("deleted_at IS NULL")
class CompetitionRegistrationEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "competition_id", nullable = false)
    val competitionId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_type", nullable = false)
    val registrationType: RegistrationType,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    val gender: Gender,

    @Column(name = "team_name")
    val teamName: String? = null,

    @Column(name = "scale_category", nullable = false)
    val scaleCategory: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    val paymentStatus: PaymentStatus,

    @Column(name = "payment_note", columnDefinition = "TEXT")
    val paymentNote: String? = null,

    @Column(name = "manual_rank")
    val manualRank: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: com.waytofit.user.adapter.out.persistence.entity.UserEntity? = null,

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", insertable = false, updatable = false)
    val teamMembers: List<CompetitionTeamMemberEntity> = emptyList(),
) : BaseEntity() {

    fun toDomain(): CompetitionRegistration = CompetitionRegistration(
        id = id,
        competitionId = competitionId,
        userId = userId,
        registrationType = registrationType,
        gender = gender,
        teamName = teamName,
        scaleCategory = scaleCategory,
        paymentStatus = paymentStatus,
        paymentNote = paymentNote,
        manualRank = manualRank,
        audit = AuditInfo(
            createdAt = createdAt,
            createdBy = createdBy,
            lastModifiedAt = lastModifiedAt,
            lastModifiedBy = lastModifiedBy
        )
    )

    companion object {
        fun fromDomain(domain: CompetitionRegistration): CompetitionRegistrationEntity = CompetitionRegistrationEntity(
            id = domain.id,
            competitionId = domain.competitionId,
            userId = domain.userId,
            registrationType = domain.registrationType,
            gender = domain.gender,
            teamName = domain.teamName,
            scaleCategory = domain.scaleCategory,
            paymentStatus = domain.paymentStatus,
            paymentNote = domain.paymentNote,
            manualRank = domain.manualRank
        )
    }
}
