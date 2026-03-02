# Auction Platform - Real-time Bidding Service

## Project Overview
A real-time auction platform built for portfolio purposes, targeting production-level quality.
Monorepo structure containing backend (Spring Boot) and frontend (Next.js).

## Repository Structure
```
auction-platform/
├── backend/                  # Spring Boot application
├── frontend/                 # Next.js 14 application
├── nginx/                    # Reverse proxy config
├── monitoring/               # Prometheus + Grafana config
├── .github/workflows/        # CI/CD pipelines
├── docker-compose.yml        # Local development
├── docker-compose.prod.yml   # Production
└── CLAUDE.md
```

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.3.x
- Spring Security + JWT (Access + Refresh Token)
- Spring Data JPA + QueryDSL
- Spring WebSocket (STOMP protocol)
- Spring Scheduler (auction expiry processing)
- Redis (distributed lock + caching)
- PostgreSQL 15
- AWS S3 (image upload)
- Telegram Bot API (notifications)
- Prometheus + Micrometer (metrics)

### Frontend
- Next.js 14 (App Router)
- TypeScript
- Tailwind CSS
- SockJS + STOMP.js (WebSocket client)
- Zustand (client state)
- TanStack Query / React Query (server state)

### DevOps
- Docker + Docker Compose
- AWS (EC2 + RDS + ElastiCache + S3 + ECR)
- GitHub Actions (CI/CD)
- Nginx (reverse proxy + SSL termination)
- Grafana + Prometheus (monitoring)

## Backend Project Structure
```
backend/src/main/java/com/auction/
├── domain/
│   ├── user/
│   │   ├── controller/UserController.java
│   │   ├── service/UserService.java
│   │   ├── repository/UserRepository.java
│   │   ├── entity/User.java
│   │   └── dto/
│   │       ├── SignupRequest.java
│   │       ├── LoginRequest.java
│   │       └── UserResponse.java
│   ├── auction/
│   │   ├── controller/AuctionController.java
│   │   ├── service/AuctionService.java
│   │   ├── repository/AuctionRepository.java
│   │   ├── entity/
│   │   │   ├── AuctionItem.java
│   │   │   └── AuctionImage.java
│   │   └── dto/
│   ├── bid/
│   │   ├── controller/BidController.java
│   │   ├── service/BidService.java
│   │   ├── repository/BidRepository.java
│   │   ├── entity/Bid.java
│   │   └── dto/
│   ├── notification/
│   │   ├── service/NotificationService.java
│   │   ├── entity/Notification.java
│   │   └── NotificationType.java (enum)
│   └── watchlist/
│       ├── controller/WatchlistController.java
│       ├── service/WatchlistService.java
│       ├── repository/WatchlistRepository.java
│       └── entity/Watchlist.java
├── global/
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   ├── WebSocketConfig.java
│   │   ├── S3Config.java
│   │   └── AsyncConfig.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── CustomException.java
│   │   └── ErrorCode.java (enum)
│   ├── response/
│   │   └── ApiResponse.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetailsService.java
│   └── websocket/
│       └── AuctionWebSocketHandler.java
└── infra/
    ├── redis/
    │   └── RedissonLockService.java
    ├── s3/
    │   └── S3Service.java
    └── telegram/
        └── TelegramService.java
```

## Database Schema (ERD)

### users
```sql
id               BIGINT PK AUTO_INCREMENT
email            VARCHAR(100) UNIQUE NOT NULL
password         VARCHAR(255) NOT NULL          -- BCrypt
nickname         VARCHAR(50) NOT NULL
telegram_chat_id VARCHAR(100)                   -- for notifications
role             ENUM('USER', 'ADMIN') DEFAULT 'USER'
created_at       TIMESTAMP
updated_at       TIMESTAMP
```

### categories
```sql
id         BIGINT PK AUTO_INCREMENT
name       VARCHAR(50) NOT NULL
created_at TIMESTAMP
```

### auction_items
```sql
id            BIGINT PK AUTO_INCREMENT
user_id       BIGINT FK → users.id
category_id   BIGINT FK → categories.id
title         VARCHAR(100) NOT NULL
description   TEXT
start_price   BIGINT NOT NULL
current_price BIGINT NOT NULL
buy_now_price BIGINT                            -- nullable, instant purchase
min_bid_unit  BIGINT NOT NULL DEFAULT 1000
status        ENUM('PENDING','ACTIVE','ENDED','CANCELLED') DEFAULT 'PENDING'
start_at      TIMESTAMP NOT NULL
end_at        TIMESTAMP NOT NULL
created_at    TIMESTAMP
updated_at    TIMESTAMP
```

