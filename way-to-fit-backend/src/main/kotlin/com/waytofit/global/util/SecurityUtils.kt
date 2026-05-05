package com.waytofit.global.util

import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {
    fun getCurrentAuditor(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication == null || !authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            null
        } else {
            authentication.name
        }
    }
}
