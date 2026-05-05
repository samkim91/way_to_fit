package com.waytofit.user.adapter.out.persistence.repository

import com.waytofit.user.adapter.out.persistence.entity.AuthCodeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface AuthCodeRepository : JpaRepository<AuthCodeEntity, UUID> {
    fun findByCode(code: String): Optional<AuthCodeEntity>
    fun deleteByCode(code: String)
}
