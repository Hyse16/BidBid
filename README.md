# BidBid — 실시간 경매 플랫폼

> 포트폴리오 목적으로 제작된 실시간 경매 플랫폼입니다.
> Redis 분산 락으로 동시성을 제어하고, WebSocket STOMP로 입찰 결과를 실시간 브로드캐스트합니다.

---

## 아키텍처

```
                         ┌─────────────────────────────────────────────┐
                         │              AWS (Production)                │
                         │                                              │
  User Browser ──HTTPS──▶│  Nginx (Reverse Proxy + SSL)                │
  User Browser ──WSS────▶│    /api/**  →  Backend:8080                 │
                         │    /ws/**   →  Backend:8080 (WebSocket)      │
                         │    /        →  Frontend:3000                 │
                         │                                              │
                         │  ┌──────────────┐  ┌──────────────────────┐ │
                         │  │  Next.js 14  │  │  Spring Boot 3.3     │ │
                         │  │  (Frontend)  │  │  (Backend)           │ │
                         │  └──────────────┘  └──────────┬─────────┬─┘ │
                         │                               │         │   │
                         │  ┌─────────────┐  ┌──────────┴─┐  ┌────┴─┐ │
                         │  │  Prometheus │  │ PostgreSQL  │  │Redis │ │
                         │  │  + Grafana  │  │ (RDS)       │  │      │ │
                         │  └─────────────┘  └────────────┘  └──────┘ │
                         │                               │             │
                         │                          ┌────┴────┐        │
                         │                          │  AWS S3 │        │
                         │                          └─────────┘        │
                         └─────────────────────────────────────────────┘
```

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Java 17, Spring Boot 3.3, Spring Security, JPA, QueryDSL |
| 인증 | JWT (Access 1h + Refresh 7d), BCrypt |
| 실시간 | WebSocket STOMP, SockJS |
| 동시성 | Redis Redisson 분산 락 |
| 캐싱 | Spring Cache + Redis (auctions 30s, auction 5분) |
| DB | PostgreSQL 15 (JPA 자동 DDL) |
| 이미지 | AWS S3 |
| 알림 | Telegram Bot API (비동기) |
| Frontend | Next.js 14 App Router, TypeScript, Tailwind CSS |
| 상태 관리 | Zustand (클라이언트), TanStack Query (서버 상태) |
| 모니터링 | Prometheus + Grafana |
| CI/CD | GitHub Actions → AWS ECR → EC2 Docker Compose |

---

## 핵심 구현 포인트

### 1. 동시성 제어 — Redis Redisson 분산 락

100명이 동시에 같은 경매에 입찰해도 Race Condition 없이 단 하나의 WINNING 입찰만 생성됩니다.

```java
// RedissonLockService.executeWithLock("auction:bid:{auctionId}", () -> {
//   1. 현재 최고 입찰 조회
//   2. 입찰가 검증 (현재가 초과 + 최소 단위 충족)
//   3. 기존 WINNING → OUTBID 처리
//   4. 새 WINNING 입찰 저장
//   5. currentPrice 업데이트 + WebSocket 브로드캐스트
// })
```

### 2. 실시간 입찰 흐름

```
입찰자       → POST /api/auctions/{id}/bids
백엔드       → Redis 락 획득 → 입찰 처리 → 락 해제
백엔드       → STOMP BROADCAST /topic/auction/{id}
전체 구독자   → 현재가·입찰자·시각 실시간 수신
```

### 3. Telegram 알림 (비동기)

| 이벤트 | 알림 |
|--------|------|
| 새 입찰 | `📈 [상품]에 새 입찰! 현재가: 75,000원` |
| 내 입찰 추월 | `⚠️ [상품]에서 더 높은 입찰이 들어왔습니다! 현재: 80,000원` |
| 경매 낙찰 | `🎉 축하합니다! [상품] 낙찰가: 95,000원` |
| 경매 종료 1시간 전 | `⏰ [상품] 1시간 후 종료! 현재: 80,000원` |

---

## 로컬 실행 방법

### 사전 요구 사항

- Java 17+
- Docker Desktop
- Node.js 18+

### 1. 인프라 실행 (PostgreSQL + Redis)

```bash
docker-compose up -d postgres redis
```

### 2. 백엔드 실행

```bash
# 환경 변수 설정 (application-dev.yml 참조)
export JWT_SECRET=your-secret-key-at-least-32-chars
export AWS_ACCESS_KEY=your-key
export AWS_SECRET_KEY=your-secret
export S3_BUCKET=your-bucket
export TELEGRAM_BOT_TOKEN=your-token  # 선택 사항

cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

백엔드 실행 후 `http://localhost:8080/actuator/health` 에서 상태 확인

### 3. 프론트엔드 실행

```bash
cd frontend
cp .env.example .env.local  # 환경 변수 파일 생성
npm install
npm run dev
```

브라우저에서 `http://localhost:3000` 접속

### 4. 전체 스택 Docker Compose (선택)

```bash
docker-compose up --build
```

---

## API 엔드포인트

### 인증

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 발급) |
| POST | `/api/auth/refresh` | 토큰 갱신 |
| POST | `/api/auth/logout` | 로그아웃 (Refresh Token 무효화) |

