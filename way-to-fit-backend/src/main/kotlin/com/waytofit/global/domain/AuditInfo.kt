package com.waytofit.global.domain

import java.time.Instant

data class AuditInfo(
    val createdAt: Instant? = null,
    val createdBy: String? = null,
    val lastModifiedAt: Instant? = null,
    val lastModifiedBy: String? = null,
) {
    companion object {
        fun empty() = AuditInfo()
    }
}
