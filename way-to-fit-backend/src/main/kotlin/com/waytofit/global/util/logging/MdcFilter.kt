package com.waytofit.global.util.logging

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.*

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MdcFilter : Filter {

    companion object {
        private const val TRACE_ID = "traceId"
        private const val TRACE_ID_HEADER = "X-Trace-Id"
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 12)
        MDC.put(TRACE_ID, traceId)

        try {
            if (response is HttpServletResponse) {
                response.setHeader(TRACE_ID_HEADER, traceId)
            }
            chain.doFilter(request, response)
        } finally {
            MDC.remove(TRACE_ID)
        }
    }
}
