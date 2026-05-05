package com.waytofit.competition.adapter.`in`.websocket

import com.waytofit.competition.application.port.`in`.EventLeaderboardResult
import com.waytofit.competition.application.port.`in`.GetEventLeaderboardUseCase
import com.waytofit.competition.application.port.`in`.GetOverallLeaderboardUseCase
import com.waytofit.competition.application.port.`in`.OverallLeaderboardResult
import com.waytofit.competition.domain.enums.ScoreStatus
import com.waytofit.competition.domain.event.ScoreReviewedEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.util.UUID

class LeaderboardEventListenerTest {

    private val messagingTemplate = mock(SimpMessagingTemplate::class.java)
    private val getEventLeaderboardUseCase = mock(GetEventLeaderboardUseCase::class.java)
    private val getOverallLeaderboardUseCase = mock(GetOverallLeaderboardUseCase::class.java)
    private val cacheManager = mock(org.springframework.cache.CacheManager::class.java)
    private val listener = LeaderboardEventListener(
        messagingTemplate, getEventLeaderboardUseCase, getOverallLeaderboardUseCase, cacheManager
    )

    @Test
    fun `handleScoreReviewedEvent sends leaderboard data to topics`() {
        val competitionId = UUID.randomUUID()
        val stageId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val scoreId = UUID.randomUUID()

        val event = ScoreReviewedEvent(
            competitionId = competitionId,
            stageId = stageId,
            eventId = eventId,
            scoreId = scoreId,
            status = ScoreStatus.APPROVED
        )

        `when`(getEventLeaderboardUseCase.getEventLeaderboard(any())).thenReturn(
            EventLeaderboardResult(eventId = eventId, entries = emptyList())
        )
        `when`(getOverallLeaderboardUseCase.getOverallLeaderboard(any())).thenReturn(
            OverallLeaderboardResult(stageId = stageId, entries = emptyList())
        )

        listener.handleScoreReviewedEvent(event)

        verify(messagingTemplate).convertAndSend(
            eq("/topic/competitions/$competitionId/events/$eventId/leaderboard"), 
            any(Any::class.java)
        )
        verify(messagingTemplate).convertAndSend(
            eq("/topic/competitions/$competitionId/stages/$stageId/leaderboard"), 
            any(Any::class.java)
        )
    }

    private fun <T> any(): T = org.mockito.ArgumentMatchers.any()
}
