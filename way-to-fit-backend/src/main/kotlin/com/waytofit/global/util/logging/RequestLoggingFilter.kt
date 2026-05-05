package com.waytofit.global.util.logging

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch

@Component
class RequestLoggingFilter : Filter {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request !is HttpServletRequest || response !is HttpServletResponse) {
            chain.doFilter(request, response)
            return
        }

        // 특정 경로는 로깅 제외 (예: actuator, swagger 등)
        val uri = request.requestURI
        if (com.waytofit.global.config.SecurityConstants.LOGGING_EXCLUDE_URLS.any { uri.contains(it) }) {
            chain.doFilter(request, response)
            return
        }

        val stopWatch = StopWatch()
        stopWatch.start()

        log.info(">>> REQUEST [{} {}] from {}", request.method, uri, request.remoteAddr)

        try {
            chain.doFilter(request, response)
        } finally {
            stopWatch.stop()
            log.info("<<< RESPONSE [{} {}] Status: {}, Time: {}ms", 
                request.method, uri, response.status, stopWatch.totalTimeMillis)
        }
    }
}
