# BidBid — 프로젝트 스펙

> AI가 코드를 짤 때 지켜야 할 규칙과 절대 하면 안 되는 것.
> 이 문서를 AI에게 항상 함께 공유하세요.

---

## 기술 스택

| 영역 | 선택 | 이유 |
|------|------|------|
| 백엔드 프레임워크 | Spring Boot 3.3.x (Java 17) | 이미 구축됨, 포트폴리오 수준 안정성 |
| 프론트엔드 | Next.js 14 (App Router, TypeScript) | 이미 구축됨, SSR + CSR 혼합 |
| 데이터베이스 | PostgreSQL 15 (AWS RDS) | 이미 구축됨 |
| 캐시 / 분산 락 | Redis 7 + Redisson (AWS ElastiCache) | 이미 구축됨 |
| ORM | Spring Data JPA + QueryDSL | 이미 구축됨 |
| 인증 | JWT (Access 1h + Refresh 7d) + Kakao OAuth 추가 예정 | 이미 구축됨 |
| 실시간 통신 | WebSocket STOMP | 이미 구축됨 |
| 이미지 저장 | AWS S3 + CloudFront | 이미 구축됨 |
| 알림 | Telegram Bot API (@Async) | 이미 구축됨 |
| 결제 | Toss Payments (Phase 3 추가 예정) | 한국 개발자 커뮤니티 표준, Spring Boot 연동 예제 풍부 |
| 스타일링 | Tailwind CSS | 이미 구축됨 |
| 상태 관리 | Zustand (클라이언트) + TanStack Query (서버) | 이미 구축됨 |
| 배포 | Docker + Nginx + GitHub Actions + AWS EC2 | 이미 구축됨 |
| 모니터링 | Prometheus + Grafana | 이미 구축됨 |

---

## 프로젝트 구조

### 백엔드
```
backend/src/main/java/com/auction/
├── domain/
│   ├── user/       controller, service, repository, entity, dto
│   ├── auction/    controller, service, repository, entity, dto
│   ├── bid/        controller, service, repository, entity, dto
│   ├── payment/    controller, service, repository, entity, dto  ← Phase 3 추가
│   ├── notification/ service, entity
│   └── watchlist/  controller, service, repository, entity
├── global/
│   ├── config/     SecurityConfig, RedisConfig, WebSocketConfig, S3Config, AsyncConfig
│   ├── exception/  GlobalExceptionHandler, CustomException, ErrorCode
│   ├── response/   ApiResponse<T>
│   └── security/   JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
└── infra/
    ├── redis/      RedissonLockService
    ├── s3/         S3Service
    ├── telegram/   TelegramService
    └── toss/       TossPaymentClient  ← Phase 3 추가
```

### 프론트엔드
```
frontend/
├── app/            # 페이지 (Next.js App Router)
├── components/     # 재사용 가능한 UI 컴포넌트
├── hooks/          # use 접두사 커스텀 훅
├── stores/         # Zustand 스토어
├── types/          # TypeScript 타입 정의
├── lib/            # API 클라이언트, 유틸리티
└── public/         # 정적 파일
```

---

## 절대 하지 마 (DO NOT)

> AI에게 코드를 시킬 때 이 목록을 반드시 함께 공유하세요.

- [ ] API 키나 비밀번호를 코드에 직접 쓰지 마 (application.yml 환경변수 사용)
- [ ] 기존 DB 스키마를 임의로 변경하지 마 (마이그레이션 스크립트로만 변경)
- [ ] 테스트 없이 배포하지 마 (GitHub Actions 빌드 + JUnit 테스트 통과 필수)
- [ ] 목업/하드코딩 데이터로 완성이라고 하지 마 (실제 DB 연결 필수)
- [ ] 서비스 인터페이스를 추가하지 마 (포트폴리오 수준 — 직접 클래스 주입)
- [ ] ApiResponse<T> 래퍼 없이 응답을 반환하지 마 (모든 API는 ApiResponse 사용)
- [ ] CustomException + ErrorCode 없이 예외를 던지지 마
- [ ] 분산 락 없이 bid/buy-now 처리하지 마 (반드시 RedissonLockService 사용)
- [ ] 텔레그램 알림을 동기로 호출하지 마 (반드시 @Async("asyncExecutor") 사용)
- [ ] 프론트엔드에서 API를 직접 fetch 하지 마 (TanStack Query 훅으로만 호출)

