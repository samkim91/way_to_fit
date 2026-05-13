package com.waytofit.competition.adapter.`in`.web

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionEntity
import com.waytofit.competition.adapter.out.persistence.entity.CompetitionOrganizerEntity
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionJpaRepository
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionOrganizerJpaRepository
import com.waytofit.competition.domain.BankInfo
import com.waytofit.competition.domain.enums.CompetitionVisibility
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(
    properties = [
        "GOOGLE_CLIENT_ID=test-google-client-id",
        "GOOGLE_CLIENT_SECRET=test-google-client-secret",
        "JWT_SECRET=test-secret-key-with-sufficient-length-for-hs384-signing-1234567890",
        "OAUTH2_REDIRECT_URI=http://localhost:3000/auth/callback"
    ]
)
@Transactional
class CompetitionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var competitionJpaRepository: CompetitionJpaRepository

    @Autowired
    private lateinit var competitionOrganizerJpaRepository: CompetitionOrganizerJpaRepository

    @BeforeEach
    fun setUp() {
        competitionOrganizerJpaRepository.deleteAll()
        competitionJpaRepository.deleteAll()
    }

    @Test
    @WithMockUser(username = ORGANIZER_ID)
    fun `my competitions returns competitions organized by current user`() {
        val competition = competitionJpaRepository.save(
            CompetitionEntity(
                name = "2026 서머 핏",
                description = "테스트 대회",
                startAt = Instant.parse("2026-06-01T00:00:00Z"),
                endAt = Instant.parse("2026-06-02T00:00:00Z"),
                registrationStartAt = Instant.parse("2026-05-01T00:00:00Z"),
                registrationEndAt = Instant.parse("2026-05-20T00:00:00Z"),
                visibility = CompetitionVisibility.PUBLIC,
                bankInfo = BankInfo(
                    bankName = "테스트은행",
                    accountNumber = "123-456",
                    accountHolder = "홍길동",
                    entryFee = 10000
                )
            )
        )
        competitionOrganizerJpaRepository.save(
            CompetitionOrganizerEntity(
                competitionId = competition.id!!,
                userId = UUID.fromString(ORGANIZER_ID)
            )
        )

        mockMvc.perform(get("/api/competitions/my").param("page", "0").param("size", "20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("0000"))
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].id").value(competition.id.toString()))
            .andExpect(jsonPath("$.data.content[0].name").value("2026 서머 핏"))
            .andExpect(jsonPath("$.data.content[0].visibility").value("PUBLIC"))
            .andExpect(jsonPath("$.data.totalElements").value(1))
    }

    @Test
    @WithMockUser(username = OTHER_USER_ID)
    fun `my competitions returns empty page when current user is not an organizer`() {
        val competition = competitionJpaRepository.save(
            CompetitionEntity(
                name = "2026 서머 핏",
                description = "테스트 대회",
                startAt = Instant.parse("2026-06-01T00:00:00Z"),
                endAt = Instant.parse("2026-06-02T00:00:00Z"),
                registrationStartAt = Instant.parse("2026-05-01T00:00:00Z"),
                registrationEndAt = Instant.parse("2026-05-20T00:00:00Z"),
                visibility = CompetitionVisibility.PUBLIC,
                bankInfo = BankInfo(
                    bankName = "테스트은행",
                    accountNumber = "123-456",
                    accountHolder = "홍길동",
                    entryFee = 10000
                )
            )
        )
        competitionOrganizerJpaRepository.save(
            CompetitionOrganizerEntity(
                competitionId = competition.id!!,
                userId = UUID.fromString(ORGANIZER_ID)
            )
        )

        mockMvc.perform(get("/api/competitions/my").param("page", "0").param("size", "20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("0000"))
            .andExpect(jsonPath("$.data.content.length()").value(0))
            .andExpect(jsonPath("$.data.totalElements").value(0))
    }

    companion object {
        private const val ORGANIZER_ID = "7adaa60e-4f60-4b34-ba4f-6a7d6db53931"
        private const val OTHER_USER_ID = "0f0aa88a-0f94-4594-949c-1f10a5b0daef"
    }
}
