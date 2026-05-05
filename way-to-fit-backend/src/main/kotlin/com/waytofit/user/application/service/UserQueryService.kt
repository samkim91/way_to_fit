package com.waytofit.user.application.service

import com.waytofit.user.application.port.`in`.UserQueryUseCase
import com.waytofit.user.application.port.out.UserPersistencePort
import com.waytofit.user.domain.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserQueryService(
    private val userRepository: UserPersistencePort,
) : UserQueryUseCase {

    @Transactional(readOnly = true)
    override fun findById(id: UUID): User? {
        return userRepository.findById(id)
    }
}
