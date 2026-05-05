package com.waytofit.global.persistence

import com.waytofit.global.util.SecurityUtils
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class AuditorAwareImpl : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
        return Optional.ofNullable(SecurityUtils.getCurrentAuditor())
    }
}
