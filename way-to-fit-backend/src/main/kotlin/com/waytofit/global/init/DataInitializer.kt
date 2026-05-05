package com.waytofit.global.init

import com.waytofit.user.adapter.out.persistence.entity.UserEntity
import com.waytofit.user.adapter.out.persistence.repository.UserJpaRepository
import com.waytofit.user.domain.User
import com.waytofit.user.domain.enums.OAuthProvider
import com.waytofit.user.domain.enums.UserRole
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("local")
@Order(1)
class DataInitializer(
    private val userJpaRepository: UserJpaRepository,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val SEED_OAUTH_ID = "dummy-superadmin-011"
        private val SEED_OAUTH_PROVIDER = OAuthProvider.GOOGLE
    }

    @Transactional
    override fun run(args: ApplicationArguments) {
        val adminInitialized =
            userJpaRepository.findByOauthProviderAndOauthId(SEED_OAUTH_PROVIDER, SEED_OAUTH_ID) != null

        if (!adminInitialized) {
            log.info("[DataInitializer] 슈퍼 어드민 초기화를 시작합니다.")
            saveSuperAdmin()
        }

        log.info("[DataInitializer] 더미 데이터 초기화 완료.")
    }

    private fun saveSuperAdmin() {
        val superAdmin = User.create(
            oauthProvider = OAuthProvider.GOOGLE,
            oauthId = SEED_OAUTH_ID,
            email = "superadmin@waytofit.local",
            name = "슈퍼어드민",
        ).copy(role = UserRole.SUPER_ADMIN)

        val saved = userJpaRepository.save(UserEntity.fromDomain(superAdmin))
        log.info("[DataInitializer] SUPER_ADMIN '{}' 생성 완료", saved.name)
    }
}
