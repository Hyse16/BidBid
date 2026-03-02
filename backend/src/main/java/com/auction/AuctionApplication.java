package com.auction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 경매 플랫폼 애플리케이션 진입점
 *
 * @EnableJpaAuditing  - BaseEntity의 createdAt / updatedAt 자동 기록 활성화
 * @EnableScheduling   - 경매 만료 처리 등 스케줄러 기능 활성화
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class AuctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctionApplication.class, args);
    }
}
