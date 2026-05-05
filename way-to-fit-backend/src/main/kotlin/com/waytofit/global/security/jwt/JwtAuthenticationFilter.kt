package com.waytofit.global.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
) : OncePerRequestFilter() {

    companion object {
        private const val USER_ID = "userId"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)

        if (token != null && jwtTokenProvider.validateToken(token)) {
            val subject = jwtTokenProvider.getSubject(token)
            val role = jwtTokenProvider.getRole(token)
            
            val authorities = role?.let {
                listOf(SimpleGrantedAuthority("ROLE_$it"))
            } ?: emptyList()

            val authentication = UsernamePasswordAuthenticationToken(subject, null, authorities)
            SecurityContextHolder.getContext().authentication = authentication
            
            // MDC에 유저 ID 기록
            MDC.put(USER_ID, subject)
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(USER_ID)
        }
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization")
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7)
        }
        return null
    }
}
