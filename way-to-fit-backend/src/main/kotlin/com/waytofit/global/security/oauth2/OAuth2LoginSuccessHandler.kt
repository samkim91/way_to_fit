package com.waytofit.global.security.oauth2

import com.waytofit.competition.application.port.`in`.CreateAthleteProfileUseCase
import com.waytofit.global.security.jwt.CookieManager
import com.waytofit.user.application.port.`in`.AuthCommandUseCase
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2LoginSuccessHandler(
    private val authCommandUseCase: AuthCommandUseCase,
    private val createAthleteProfileUseCase: CreateAthleteProfileUseCase,
    private val authorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    @param:Value($$"${oauth2.redirect-uri:/oauth2/callback}") private val defaultRedirectUri: String,
    @param:Value($$"${oauth2.allowed-redirect-uris:}") private val allowedRedirectUris: List<String>,
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val principal = authentication.principal as AuthenticatedOAuth2User
        val user = principal.user

        // 1. 선수 프로필 자동 생성 (없을 경우)
        user.id?.let { createAthleteProfileUseCase.createProfileIfNotExists(it) }

        // 2. 일회성 인증 코드 생성
        val authCode = authCommandUseCase.generateAuthCode(user)

        // 3. 쿠키에서 클라이언트가 요청했던 redirect_uri 추출 (화이트리스트 검증 포함)
        val targetUri = getTargetUri(request)

        // 4. 리다이렉트 (코드 포함)
        val redirectUrl = "$targetUri?code=$authCode"
        
        // 5. 인증 관련 쿠키 삭제
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
        
        clearAuthenticationAttributes(request)
        response.sendRedirect(redirectUrl)
    }

    private fun getTargetUri(request: HttpServletRequest): String {
        val targetUri = request.cookies?.find { it.name == HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME }?.value
            ?: return defaultRedirectUri

        // 화이트리스트 검증: 비어있지 않은 경우에만 검증 수행
        if (allowedRedirectUris.isNotEmpty() && !allowedRedirectUris.contains(targetUri)) {
            logger.warn("Not allowed redirect_uri requested: $targetUri. Fallback to default.")
            return defaultRedirectUri
        }

        return targetUri
    }
}
