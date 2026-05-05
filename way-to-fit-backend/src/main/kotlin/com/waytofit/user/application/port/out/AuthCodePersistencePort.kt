package com.waytofit.user.application.port.out

import com.waytofit.user.domain.AuthCode

interface AuthCodePersistencePort {
    fun save(authCode: AuthCode): AuthCode
    fun findByCode(code: String): AuthCode?
    fun deleteByCode(code: String)
}
