package com.waytofit.global.config

import com.waytofit.global.security.jwt.JwtAuthenticationFilter
import com.waytofit.global.security.oauth2.CustomOAuth2UserService
import com.waytofit.global.security.oauth2.OAuth2LoginSuccessHandler
import com.waytofit.global.security.CustomAccessDeniedHandler
import com.waytofit.global.security.CustomAuthenticationEntryPoint
import com.waytofit.global.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
    private val httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    private val corsProperties: CorsProperties,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            // OAuth2 인가 코드 플로우는 세션이 필요하므로 IF_REQUIRED 유지
            // API 요청은 JWT로만 인증 (JwtAuthenticationFilter)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
            .authorizeHttpRequests {
                it.requestMatchers(*SecurityConstants.PUBLIC_URLS).permitAll()
                it.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/competitions/my").authenticated()
                it.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/competitions/**").permitAll()
                it.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/athletes/**").permitAll()
                it.requestMatchers("/api/v1/super-admin/**").hasRole("SUPER_ADMIN")
                it.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(customAuthenticationEntryPoint)
                it.accessDeniedHandler(customAccessDeniedHandler)
            }
            .oauth2Login {
                it.authorizationEndpoint { endpoint ->
                    endpoint.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                }
                it.userInfoEndpoint { endpoint ->
                    endpoint.userService(customOAuth2UserService)
                }
                it.successHandler(oAuth2LoginSuccessHandler)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            this.allowedOrigins = corsProperties.allowedOrigins
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
