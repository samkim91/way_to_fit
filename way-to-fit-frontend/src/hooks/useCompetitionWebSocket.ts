import { useEffect, useRef, useCallback, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ACCESS_TOKEN_KEY } from '@/lib/constants';
import type { EventLeaderboardResponse, OverallLeaderboardResponse } from '@/features/competition/types';

interface UseCompetitionWebSocketOptions {
  competitionId: string;
  stageId?: string;
  eventId?: string;
  onOverallLeaderboardUpdate?: (data: OverallLeaderboardResponse) => void;
  onEventLeaderboardUpdate?: (data: EventLeaderboardResponse) => void;
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
  const [isConnected, setIsConnected] = useState(false);

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
        setIsConnected(true);

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
        setIsConnected(false);
      },
      onStompError: (frame) => {
        setIsConnected(false);
        console.error('STOMP error', frame);
      },
      onWebSocketClose: () => {
        setIsConnected(false);
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

  return { isConnected };
}
