package com.waytofit.competition.adapter.out.persistence.entity

import com.waytofit.competition.domain.AthleteProfile
import com.waytofit.global.persistence.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(
    name = "athlete_profiles",
    indexes = [Index(name = "idx_athlete_profile_user_id", columnList = "user_id", unique = true)]
)
@SQLRestriction("deleted_at IS NULL")
class AthleteProfileEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: UUID,

    @Column(name = "box_id")
    val boxId: UUID? = null,

    @Column(name = "biography", length = 1000)
    val biography: String? = null,

    @Column(name = "profile_image_url", length = 500)
    val profileImageUrl: String? = null,
) : BaseEntity() {

    fun toDomain(): AthleteProfile = AthleteProfile(
        id = id,
        userId = userId,
        boxId = boxId,
        biography = biography,
        profileImageUrl = profileImageUrl
    )

    companion object {
        fun fromDomain(domain: AthleteProfile): AthleteProfileEntity = AthleteProfileEntity(
            id = domain.id,
            userId = domain.userId,
            boxId = domain.boxId,
            biography = domain.biography,
            profileImageUrl = domain.profileImageUrl
        )
    }
}
