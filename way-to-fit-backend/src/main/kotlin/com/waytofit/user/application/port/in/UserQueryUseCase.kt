package com.waytofit.user.application.port.`in`

import com.waytofit.user.domain.User
import java.util.UUID

interface UserQueryUseCase {
    fun findById(id: UUID): User?
}
