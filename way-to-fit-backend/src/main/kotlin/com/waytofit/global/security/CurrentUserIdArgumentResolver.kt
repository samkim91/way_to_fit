package com.waytofit.global.security

import com.waytofit.global.util.SecurityUtils
import org.slf4j.MDC
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.UUID

@Component
class CurrentUserIdArgumentResolver : HandlerMethodArgumentResolver {

    companion object {
        private const val USER_ID = "userId"
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUserId::class.java) &&
                parameter.parameterType == UUID::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): UUID? {
        val userIdString = MDC.get(USER_ID) ?: SecurityUtils.getCurrentAuditor() ?: return null
        return UUID.fromString(userIdString)
    }
}
