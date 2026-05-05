import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ACCESS_TOKEN_KEY } from '@/lib/constants';

interface UseCompetitionWebSocketOptions {
  competitionId: string;
  stageId?: string;
  eventId?: string;
  onOverallLeaderboardUpdate?: (data: unknown) => void;
  onEventLeaderboardUpdate?: (data: unknown) => void;
}

/**
 * STOMP over SockJS WebSocket 훅
 * - 전체 리더보드: /topic/competitions/{id}/stages/{stageId}/leaderboard
 * - 이벤트별 리더보드: /topic/competitions/{id}/events/{eventId}/leaderboard
 */
export function useCompetitionWebSocket({
  competitionId,
  stageId,
  eventId,
  onOverallLeaderboardUpdate,
  onEventLeaderboardUpdate,
}: UseCompetitionWebSocketOptions) {
  const clientRef = useRef<Client | null>(null);
  const isConnectedRef = useRef(false);

  const getToken = () => localStorage.getItem(ACCESS_TOKEN_KEY);

  const connect = useCallback(() => {
    const token = getToken();
    if (!token) return;

    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

    const client = new Client({
      webSocketFactory: () => new SockJS(`${baseUrl}/ws`),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        isConnectedRef.current = true;

        if (stageId && onOverallLeaderboardUpdate) {
          client.subscribe(
            `/topic/competitions/${competitionId}/stages/${stageId}/leaderboard`,
            (message) => {
              try {
                onOverallLeaderboardUpdate(JSON.parse(message.body));
              } catch (e) {
                console.error('Failed to parse overall leaderboard WS message', e);
              }
            },
          );
        }

        if (eventId && onEventLeaderboardUpdate) {
          client.subscribe(
            `/topic/competitions/${competitionId}/events/${eventId}/leaderboard`,
            (message) => {
              try {
                onEventLeaderboardUpdate(JSON.parse(message.body));
              } catch (e) {
                console.error('Failed to parse event leaderboard WS message', e);
              }
            },
          );
        }
      },
      onDisconnect: () => {
        isConnectedRef.current = false;
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame);
      },
    });

    client.activate();
    clientRef.current = client;
  }, [competitionId, stageId, eventId, onOverallLeaderboardUpdate, onEventLeaderboardUpdate]);

  useEffect(() => {
    connect();
    return () => {
      clientRef.current?.deactivate();
      clientRef.current = null;
    };
  }, [connect]);

  return { isConnectedRef };
}
