package com.auction.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리 설정
 *
 * @Async 어노테이션이 붙은 메서드는 이 설정의 스레드 풀에서 실행된다.
 * 주로 텔레그램 알림 전송에 사용하여 메인 스레드를 블로킹하지 않는다.
 *
 * 스레드 풀 설정:
 *   - corePoolSize  : 항상 유지할 스레드 수 (2개)
 *   - maxPoolSize   : 큐가 꽉 찼을 때 최대 확장 수 (10개)
 *   - queueCapacity : 스레드가 모두 사용 중일 때 대기할 요청 수 (100개)
 *   - 종료 시 최대 30초 동안 진행 중인 작업 완료를 기다린다
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 스레드 풀 크기 설정
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);

        // 스레드 이름 및 종료 정책
        executor.setThreadNamePrefix("async-");              // 로그에서 스레드 식별용
        executor.setWaitForTasksToCompleteOnShutdown(true);  // 종료 시 진행 중 작업 완료 대기
        executor.setAwaitTerminationSeconds(30);             // 최대 대기 시간 (초)

        executor.initialize();
        return executor;
    }
}
