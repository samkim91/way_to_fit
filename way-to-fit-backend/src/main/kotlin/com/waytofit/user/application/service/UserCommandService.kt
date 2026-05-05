package com.waytofit.user.application.service

import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import com.waytofit.user.application.port.`in`.UserCommandUseCase
import com.waytofit.user.application.port.out.UserPersistencePort
import com.waytofit.user.domain.User
import com.waytofit.user.domain.enums.OAuthProvider
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserCommandService(
    private val userRepository: UserPersistencePort,
) : UserCommandUseCase {

    @Transactional
    override fun findOrCreate(
        oauthProvider: OAuthProvider,
        oauthId: String,
        email: String?,
        name: String,
    ): User {
        return try {
            userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId)
                ?: userRepository.save(User.create(oauthProvider, oauthId, email, name))
        } catch (e: DataIntegrityViolationException) {
            userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId)
                ?: throw BusinessException(ResponseCode.USER_DATA_INTEGRITY_VIOLATION)
        }
    }
}
