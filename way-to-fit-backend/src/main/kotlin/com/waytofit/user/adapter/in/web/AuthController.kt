package com.waytofit.user.adapter.`in`.web

import com.waytofit.global.common.response.ApiResponse
import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import com.waytofit.global.security.jwt.CookieManager
import com.waytofit.user.adapter.`in`.web.dto.AuthTokenRequest
import com.waytofit.user.adapter.`in`.web.dto.AuthTokenResponse
import com.waytofit.user.adapter.`in`.web.dto.ReissueResponse
import com.waytofit.user.application.port.`in`.AuthCommandUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authCommandUseCase: AuthCommandUseCase,
    private val cookieManager: CookieManager,
) {

    @Operation(summary = "인증 코드로 토큰 교환", description = "OAuth2 성공 후 받은 코드를 실제 토큰으로 교환합니다.")
    @PostMapping("/token")
    fun exchangeToken(
        @RequestBody request: AuthTokenRequest,
        servletRequest: HttpServletRequest,
        servletResponse: HttpServletResponse,
    ): ApiResponse<AuthTokenResponse> {
        val deviceInfo = servletRequest.getHeader("User-Agent")
        val (accessToken, refreshToken) = authCommandUseCase.exchangeToken(request.code, deviceInfo)

        cookieManager.setRefreshTokenCookie(servletResponse, refreshToken)
        return ApiResponse.success(AuthTokenResponse(accessToken = accessToken))
    }

    @Operation(summary = "OAuth2 Google 로그인 리다이렉트", description = "이 주소로 접속하면 Google 로그인 페이지로 리다이렉트됩니다.")
    @GetMapping("/login/google")
    fun login(response: HttpServletResponse) {
        response.sendRedirect("/oauth2/authorization/google")
    }

    @Operation(summary = "토큰 재발급", description = "쿠키의 refresh_token을 사용하여 새로운 토큰을 발급합니다.")
    @PostMapping("/reissue")
    fun reissue(
        @CookieValue(name = "refresh_token", required = false) refreshToken: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ApiResponse<ReissueResponse> {
        if (refreshToken.isNullOrBlank()) {
            return ApiResponse.error(ResponseCode.REFRESH_TOKEN_IS_EMPTY)
        }

        val deviceInfo = request.getHeader("User-Agent")
        val (newAccessToken, newRefreshToken) = authCommandUseCase.reissue(refreshToken, deviceInfo)

        cookieManager.setRefreshTokenCookie(response, newRefreshToken)
        
        return ApiResponse.success(ReissueResponse(accessToken = newAccessToken))
    }

    @Operation(summary = "로그아웃", description = "인증 쿠키를 삭제하고 로그아웃 처리합니다.")
    @PostMapping("/logout")
    fun logout(
        @CookieValue(name = "refresh_token", required = false) refreshToken: String?,
        response: HttpServletResponse,
    ): ApiResponse<Unit> {
        if (!refreshToken.isNullOrBlank()) {
            authCommandUseCase.logout(refreshToken)
        }

        cookieManager.clearRefreshTokenCookie(response)
        return ApiResponse.success()
    }
}
