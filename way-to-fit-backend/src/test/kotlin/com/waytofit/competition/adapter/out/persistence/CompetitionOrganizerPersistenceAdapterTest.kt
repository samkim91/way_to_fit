package com.waytofit.competition.adapter.out.persistence

import com.waytofit.competition.adapter.out.persistence.entity.CompetitionOrganizerEntity
import com.waytofit.competition.adapter.out.persistence.repository.CompetitionOrganizerJpaRepository
import com.waytofit.global.config.JpaConfig
import com.waytofit.global.config.QueryDslConfig
import com.waytofit.global.persistence.AuditorAwareImpl
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig::class, JpaConfig::class)
class CompetitionOrganizerPersistenceAdapterTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun auditorAwareImpl(): AuditorAwareImpl = AuditorAwareImpl()
    }

    @Autowired
    private lateinit var jpaRepository: CompetitionOrganizerJpaRepository

    @Test
    fun `isOrganizer returns true when organizer exists`() {
        val adapter = CompetitionOrganizerPersistenceAdapter(jpaRepository)
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        jpaRepository.save(CompetitionOrganizerEntity(competitionId = competitionId, userId = userId))

        assertTrue(adapter.isOrganizer(competitionId, userId))
    }

    @Test
    fun `isOrganizer returns false when organizer does not exist`() {
        val adapter = CompetitionOrganizerPersistenceAdapter(jpaRepository)
        val competitionId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        assertFalse(adapter.isOrganizer(competitionId, userId))
    }
}
