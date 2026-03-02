"use client";

import { useEffect, useRef, useState } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { BidBroadcastMessage } from "@/types/bid";

const WS_URL = process.env.NEXT_PUBLIC_WS_URL || "http://localhost:8080";

/**
 * WebSocket STOMP 연결 훅
 *
 * 특정 경매 ID를 구독하고, 새 입찰이 들어오면 콜백을 호출한다.
 * 컴포넌트 언마운트 시 연결을 자동으로 해제한다.
 *
 * @param auctionId 구독할 경매 ID
 * @param onBidUpdate 새 입찰 메시지 수신 콜백
 */
export function useWebSocket(
  auctionId: number,
  onBidUpdate: (message: BidBroadcastMessage) => void
) {
  const clientRef = useRef<Client | null>(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () =>
        new SockJS(`${WS_URL}/ws`) as unknown as WebSocket,

      onConnect: () => {
        setConnected(true);

        // 특정 경매 토픽 구독
        client.subscribe(`/topic/auction/${auctionId}`, (message: IMessage) => {
          const payload: BidBroadcastMessage = JSON.parse(message.body);
          onBidUpdate(payload);
        });
      },

      onDisconnect: () => {
        setConnected(false);
      },

      onStompError: (frame) => {
        console.error("STOMP 오류:", frame.headers["message"]);
      },

      reconnectDelay: 5000, // 연결 끊김 시 5초 후 재연결
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [auctionId, onBidUpdate]);

  return { connected };
}
