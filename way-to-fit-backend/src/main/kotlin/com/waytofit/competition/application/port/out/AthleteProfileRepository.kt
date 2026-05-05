package com.waytofit.competition.application.port.out

import com.waytofit.competition.domain.AthleteProfile
import com.waytofit.competition.domain.AthleteSearchResult
import java.util.UUID

interface AthleteProfileRepository {
    fun findByUserId(userId: UUID): AthleteProfile?
    fun save(athleteProfile: AthleteProfile): AthleteProfile
    fun existsByUserId(userId: UUID): Boolean
    fun searchAthletesByName(name: String): List<AthleteSearchResult>
}
