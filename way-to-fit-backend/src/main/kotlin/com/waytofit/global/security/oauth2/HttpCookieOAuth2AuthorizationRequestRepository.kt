package com.waytofit.global.security.oauth2

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import org.springframework.util.SerializationUtils
import java.util.*

@Component
class HttpCookieOAuth2AuthorizationRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    companion object {
        const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
        const val REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"
        private const val COOKIE_EXPIRE_SECONDS = 180
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)?.let {
            deserialize(it.value, OAuth2AuthorizationRequest::class.java)
        }
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (authorizationRequest == null) {
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
            return
        }

        addCookie(
            response,
            OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            serialize(authorizationRequest),
            COOKIE_EXPIRE_SECONDS
        )

        // 클라이언트가 전달한 redirect_uri 파라미터를 쿠키에 저장
        request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME)?.let {
            addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, it, COOKIE_EXPIRE_SECONDS)
        }
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest? {
        return loadAuthorizationRequest(request)
    }

    fun removeAuthorizationRequestCookies(request: HttpServletRequest, response: HttpServletResponse) {
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
    }

    private fun getCookie(request: HttpServletRequest, name: String): Cookie? {
        return request.cookies?.find { it.name == name }
    }

    private fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value).apply {
            path = "/"
            isHttpOnly = true
            this.maxAge = maxAge
        }
        response.addCookie(cookie)
    }

    private fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        request.cookies?.find { it.name == name }?.let {
            it.value = ""
            it.path = "/"
            it.maxAge = 0
            response.addCookie(it)
        }
    }

    private fun serialize(`object`: Any): String {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(`object`))
    }

    private fun <T> deserialize(cookie: String, cls: Class<T>): T {
        return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie)))
    }
}
