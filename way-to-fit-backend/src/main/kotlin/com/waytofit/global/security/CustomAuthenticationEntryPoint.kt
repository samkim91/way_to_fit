package com.waytofit.global.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.waytofit.global.common.response.ApiResponse
import com.waytofit.global.common.response.ResponseCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val responseCode = ResponseCode.UNAUTHORIZED
        response.status = responseCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(
            objectMapper.writeValueAsString(ApiResponse.error<Unit>(responseCode))
        )
    }
}
