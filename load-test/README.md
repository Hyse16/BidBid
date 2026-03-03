# JMeter 동시 입찰 부하 테스트

## 테스트 목적

100명이 동시에 동일 경매에 입찰할 때 **Race Condition 없이 단 하나의 WINNING 입찰만 남는지** 검증한다.
Redis Redisson 분산 락의 동시성 제어 효과를 측정한다.

## 테스트 시나리오

```
동시 사용자: 100명
요청 유형:  POST /api/auctions/{id}/bids
입찰가:     사용자마다 고유한 입찰가 (CSV에서 로드)
Ramp-up:   0초 (완전 동시 실행)
반복 횟수:  1회
```

## 사전 준비

### 1. 테스트용 경매 등록

```bash
# 로그인 후 ACTIVE 상태의 경매를 등록하거나, 스케줄러가 PENDING → ACTIVE 전환 대기
curl -X POST http://localhost:8080/api/auctions \
  -H "Authorization: Bearer {TOKEN}" \
  -F 'request={"categoryId":1,"title":"JMeter 테스트 경매","startPrice":10000,"minBidUnit":100,"startAt":"...","endAt":"..."};type=application/json'
```

### 2. token.csv 생성

100개의 서로 다른 사용자 JWT 토큰과 입찰가를 준비한다.

```csv
JWT_TOKEN,BID_PRICE
eyJhbGci...,10100
eyJhbGci...,10200
eyJhbGci...,10300
...
```

간단한 생성 스크립트:
```bash
python3 scripts/generate_tokens.py --users 100 --base-price 10100 --step 100
```

### 3. 테스트 실행

```bash
# CLI 모드 실행 + HTML 리포트 생성
jmeter -n \
  -t concurrent-bid-test.jmx \
  -l results.jtl \
  -e -o report/ \
  -JAUCTION_ID=1
```

## 검증 포인트

테스트 종료 후 DB에서 다음을 확인한다:

```sql
-- WINNING 입찰이 정확히 1개인지 확인 (Race Condition 없음 검증)
SELECT COUNT(*) FROM bids WHERE auction_item_id = 1 AND status = 'WINNING';
-- 예상 결과: 1

-- 전체 입찰 수
SELECT status, COUNT(*) FROM bids WHERE auction_item_id = 1 GROUP BY status;
-- WINNING: 1, OUTBID: N (입찰 성공한 나머지), FAILED: 0
```

## 예상 결과

| 지표 | 예상값 |
|------|--------|
| WINNING 입찰 수 | 정확히 1 |
| 평균 응답 시간 | < 500ms |
| 오류율 (서버 오류) | 0% |
| BID_LOCK_FAILED 응답 | < 5% (재시도 권장) |

## 결과 분석

```
jmeter -g results.jtl -o report/
open report/index.html
```

주요 확인 항목:
- **Throughput** : 초당 처리 요청 수
- **Response Time** : p50 / p95 / p99 응답 시간
- **Error Rate** : 5xx 오류 비율 (0이어야 정상)
