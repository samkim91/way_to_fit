package com.waytofit.competition.application.port.out

import java.util.UUID

interface UserQueryPort {
    fun findNamesByIds(ids: Collection<UUID>): Map<UUID, String>
}
