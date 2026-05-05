package com.waytofit.global.domain

data class AuditedResult<T>(
    val value: T,
    val auditNames: AuditedNames,
)
