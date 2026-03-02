package com.auction.infra.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 텔레그램 봇 알림 서비스
 *
 * 입찰, 낙찰, 패배, 경매 마감 임박 등의 이벤트가 발생하면
 * 사용자의 텔레그램으로 알림 메시지를 전송한다.
 *
 * @Async를 통해 비동기로 실행되므로 메인 비즈니스 로직을 블로킹하지 않는다.
 * 전송 실패 시 예외를 던지지 않고 경고 로그만 남겨 서비스 안정성을 유지한다.
 *
 * 알림 타입별 메시지 형식 (HTML parse_mode):
 *   BID            → "📈 New bid on [item]! Current price: 75,000원"
 *   OUTBID         → "⚠️ You've been outbid on [item]! Current: 80,000원"
 *   WIN            → "🎉 Congratulations! You won [item] for 95,000원"
 *   LOSE           → "😢 You didn't win [item]. Final price: 95,000원"
 *   EXPIRY_WARNING → "⏰ [item] ends in 1 hour! Current: 80,000원"
 *
 * 사전 조건: 사용자가 telegramChatId를 프로필에 등록해야 알림을 받을 수 있다.
 */
@Slf4j
@Service
public class TelegramService {

    @Value("${telegram.bot-token}")
    private String botToken; // 텔레그램 BotFather에서 발급받은 봇 토큰

    private final RestClient restClient = RestClient.create();

    /**
     * 텔레그램 메시지 비동기 전송
     *
     * @param chatId  수신자의 텔레그램 Chat ID (사용자 프로필에 저장된 값)
     * @param message HTML 형식의 메시지 본문
     */
    @Async("asyncExecutor") // AsyncConfig에 등록된 스레드 풀 사용
    public void sendMessage(String chatId, String message) {
        // Chat ID가 없으면 텔레그램 미등록 사용자이므로 전송 생략
        if (chatId == null || chatId.isBlank()) {
            return;
        }

        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            restClient.post()
                    .uri(url)
                    .body(new SendMessageRequest(chatId, message, "HTML"))
                    .retrieve()
                    .toBodilessEntity();

            log.debug("Telegram message sent to chatId={}", chatId);

        } catch (Exception e) {
            // 전송 실패는 경고 로그만 남기고 예외를 전파하지 않음
            // → 알림 실패가 서비스 장애로 이어지지 않도록 격리
            log.warn("Failed to send Telegram message to chatId={}: {}", chatId, e.getMessage());
        }
    }

    /** 텔레그램 sendMessage API 요청 바디 */
    private record SendMessageRequest(
            String chat_id,    // 수신자 Chat ID
            String text,       // 메시지 내용
            String parse_mode  // 메시지 형식 ("HTML" 또는 "Markdown")
    ) {}
}
