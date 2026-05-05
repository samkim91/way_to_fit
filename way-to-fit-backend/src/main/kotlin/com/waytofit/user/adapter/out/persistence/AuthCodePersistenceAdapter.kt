package com.waytofit.user.adapter.out.persistence

import com.waytofit.user.adapter.out.persistence.entity.AuthCodeEntity
import com.waytofit.user.adapter.out.persistence.repository.AuthCodeRepository
import com.waytofit.user.application.port.out.AuthCodePersistencePort
import com.waytofit.user.domain.AuthCode
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AuthCodePersistenceAdapter(
    private val authCodeRepository: AuthCodeRepository,
) : AuthCodePersistencePort {

    @Transactional
    override fun save(authCode: AuthCode): AuthCode {
        return authCodeRepository.save(AuthCodeEntity.fromDomain(authCode)).toDomain()
    }

    @Transactional(readOnly = true)
    override fun findByCode(code: String): AuthCode? {
        return authCodeRepository.findByCode(code).map { it.toDomain() }.orElse(null)
    }

    @Transactional
    override fun deleteByCode(code: String) {
        authCodeRepository.deleteByCode(code)
    }
}
