package com.waytofit.global.persistence.projection

import com.querydsl.core.annotations.QueryProjection

data class AuditNamesProjection @QueryProjection constructor(
    val createdByName: String?,
    val lastModifiedByName: String?,
)