### auction_images
```sql
id              BIGINT PK AUTO_INCREMENT
auction_item_id BIGINT FK → auction_items.id
image_url       VARCHAR(500) NOT NULL           -- S3 URL
is_thumbnail    BOOLEAN DEFAULT FALSE
created_at      TIMESTAMP
```

### bids
```sql
id              BIGINT PK AUTO_INCREMENT
auction_item_id BIGINT FK → auction_items.id
user_id         BIGINT FK → users.id
bid_price       BIGINT NOT NULL
status          ENUM('WINNING','OUTBID','FAILED') DEFAULT 'WINNING'
created_at      TIMESTAMP
```

### auction_results
```sql
id              BIGINT PK AUTO_INCREMENT
auction_item_id BIGINT FK UNIQUE → auction_items.id
winner_id       BIGINT FK → users.id
final_price     BIGINT NOT NULL
created_at      TIMESTAMP
updated_at      TIMESTAMP
```

### notifications
```sql
id         BIGINT PK AUTO_INCREMENT
user_id    BIGINT FK → users.id
type       ENUM('BID','OUTBID','WIN','LOSE','EXPIRY_WARNING')
message    TEXT
is_sent    BOOLEAN DEFAULT FALSE
created_at TIMESTAMP
```

### watchlist
```sql
id              BIGINT PK AUTO_INCREMENT
user_id         BIGINT FK → users.id
auction_item_id BIGINT FK → auction_items.id
created_at      TIMESTAMP
UNIQUE(user_id, auction_item_id)
```

## API Endpoints

```
# Auth
POST   /api/auth/signup
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/logout

# Auction
GET    /api/auctions              # list (paging, filter by category/status)
GET    /api/auctions/{id}         # detail
POST   /api/auctions              # create (auth required)
PUT    /api/auctions/{id}         # update (owner only)
DELETE /api/auctions/{id}         # delete (owner only)

# Bid
POST   /api/auctions/{id}/bids    # place bid (auth required)
GET    /api/auctions/{id}/bids    # bid history

# Watchlist
POST   /api/watchlist/{auctionId} # add
DELETE /api/watchlist/{auctionId} # remove
GET    /api/watchlist             # my watchlist

# User
GET    /api/users/me              # my profile
PUT    /api/users/me              # update profile
GET    /api/users/me/auctions     # my auction items
GET    /api/users/me/bids         # my bid history

# WebSocket (STOMP)
SUBSCRIBE /topic/auction/{id}     # subscribe to real-time bid updates
SEND      /app/auction/{id}/bid   # send bid
```

## Key Implementation Details

### 1. Concurrency Control (Redis Distributed Lock)
```java
// Always use distributed lock when processing bids
RLock lock = redissonClient.getLock("auction:bid:" + auctionId);
try {
    lock.lock(5, TimeUnit.SECONDS);
    // 1. Check current highest price
    // 2. Validate bid amount > current_price
    // 3. Save bid, update current_price
    // 4. Broadcast via WebSocket
} finally {
    lock.unlock();
}
```

### 2. WebSocket Real-time Flow
```
Client → STOMP SEND /app/auction/{id}/bid {bidPrice}
Server → Process bid with Redis lock
Server → STOMP BROADCAST /topic/auction/{id} {currentPrice, bidder, timestamp}
All subscribers receive real-time update
```

### 3. Telegram Notifications (Async)
```java
// All telegram calls must be @Async to avoid blocking main thread
@Async
public void sendMessage(String chatId, String message)

// Notification types:
// BID            → "📈 New bid on [item]! Current price: 75,000원"
// OUTBID         → "⚠️ You've been outbid on [item]! Current: 80,000원"
// WIN            → "🎉 Congratulations! You won [item] for 95,000원"
// LOSE           → "😢 You didn't win [item]. Final price: 95,000원"
// EXPIRY_WARNING → "⏰ [item] ends in 1 hour! Current: 80,000원"
```

### 4. Auction Expiry Scheduler
```java
@Scheduled(fixedRate = 60000) // every 1 minute
public void processExpiredAuctions()  // ACTIVE → ENDED, create auction_results

@Scheduled(fixedRate = 60000)
public void sendExpiryWarnings()      // notify watchers 1 hour before end
```

### 5. Redis Caching Strategy
```java
@Cacheable(value = "auctions", key = "#pageable")  // cache auction list (TTL: 30s)
@CacheEvict(value = "auctions", allEntries = true)  // evict on bid/create/update
```

## Coding Conventions

