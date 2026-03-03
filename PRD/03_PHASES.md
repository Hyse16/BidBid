# BidBid — Phase 분리 계획

> 한 번에 다 만들면 복잡해져서 품질이 떨어집니다.
> Phase별로 나눠서 각각 "진짜 동작하는 제품"을 만듭니다.

---

## Phase 1: 핵심 기능 완료 (Step 1~8 완료 ✅)

### 목표
실시간 입찰, 알림, 배포, 모니터링까지 포트폴리오 수준의 완성도를 갖춘 경매 플랫폼.

### 기능
- [x] 사용자 인증 (이메일+비밀번호, JWT Access/Refresh Token)
- [x] 경매 아이템 CRUD (이미지 S3 업로드, QueryDSL 검색/필터)
- [x] 실시간 입찰 (WebSocket STOMP + Redis 분산 락)
- [x] 텔레그램 알림 + 알림함 UI (BID/OUTBID/WIN/LOSE/EXPIRY_WARNING)
- [x] 경매 종료 스케줄러 (매 1분 ACTIVE→ENDED 처리)
- [x] 관심목록 (Watchlist 추가/제거/조회)
- [x] 낙찰 결과 조회 API
- [x] Docker + Nginx + GitHub Actions CI/CD
- [x] Prometheus + Grafana 모니터링 대시보드
- [x] JMeter 100명 동시 입찰 테스트

### 데이터
users, categories, auction_items, auction_images, bids, auction_results, notifications, watchlist

### 인증
이메일 + 비밀번호 (BCrypt + JWT)

### "진짜 제품" 체크리스트
- [x] 실제 DB 연결 (PostgreSQL + RDS)
- [x] 실제 인증 (JWT, 하드코딩 없음)
- [x] 실제 서버에 배포 (AWS EC2 + Docker)
- [x] 다른 사람이 URL로 접속해서 써볼 수 있음

---

## Phase 2: 즉시구매 구현 (예상 1~2주)

### 전제 조건
- Phase 1이 안정적으로 배포된 상태 ✅

### 목표
buy_now_price가 설정된 경매에서 즉시구매 버튼으로 경매를 즉시 종료하고 낙찰 처리한다.

### 기능
- [ ] `POST /api/auctions/{id}/buy-now` API 구현
- [ ] BuyNowService — Redis 분산 락 + 경매 즉시 ENDED 처리 + auction_results 생성
- [ ] 입찰 중인 사람들에게 OUTBID 알림 일괄 전송
- [ ] 프론트엔드: 즉시구매 버튼 표시 조건 (buy_now_price 존재 + ACTIVE 상태)
- [ ] 프론트엔드: 즉시구매 확인 다이얼로그 ("xx원에 즉시 구매하시겠습니까?")
- [ ] 즉시구매 완료 후 낙찰자에게 WIN 알림 + 결제 유도 메시지

### 추가 데이터
- 기존 auction_results 재사용 (buy_now 여부 플래그 불필요)

### Phase 2 시작 프롬프트
```
이 PRD를 읽고 Phase 2 즉시구매 기능을 구현해주세요.
@PRD/01_PRD.md
@PRD/02_DATA_MODEL.md
@PRD/04_PROJECT_SPEC.md

Phase 2 범위:
- POST /api/auctions/{id}/buy-now API (BuyNowService)
- Redis 분산 락으로 동시 즉시구매 + 입찰 충돌 방지
- 프론트엔드 즉시구매 버튼 + 확인 모달

반드시 지켜야 할 것:
- 04_PROJECT_SPEC.md의 "절대 하지 마" 목록 준수
- 기존 BidService의 분산 락 패턴 동일하게 적용
- buy_now_price가 null이면 버튼 미표시
```

### 통합 테스트
- 즉시구매 시 진행 중 입찰 OUTBID 처리 확인
- 즉시구매와 동시 입찰 경합 조건 테스트

---

## Phase 3: 결제 시스템 (예상 2~3주)

### 전제 조건
- Phase 2 즉시구매 기능 안정화 완료

### 목표
Toss Payments 연동으로 낙찰자가 앱 안에서 바로 결제 완료할 수 있다.

### 기능
- [ ] payments 테이블 마이그레이션 추가
- [ ] PaymentService 구현 (결제 요청 → 승인 → 웹훅 처리)
- [ ] `POST /api/payments/confirm` — Toss Payments 승인 API 호출
- [ ] `POST /api/payments/webhook` — Toss 웹훅 수신 (pending→completed/failed)
- [ ] `GET /api/users/me/payments` — 결제 내역 조회
- [ ] 프론트엔드: 낙찰자 마이페이지에 [결제하기] 버튼
- [ ] 프론트엔드: Toss Payments SDK 위젯 연동
- [ ] 결제 성공/실패 알림 (텔레그램 + 알림함)

### 추가 데이터
- payments 테이블 신규 생성 (02_DATA_MODEL.md 참조)

### 환경변수 추가
```
TOSS_CLIENT_KEY=test_ck_...
TOSS_SECRET_KEY=test_sk_...
```

### 통합 테스트
- 결제 성공 → payments.status = completed 확인
- 결제 실패 → 적절한 에러 메시지 표시
- 웹훅 중복 처리 방어 (idempotency)

### Phase 3 시작 프롬프트
```
이 PRD를 읽고 Phase 3 결제 시스템을 구현해주세요.
@PRD/01_PRD.md
@PRD/02_DATA_MODEL.md
@PRD/04_PROJECT_SPEC.md

Phase 3 범위:
- payments 테이블 + PaymentService
- Toss Payments 결제 승인 + 웹훅 처리
- 프론트엔드 결제 흐름 (위젯 → 성공/실패 페이지)

반드시 지켜야 할 것:
- 04_PROJECT_SPEC.md의 "절대 하지 마" 목록 준수
- 웹훅 처리 시 idempotency 보장 (중복 처리 방지)
- 테스트 키 사용 (실제 결제 발생 안 함)
```

---

## Phase 4: 모바일 최적화 (예상 1~2주)

### 전제 조건
- Phase 1~3 기능이 안정적으로 운영 중

### 목표
모바일(375px~768px)에서 입찰, 즉시구매, 결제 전 흐름이 완전히 동작한다.

### 기능
- [ ] 모든 페이지 Tailwind 반응형 클래스 적용 (모바일 우선)
- [ ] Bottom Navigation Bar 컴포넌트 (경매 목록 / 관심목록 / 알림 / 마이페이지)
- [ ] 모바일 입찰 UI 최적화 (큰 터치 타겟, 숫자 입력 최적화)
- [ ] PWA 설정 (manifest.json + Service Worker)
- [ ] 푸시 알림 연동 (Web Push API → 기존 텔레그램과 병행)

### 주의사항
- PWA 푸시 알림은 HTTPS 필요 (이미 배포 환경 SSL 완료)
- Service Worker 캐싱 전략이 실시간 WebSocket 데이터와 충돌하지 않도록 주의

---

## Phase 로드맵 요약

| Phase | 핵심 기능 | 상태 |
|-------|----------|------|
| Phase 1 (MVP) | 인증 + 경매 CRUD + 실시간 입찰 + 알림 + 배포 | ✅ 완료 |
| Phase 2 | 즉시구매 (Buy Now) | 🔜 다음 |
| Phase 3 | Toss Payments 결제 연동 | ⏳ Phase 2 완료 후 |
| Phase 4 | 모바일 반응형 + PWA | ⏳ Phase 3 완료 후 |
