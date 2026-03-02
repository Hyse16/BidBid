package com.auction.infra.redis;

import com.auction.global.exception.CustomException;
import com.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson 기반 분산 락 서비스
 *
 * 동시에 여러 사용자가 같은 경매에 입찰할 때 발생하는 Race Condition을 방지한다.
 * Redis의 분산 락을 사용하여 특정 경매에 대한 처리가 동시에 하나만 실행되도록 보장한다.
 *
 * 락 키 예시: "auction:bid:{auctionId}"
 *
 * 기본 설정:
 *   - waitTime  : 3초 → 락 획득 대기 시간 (초과 시 BID_LOCK_FAILED 예외)
 *   - leaseTime : 5초 → 락 자동 해제 시간 (데드락 방지)
 *
 * 주의: tryLock은 waitTime 내 락 획득에 실패하면 즉시 false를 반환한다.
 *       lock.isHeldByCurrentThread() 확인 후 unlock을 호출하여 다른 스레드의 락을 해제하지 않도록 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedissonLockService {

    private final RedissonClient redissonClient;

    private static final long DEFAULT_WAIT_TIME  = 3L; // 락 획득 대기 시간 (초)
    private static final long DEFAULT_LEASE_TIME = 5L; // 락 자동 해제 시간 (초)

    /**
     * 기본 설정으로 분산 락을 획득하고 작업을 실행한다. (반환값 있음)
     *
     * @param lockKey 락 식별 키
     * @param task    락 안에서 실행할 작업
     * @return 작업 실행 결과
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> task) {
        return executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, task);
    }

    /**
     * 커스텀 설정으로 분산 락을 획득하고 작업을 실행한다. (반환값 있음)
     *
     * @param lockKey   락 식별 키
     * @param waitTime  락 획득 대기 시간 (초)
     * @param leaseTime 락 자동 해제 시간 (초)
     * @param task      락 안에서 실행할 작업
     * @return 작업 실행 결과
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, Supplier<T> task) {
        RLock   lock     = redissonClient.getLock(lockKey);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);

            if (!acquired) {
                // 대기 시간 내 락 획득 실패 → 클라이언트에게 재시도 요청
                throw new CustomException(ErrorCode.BID_LOCK_FAILED);
            }

            return task.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            throw new CustomException(ErrorCode.BID_LOCK_FAILED);

        } finally {
            // 현재 스레드가 락을 보유한 경우에만 해제 (다른 스레드 락 해제 방지)
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 기본 설정으로 분산 락을 획득하고 작업을 실행한다. (반환값 없음)
     *
     * @param lockKey 락 식별 키
     * @param task    락 안에서 실행할 작업
     */
    public void executeWithLock(String lockKey, Runnable task) {
        executeWithLock(lockKey, () -> {
            task.run();
            return null;
        });
    }
}