### Common
- All API responses wrapped in `ApiResponse<T>`
- All exceptions handled via `CustomException` + `ErrorCode` enum
- All timestamps in UTC, convert to KST on frontend

### Backend
- Package: `com.auction.{domain}.{layer}`
- Entities extend `BaseEntity` (JPA Auditing: createdAt, updatedAt)
- Separate Request/Response DTOs
- No service interfaces (portfolio level)
- Unit tests required for service layer
- Integration tests for controllers

### Frontend
- Components: PascalCase
- Hooks: `use` prefix
- API calls via React Query only
- Types managed in `types/` directory

## Environment Variables

### Backend (application.yml)
```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}

jwt:
  secret: ${JWT_SECRET}
  access-expiration: 3600000     # 1 hour
  refresh-expiration: 604800000  # 7 days

aws:
  s3:
    bucket: ${S3_BUCKET}
  credentials:
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}
  region: ap-northeast-2

telegram:
  bot-token: ${TELEGRAM_BOT_TOKEN}
```

### Frontend (.env.local)
```
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080
```

## Docker Compose (Local Development)
```yaml
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: auction
      POSTGRES_USER: auction
      POSTGRES_PASSWORD: auction1234
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"

  grafana:
    image: grafana/grafana
    ports:
      - "3001:3000"
```

## AWS Infrastructure
```
EC2 t3.small        → application server (Docker containers)
RDS PostgreSQL      → db.t3.micro (free tier)
ElastiCache Redis   → cache.t3.micro
S3                  → image storage
ECR                 → Docker image registry
Route53 + ACM       → domain + SSL certificate
```

## CI/CD (GitHub Actions)
```
Trigger: push to main branch
Steps:
  1. Run JUnit5 tests
  2. Build Docker image
  3. Push to AWS ECR
  4. SSH into EC2 → docker pull + restart containers
  5. Send deployment result notification via Telegram
```

## Monitoring (Grafana Dashboards)
```
Key metrics:
- API response time (p50, p95, p99)
- Active WebSocket connections
- Bids per second
- Redis cache hit rate
- JVM memory / GC activity
- DB connection pool usage
```

## Development Order

```
Step 1 - Project Initialization
  □ Spring Boot project setup (build.gradle + dependencies)
  □ application.yml with dev/prod profiles
  □ Docker Compose (postgres + redis)
  □ BaseEntity, ApiResponse<T>, ErrorCode enum
  □ Security skeleton + JPA configuration

Step 2 - Authentication
  □ User entity + repository
  □ Signup / Login API
  □ JWT access + refresh token
  □ JwtAuthenticationFilter
  □ Unit tests

Step 3 - Auction CRUD
  □ AuctionItem + AuctionImage entity
  □ Create / Read / Update / Delete API
  □ S3 image upload integration
  □ Category management
  □ Pagination + filtering (QueryDSL)

Step 4 - Real-time Bidding (Core Feature)
  □ WebSocket STOMP configuration
  □ Bid API endpoint
  □ Redis distributed lock (concurrency control)
  □ Real-time broadcast to all subscribers

Step 5 - Notifications + Scheduler
  □ Telegram Bot setup + TelegramService
  □ Async notification service
  □ Auction expiry scheduler (ACTIVE → ENDED)
  □ 1-hour expiry warning scheduler

Step 6 - Frontend
  □ Next.js 14 project init + Tailwind
  □ Auth pages (login / signup)
  □ Auction list + detail pages
  □ Real-time bid UI (WebSocket + STOMP.js)
  □ My page (my auctions, my bids, watchlist)

Step 7 - Deployment
  □ AWS infrastructure setup
  □ Dockerfile for backend + frontend
  □ GitHub Actions CI/CD pipeline
  □ Nginx reverse proxy + SSL (ACM)

Step 8 - Quality & Polish
  □ JUnit5 service unit tests
  □ Controller integration tests
  □ JMeter concurrency test (100 simultaneous bids)
  □ Grafana dashboard configuration
  □ README.md with architecture diagram
```

## Portfolio Interview Talking Points
```
1. Concurrency handling
   "Solved Race Condition in simultaneous bidding using Redis distributed lock (Redisson)"

2. Real-time communication
   "Implemented real-time bidding with WebSocket STOMP protocol"

3. Caching strategy
   "Reduced DB load with Redis caching + cache eviction on bid/update events"

4. CI/CD automation
   "Automated test + build + deployment pipeline with GitHub Actions"

5. Observability
   "Set up production monitoring with Prometheus metrics + Grafana dashboards"

6. Load testing
   "Verified correctness under 100 concurrent bid scenario using JMeter"
```
