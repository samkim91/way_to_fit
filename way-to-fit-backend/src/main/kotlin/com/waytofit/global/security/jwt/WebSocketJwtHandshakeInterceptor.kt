package com.waytofit.global.security.jwt

import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import java.lang.Exception

@Component
class WebSocketJwtHandshakeInterceptor(
    private val jwtTokenProvider: JwtTokenProvider,
) : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        if (request is ServletServerHttpRequest) {
            val servletRequest = request.servletRequest
            
            // 1. 쿼리 파라미터에서 토큰 추출 (브라우저 WebSocket API 지원)
            var token = servletRequest.getParameter("token")

            // 2. 헤더에서 토큰 추출 (다른 클라이언트 지원)
            if (token == null) {
                val bearerToken = servletRequest.getHeader("Authorization")
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    token = bearerToken.substring(7)
                }
            }

            if (token != null && jwtTokenProvider.validateToken(token)) {
                val userId = jwtTokenProvider.getSubject(token)
                attributes["userId"] = userId
                return true
            }
        }
        return false
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) {
    }
}
