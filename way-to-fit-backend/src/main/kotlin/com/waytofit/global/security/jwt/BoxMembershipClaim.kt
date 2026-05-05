package com.waytofit.global.security.jwt

import java.util.UUID

data class BoxMembershipClaim(
    val boxId: UUID,
    val boxName: String,
    val role: String,
)