---

## 항상 해 (ALWAYS DO)

- [ ] 변경하기 전에 계획을 먼저 보여줘
- [ ] 환경변수는 application.yml (backend) / .env.local (frontend) 에 저장
- [ ] 에러 발생 시 사용자에게 친절한 한국어 메시지 표시
- [ ] 모든 타임스탬프는 UTC 저장, 프론트에서 KST 변환
- [ ] 한글 주석으로 클래스/메서드 역할 설명 (MEMORY.md 코드 스타일 준수)
- [ ] 새 엔티티 추가 시 BaseEntity 상속 (JPA Auditing: createdAt, updatedAt)
- [ ] 새 API 추가 시 서비스 레이어 단위 테스트 작성

---

## 코딩 컨벤션

### 백엔드 (Java)
- 패키지: `com.auction.{domain}.{layer}`
- 모든 API 응답: `ApiResponse<T>` 래퍼
- 예외: `CustomException(ErrorCode.XXX)` 형식
- 한글 Javadoc 주석 필수 (클래스 상단 + 주요 메서드)
- 변수 선언 열 맞춤 (MEMORY.md 참조)

### 프론트엔드 (TypeScript)
- 컴포넌트: PascalCase
- 훅: `use` 접두사
- API 호출: TanStack Query 훅만 사용
- 타입: `types/` 디렉토리에서 관리

---

## 테스트 방법

```bash
# 백엔드 테스트 실행
cd backend && ./gradlew test

# 백엔드 로컬 실행
cd backend && ./gradlew bootRun

# 프론트엔드 로컬 실행
cd frontend && npm run dev

# 타입 체크
cd frontend && npx tsc --noEmit

# 빌드 확인
cd frontend && npm run build

# Docker 전체 실행 (로컬)
docker-compose up -d
```

---

## 배포 방법

```bash
# main 브랜치 push → GitHub Actions 자동 실행
git push origin main

# 배포 순서:
# 1. JUnit5 테스트
# 2. Docker 이미지 빌드
# 3. AWS ECR push
# 4. EC2 SSH → docker pull + restart
# 5. Telegram 배포 완료 알림
```

---

## 환경변수

### 백엔드 (application.yml)
| 변수명 | 설명 | 어디서 발급 |
|--------|------|------------|
| DB_URL | PostgreSQL 연결 URL | AWS RDS |
| DB_USERNAME | DB 사용자명 | AWS RDS |
| DB_PASSWORD | DB 비밀번호 | AWS RDS |
| REDIS_HOST | Redis 호스트 | AWS ElastiCache |
| JWT_SECRET | JWT 서명 키 | 직접 생성 (32자 이상) |
| AWS_ACCESS_KEY | S3 접근 키 | AWS IAM |
| AWS_SECRET_KEY | S3 시크릿 키 | AWS IAM |
| S3_BUCKET | S3 버킷명 | AWS S3 |
| TELEGRAM_BOT_TOKEN | 텔레그램 봇 토큰 | @BotFather |
| TOSS_CLIENT_KEY | Toss Payments 클라이언트 키 | https://developers.tosspayments.com |
| TOSS_SECRET_KEY | Toss Payments 시크릿 키 | https://developers.tosspayments.com |
| KAKAO_CLIENT_ID | 카카오 OAuth 앱 키 | https://developers.kakao.com |
| KAKAO_CLIENT_SECRET | 카카오 OAuth 시크릿 | https://developers.kakao.com |

### 프론트엔드 (.env.local)
| 변수명 | 설명 |
|--------|------|
| NEXT_PUBLIC_API_URL | 백엔드 API URL |
| NEXT_PUBLIC_WS_URL | WebSocket URL |
| NEXT_PUBLIC_TOSS_CLIENT_KEY | Toss Payments 클라이언트 키 |

> .env.local 파일은 절대 GitHub에 올리지 마세요. (.gitignore 확인 필수)

---

## [NEEDS CLARIFICATION]

- [ ] Kakao OAuth redirect URI 배포 환경 URL 확인 필요
- [ ] Toss Payments 상용 전환 시 사업자 등록증 필요 여부 확인
- [ ] Phase 4 PWA Service Worker의 WebSocket 연결 유지 전략 결정
