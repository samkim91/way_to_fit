package com.waytofit.competition.adapter.out.persistence.entity

import com.waytofit.competition.domain.BankInfo
import com.waytofit.competition.domain.Competition
import com.waytofit.competition.domain.enums.CompetitionStatus
import com.waytofit.global.domain.AuditInfo
import com.waytofit.global.persistence.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "competitions")
@SQLRestriction("deleted_at IS NULL")
class CompetitionEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String,

    @Column(name = "banner_image_url")
    val bannerImageUrl: String? = null,

    @Column(name = "start_at", nullable = false)
    val startAt: Instant,

    @Column(name = "end_at", nullable = false)
    val endAt: Instant,

    @Column(name = "registration_start_at", nullable = false)
    val registrationStartAt: Instant,

    @Column(name = "registration_end_at", nullable = false)
    val registrationEndAt: Instant,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: CompetitionStatus,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "bankName", column = Column(name = "bank_name")),
        AttributeOverride(name = "accountNumber", column = Column(name = "account_number")),
        AttributeOverride(name = "accountHolder", column = Column(name = "account_holder")),
        AttributeOverride(name = "entryFee", column = Column(name = "entry_fee")),
    )
    val bankInfo: BankInfo,
) : BaseEntity() {

    fun toDomain(): Competition = Competition(
        id = id,
        name = name,
        description = description,
        bannerImageUrl = bannerImageUrl,
        startAt = startAt,
        endAt = endAt,
        registrationStartAt = registrationStartAt,
        registrationEndAt = registrationEndAt,
        status = status,
        bankInfo = bankInfo,
        audit = AuditInfo(
            createdAt = createdAt,
            createdBy = createdBy,
            lastModifiedAt = lastModifiedAt,
            lastModifiedBy = lastModifiedBy
        )
    )

    companion object {
        fun fromDomain(competition: Competition): CompetitionEntity = CompetitionEntity(
            id = competition.id,
            name = competition.name,
            description = competition.description,
            bannerImageUrl = competition.bannerImageUrl,
            startAt = competition.startAt,
            endAt = competition.endAt,
            registrationStartAt = competition.registrationStartAt,
            registrationEndAt = competition.registrationEndAt,
            status = competition.status,
            bankInfo = competition.bankInfo
        )
    }
}
