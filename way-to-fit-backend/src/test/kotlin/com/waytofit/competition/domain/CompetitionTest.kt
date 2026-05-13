package com.waytofit.competition.domain

import com.waytofit.competition.domain.enums.CompetitionLifecycle
import com.waytofit.competition.domain.enums.CompetitionVisibility
import com.waytofit.global.common.response.ResponseCode
import org.assertj.core.api.Assertions.assertThat
import com.waytofit.global.error.BusinessException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class CompetitionTest {

    private val now = Instant.now()
    private val bankInfo = BankInfo("Bank", "123", "Holder", 10000)

    @Test
    fun `정상적인 날짜 설정으로 대회 생성 성공`() {
        Competition(
            name = "Test",
            description = "Desc",
            startAt = now.plus(10, ChronoUnit.DAYS),
            endAt = now.plus(11, ChronoUnit.DAYS),
            registrationStartAt = now.plus(1, ChronoUnit.DAYS),
            registrationEndAt = now.plus(5, ChronoUnit.DAYS),
            visibility = CompetitionVisibility.PRIVATE,
            bankInfo = bankInfo
        )
    }

    @Test
    fun `신청 시작일이 종료일보다 늦으면 예외 발생`() {
        assertThatThrownBy {
            Competition(
                name = "Test",
                description = "Desc",
                startAt = now.plus(10, ChronoUnit.DAYS),
                endAt = now.plus(11, ChronoUnit.DAYS),
                registrationStartAt = now.plus(5, ChronoUnit.DAYS),
                registrationEndAt = now.plus(1, ChronoUnit.DAYS),
                visibility = CompetitionVisibility.PRIVATE,
                bankInfo = bankInfo
            )
        }.isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("responseCode", ResponseCode.COMPETITION_REGISTRATION_DATE_INVALID)
    }

    @Test
    fun `대회 시작일이 종료일보다 늦으면 예외 발생`() {
        assertThatThrownBy {
            Competition(
                name = "Test",
                description = "Desc",
                startAt = now.plus(11, ChronoUnit.DAYS),
                endAt = now.plus(10, ChronoUnit.DAYS),
                registrationStartAt = now.plus(1, ChronoUnit.DAYS),
                registrationEndAt = now.plus(5, ChronoUnit.DAYS),
                visibility = CompetitionVisibility.PRIVATE,
                bankInfo = bankInfo
            )
        }.isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("responseCode", ResponseCode.COMPETITION_PERIOD_INVALID)
    }

    @Test
    fun `신청 마감일이 대회 종료일보다 늦으면 예외 발생`() {
        assertThatThrownBy {
            Competition(
                name = "Test",
                description = "Desc",
                startAt = now.plus(10, ChronoUnit.DAYS),
                endAt = now.plus(11, ChronoUnit.DAYS),
                registrationStartAt = now.plus(1, ChronoUnit.DAYS),
                registrationEndAt = now.plus(12, ChronoUnit.DAYS),
                visibility = CompetitionVisibility.PRIVATE,
                bankInfo = bankInfo
            )
        }.isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("responseCode", ResponseCode.COMPETITION_REGISTRATION_END_INVALID)
    }

    @Test
    fun `신청 마감일이 대회 시작일보다 늦어도 대회 종료일 이전이면 허용`() {
        Competition(
            name = "Test",
            description = "Desc",
            startAt = now.plus(10, ChronoUnit.DAYS),
            endAt = now.plus(15, ChronoUnit.DAYS),
            registrationStartAt = now.plus(1, ChronoUnit.DAYS),
            registrationEndAt = now.plus(12, ChronoUnit.DAYS),
            visibility = CompetitionVisibility.PRIVATE,
            bankInfo = bankInfo
        )
    }

    @Test
    fun `신청 기간과 대회 기간이 겹쳐도 시작 이후에는 진행중 상태가 우선이다`() {
        val competition = Competition(
            name = "Overlap",
            description = "Desc",
            startAt = now.plus(1, ChronoUnit.DAYS),
            endAt = now.plus(5, ChronoUnit.DAYS),
            registrationStartAt = now.minus(1, ChronoUnit.DAYS),
            registrationEndAt = now.plus(2, ChronoUnit.DAYS),
            visibility = CompetitionVisibility.PUBLIC,
            bankInfo = bankInfo
        )

        assertThat(competition.lifecycleAt(now.plus(36, ChronoUnit.HOURS)))
            .isEqualTo(CompetitionLifecycle.IN_PROGRESS)
    }
}
