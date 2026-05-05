package com.waytofit.user.application.service

import com.waytofit.global.common.response.ResponseCode
import com.waytofit.global.error.BusinessException
import com.waytofit.global.security.jwt.JwtTokenProvider
import com.waytofit.user.application.port.`in`.AuthCommandUseCase
import com.waytofit.user.application.port.out.AuthCodePersistencePort
import com.waytofit.user.application.port.out.RefreshTokenPersistencePort
import com.waytofit.user.application.port.out.UserPersistencePort
import com.waytofit.user.domain.AuthCode
import com.waytofit.user.domain.RefreshToken
import com.waytofit.user.domain.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuthCommandService(
    private val userRepository: UserPersistencePort,
    private val refreshTokenRepository: RefreshTokenPersistencePort,
    private val authCodeRepository: AuthCodePersistencePort,
    private val jwtTokenProvider: JwtTokenProvider,
) : AuthCommandUseCase {

    @Transactional
    override fun generateAuthCode(user: User): String {
        val authCode = AuthCode.create(user.id!!)
        authCodeRepository.save(authCode)
        return authCode.code
    }

    @Transactional
    override fun exchangeToken(code: String, deviceInfo: String?): Pair<String, String> {
        val authCode = authCodeRepository.findByCode(code)
            ?: throw BusinessException(ResponseCode.AUTH_CODE_NOT_FOUND)

        if (authCode.isExpired(Instant.now())) {
            authCodeRepository.deleteByCode(code)
            throw BusinessException(ResponseCode.AUTH_CODE_EXPIRED)
        }

        val user = userRepository.findById(authCode.userId)
            ?: throw BusinessException(ResponseCode.USER_NOT_FOUND)

        // 코드는 1회성이므로 즉시 삭제
        authCodeRepository.deleteByCode(code)

        return login(user, deviceInfo)
    }

    @Transactional
    override fun login(user: User, deviceInfo: String?): Pair<String, String> {
        val userIdStr =
            if (user.id == null) throw BusinessException(ResponseCode.USER_ID_MUST_NOT_BE_NULL) else user.id.toString()
        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = userIdStr,
            email = user.email,
            name = user.name,
            role = user.role,
        )
        val refreshToken = jwtTokenProvider.generateRefreshToken(userIdStr)
        val expiresAt = jwtTokenProvider.getExpirationAsInstant(refreshToken)

        refreshTokenRepository.deleteByUserId(user.id)
        refreshTokenRepository.deleteExpiredByUserId(user.id, Instant.now())
        refreshTokenRepository.save(
            RefreshToken.create(
                userId = user.id,
                token = refreshToken,
                deviceInfo = deviceInfo,
                expiresAt = expiresAt,
            ),
        )

        return Pair(accessToken, refreshToken)
    }

    @Transactional
    override fun reissue(refreshToken: String, deviceInfo: String?): Pair<String, String> {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BusinessException(ResponseCode.REFRESH_TOKEN_INVALID)
        }

        val stored = refreshTokenRepository.findByToken(refreshToken)
            ?: throw BusinessException(ResponseCode.REFRESH_TOKEN_NOT_FOUND)

        if (stored.isExpired(Instant.now())) {
            refreshTokenRepository.deleteByToken(refreshToken)
            throw BusinessException(ResponseCode.REFRESH_TOKEN_INVALID)
        }

        val user = userRepository.findById(stored.userId)
            ?: throw BusinessException(ResponseCode.USER_NOT_FOUND)
        val userId = user.id ?: throw BusinessException(ResponseCode.USER_ID_MUST_NOT_BE_NULL)

        refreshTokenRepository.deleteByToken(refreshToken)

        val userIdStr = userId.toString()
        val newAccessToken = jwtTokenProvider.generateAccessToken(
            userId = userIdStr,
            email = user.email,
            name = user.name,
            role = user.role,
        )
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(userIdStr)
        val expiresAt = jwtTokenProvider.getExpirationAsInstant(newRefreshToken)

        refreshTokenRepository.save(
            RefreshToken.create(
                userId = userId,
                token = newRefreshToken,
                deviceInfo = deviceInfo ?: stored.deviceInfo,
                expiresAt = expiresAt,
            ),
        )

        return Pair(newAccessToken, newRefreshToken)
    }

    @Transactional
    override fun logout(refreshToken: String) {
        refreshTokenRepository.deleteByToken(refreshToken)
    }
}
