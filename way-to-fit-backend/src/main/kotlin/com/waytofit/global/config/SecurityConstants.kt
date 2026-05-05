package com.waytofit.global.config

object SecurityConstants {
    val PUBLIC_URLS = arrayOf(
        "/api/auth/**",
        "/oauth2/**",
        "/login/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    )

    val LOGGING_EXCLUDE_URLS = arrayOf(
        "/swagger-ui",
        "/v3/api-docs",
        "/favicon.ico"
    )
}