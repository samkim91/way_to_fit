package com.waytofit.global.security.oauth2

import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import com.waytofit.user.application.port.`in`.UserCommandUseCase
import com.waytofit.user.domain.enums.OAuthProvider
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val userCommandUseCase: UserCommandUseCase,
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val providerName = userRequest.clientRegistration.registrationId

        val userInfo = extractUserInfo(providerName, oAuth2User.attributes)
        val provider = OAuthProvider.valueOf(providerName.uppercase())

        val user = userCommandUseCase.findOrCreate(
            oauthProvider = provider,
            oauthId = userInfo.oauthId,
            email = userInfo.email,
            name = userInfo.name,
        )

        return AuthenticatedOAuth2User(oAuth2User, user)
    }

    private fun extractUserInfo(
        provider: String,
        attributes: Map<String, Any>,
    ): OAuthUserInfo = when (provider.lowercase()) {
        OAuthProvider.GOOGLE.name.lowercase() -> OAuthUserInfo(
            oauthId = attributes["sub"] as? String ?: throw BusinessException(ResponseCode.OAUTH_PROVIDER_NOT_SUPPORTED, "Google OAuth 'sub' attribute is missing or invalid"),
            email = attributes["email"] as? String,
            name = attributes["name"] as? String ?: "",
        )
        // Kakao, Naver는 provider 크레덴셜 확보 후 추가
        else -> throw BusinessException(ResponseCode.OAUTH_PROVIDER_NOT_SUPPORTED)
    }
}
