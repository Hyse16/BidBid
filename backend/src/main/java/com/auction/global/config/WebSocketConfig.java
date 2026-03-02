package com.auction.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket (STOMP 프로토콜) 설정
 *
 * 실시간 입찰 알림을 위해 STOMP over WebSocket을 사용한다.
 * SockJS는 WebSocket을 지원하지 않는 환경에서 폴백(fallback)을 제공한다.
 *
 * 메시지 흐름:
 *   클라이언트 → 서버 : /app/auction/{id}/bid (입찰 요청)
 *   서버 → 클라이언트 : /topic/auction/{id}  (입찰 결과 브로드캐스트)
 *
 * 연결 엔드포인트: /ws (SockJS 호환)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 서버 → 클라이언트 메시지 라우팅 prefix (구독 채널)
        registry.enableSimpleBroker("/topic");
        // 클라이언트 → 서버 메시지 라우팅 prefix (메시지 핸들러로 전달)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 개발 편의 - 프로덕션에서는 도메인 제한 권장
                .withSockJS();                 // SockJS 폴백 활성화
    }
}
