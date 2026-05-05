package com.waytofit.competition.adapter.`in`.websocket

import com.waytofit.competition.adapter.`in`.web.dto.EventLeaderboardResponse
import com.waytofit.competition.adapter.`in`.web.dto.OverallLeaderboardResponse
import com.waytofit.competition.application.port.`in`.GetEventLeaderboardQuery
import com.waytofit.competition.application.port.`in`.GetEventLeaderboardUseCase
import com.waytofit.competition.application.port.`in`.GetOverallLeaderboardQuery
import com.waytofit.competition.application.port.`in`.GetOverallLeaderboardUseCase
import com.waytofit.competition.domain.event.ScoreReviewedEvent
import org.springframework.cache.CacheManager
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class LeaderboardEventListener(
    private val messagingTemplate: SimpMessagingTemplate,
    private val getEventLeaderboardUseCase: GetEventLeaderboardUseCase,
    private val getOverallLeaderboardUseCase: GetOverallLeaderboardUseCase,
    private val cacheManager: CacheManager,
) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleScoreReviewedEvent(event: ScoreReviewedEvent) {
        // 0. 리더보드 캐시 무효화
        cacheManager.getCache("eventLeaderboard")?.clear()
        cacheManager.getCache("overallLeaderboard")?.clear()

        // 1. 이벤트별 리더보드 갱신 데이터 전송
        val eventResult = getEventLeaderboardUseCase.getEventLeaderboard(
            GetEventLeaderboardQuery(event.eventId, null, null)
        )
        val eventDestination = "/topic/competitions/${event.competitionId}/events/${event.eventId}/leaderboard"
        messagingTemplate.convertAndSend(eventDestination, EventLeaderboardResponse.fromResult(eventResult))

        // 2. 종합 리더보드 갱신 데이터 전송
        val overallResult = getOverallLeaderboardUseCase.getOverallLeaderboard(
            GetOverallLeaderboardQuery(event.stageId, null, null, null)
        )
        val overallDestination = "/topic/competitions/${event.competitionId}/stages/${event.stageId}/leaderboard"
        messagingTemplate.convertAndSend(overallDestination, OverallLeaderboardResponse.fromResult(overallResult))
    }
}