### 경매

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| GET | `/api/auctions` | 불필요 | 목록 조회 (페이징, 카테고리/상태 필터) |
| GET | `/api/auctions/{id}` | 불필요 | 상세 조회 |
| POST | `/api/auctions` | 필요 | 경매 등록 (multipart/form-data) |
| PUT | `/api/auctions/{id}` | 필요 (소유자) | 경매 수정 |
| DELETE | `/api/auctions/{id}` | 필요 (소유자) | 경매 삭제 |

### 입찰

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| POST | `/api/auctions/{id}/bids` | 필요 | 입찰 |
| GET | `/api/auctions/{id}/bids` | 불필요 | 입찰 히스토리 조회 |

### 관심 목록

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| POST | `/api/watchlist/{auctionId}` | 필요 | 관심 등록 |
| DELETE | `/api/watchlist/{auctionId}` | 필요 | 관심 취소 |
| GET | `/api/watchlist` | 필요 | 내 관심 목록 |

### 내 정보

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/users/me` | 내 프로필 조회 |
| PUT | `/api/users/me` | 프로필 수정 |
| GET | `/api/users/me/auctions` | 내 경매 목록 |
| GET | `/api/users/me/bids` | 내 입찰 내역 |

### WebSocket (STOMP)

```
# 구독: 경매 실시간 입찰 업데이트
SUBSCRIBE /topic/auction/{id}

# 응답 페이로드 예시
{
  "auctionId": 1,
  "currentPrice": 85000,
  "bidderNickname": "입찰자",
  "bidPrice": 85000,
  "bidAt": "2024-03-15T14:30:00"
}
```

---

## 부하 테스트 (JMeter)

100명 동시 입찰로 분산 락 효과를 검증합니다.

```bash
# 1. 토큰 CSV 생성
python3 load-test/scripts/generate_tokens.py --users 100 --base-price 10100 --step 100

# 2. 테스트 실행
jmeter -n \
  -t load-test/concurrent-bid-test.jmx \
  -l load-test/results.jtl \
  -e -o load-test/report/ \
  -JAUCTION_ID=1

# 3. 결과 확인 (DB)
# SELECT COUNT(*) FROM bids WHERE auction_item_id = 1 AND status = 'WINNING';
# 예상 결과: 1 (Race Condition 없음)
```

자세한 내용: [load-test/README.md](load-test/README.md)

---

## 모니터링

Grafana 대시보드: `http://localhost:3001` (admin / admin)

주요 지표:

- **API 응답 시간** p50 / p95 / p99
- **초당 입찰 수** (Bids per second)
- **WebSocket 활성 연결 수**
- **Redis 캐시 히트율** (목표: 80% 이상)
- **JVM 힙 메모리 / GC 활동**
- **DB 커넥션 풀 사용량** (HikariCP)

---

## 프로젝트 구조

```
BidBid/
├── backend/                    # Spring Boot 애플리케이션
│   └── src/
│       ├── main/java/com/auction/
│       │   ├── domain/         # 도메인 레이어 (auction, bid, user, ...)
│       │   ├── global/         # 공통 (config, exception, security, ...)
│       │   └── infra/          # 외부 연동 (redis, s3, telegram)
│       └── test/               # JUnit5 단위/통합 테스트
├── frontend/                   # Next.js 14 App Router
│   └── src/
│       ├── app/                # 페이지 라우트
│       ├── components/         # 공통 컴포넌트
│       ├── hooks/              # 커스텀 훅 (useAuth, useWebSocket)
│       ├── lib/                # Axios 클라이언트 + 인터셉터
│       └── store/              # Zustand 상태 관리
├── nginx/                      # Nginx 리버스 프록시 설정
├── monitoring/                 # Prometheus + Grafana 설정
├── load-test/                  # JMeter 부하 테스트
├── .github/workflows/          # GitHub Actions CI/CD
├── docker-compose.yml          # 로컬 개발 환경
├── docker-compose.prod.yml     # 프로덕션 환경
└── CLAUDE.md                   # 개발 가이드
```

---

## CI/CD 파이프라인

`main` 브랜치 Push → GitHub Actions 자동 실행

```
1. JUnit5 테스트 실행
2. Docker 이미지 빌드 (backend + frontend)
3. AWS ECR 푸시
4. EC2 SSH 접속 → docker-compose pull + 재시작
5. 헬스체크 후 Telegram 배포 알림
```

---

## 인터뷰 대화 포인트

**Q: 동시 입찰 Race Condition은 어떻게 해결했나요?**
Redis Redisson 분산 락을 사용해 경매 ID별로 락을 획득한 후 입찰 처리를 진행합니다. JMeter로 100명 동시 입찰 시나리오를 검증했으며, WINNING 입찰이 정확히 1개만 생성됨을 확인했습니다.

**Q: 실시간 기능은 어떻게 구현했나요?**
WebSocket STOMP 프로토콜을 사용합니다. 입찰 처리 완료 후 해당 경매를 구독 중인 모든 클라이언트에게 현재가·입찰자·시각을 브로드캐스트합니다.

**Q: 캐싱 전략은?**
경매 목록은 30초, 단건 상세는 5분 TTL로 Redis 캐싱합니다. 입찰·등록·수정·삭제 시 관련 캐시를 evict하여 데이터 정합성을 유지합니다.
