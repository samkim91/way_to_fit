package com.waytofit.global.domain

data class AuditedNames(
    val createdByName: String?,
    val lastModifiedByName: String?,
) {
    companion object {
        fun empty() = AuditedNames(null, null)
    }
}
