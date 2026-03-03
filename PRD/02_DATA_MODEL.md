# BidBid — 데이터 모델

> 이 문서는 앱에서 다루는 핵심 데이터의 구조를 정의합니다.
> 개발자가 아니어도 이해할 수 있는 "개념적 ERD"입니다.

---

## 전체 구조

```
[users] ─1:N─> [auction_items] ─1:N─> [auction_images]
   │                  │
   │                  ├─1:N─> [bids]
   │                  ├─1:N─> [watchlist]
   │                  └─1:1─> [auction_results] ─1:1─> [payments] ← NEW
   │
   1:N─> [notifications]
   1:N─> [watchlist]

[categories] ─1:N─> [auction_items]
```

---

## 엔티티 상세

### users
사이트에 가입한 회원. 판매자와 구매자 역할을 동시에 수행할 수 있다.

| 필드 | 설명 | 예시 | 필수 |
|------|------|------|------|
| id | 고유 식별자 (자동 생성) | 1 | O |
| email | 로그인 이메일 (유니크) | user@example.com | O |
| password | BCrypt 암호화된 비밀번호 | $2a$10$... | X (소셜 로그인 시 null) |
| nickname | 화면에 표시되는 이름 | 홍길동 | O |
| telegram_chat_id | 텔레그램 알림 수신 ID | 123456789 | X |
| kakao_oauth_id | 카카오 OAuth 식별자 | kakao_9876 | X |
| role | 권한 (USER / ADMIN) | USER | O |
| created_at | 가입 날짜 (자동) | 2026-03-01 | O |

### categories
경매 아이템을 분류하는 카테고리.

| 필드 | 설명 | 예시 | 필수 |
|------|------|------|------|
| id | 고유 식별자 | 1 | O |
| name | 카테고리 이름 | 전자기기 | O |
| created_at | 생성 날짜 | 2026-01-01 | O |

### auction_items
판매자가 등록한 경매 아이템. 상태가 PENDING → ACTIVE → ENDED 순으로 변한다.

| 필드 | 설명 | 예시 | 필수 |
|------|------|------|------|
| id | 고유 식별자 | 42 | O |
| user_id | 판매자 (users.id) | 5 | O |
| category_id | 카테고리 (categories.id) | 2 | O |
| title | 경매 제목 | MacBook Pro M3 | O |
| description | 상세 설명 | 2024년 구매, 상태 A급... | X |
| start_price | 시작 입찰가 (원) | 500000 | O |
| current_price | 현재 최고 입찰가 | 750000 | O |
| buy_now_price | 즉시구매가 (null이면 사용 안 함) | 1200000 | X |
| min_bid_unit | 최소 입찰 단위 (원) | 10000 | O |
| status | 상태 (PENDING/ACTIVE/ENDED/CANCELLED) | ACTIVE | O |
| start_at | 경매 시작 시각 | 2026-03-03 10:00 | O |
| end_at | 경매 종료 시각 | 2026-03-05 22:00 | O |
| created_at | 등록 날짜 | 2026-03-01 | O |

### auction_images
경매 아이템에 첨부된 이미지. S3에 저장된 URL을 참조한다.

| 필드 | 설명 | 예시 | 필수 |
|------|------|------|------|
| id | 고유 식별자 | 101 | O |
| auction_item_id | 아이템 (auction_items.id) | 42 | O |
| image_url | S3 이미지 URL | https://s3.../img.jpg | O |
| is_thumbnail | 대표 이미지 여부 | true | O |
| created_at | 업로드 날짜 | 2026-03-01 | O |

### bids
구매자가 경매에 넣은 입찰 기록. 새 입찰이 들어오면 이전 최고 입찰은 OUTBID 처리된다.

| 필드 | 설명 | 예시 | 필수 |
|------|------|------|------|
| id | 고유 식별자 | 999 | O |
| auction_item_id | 경매 아이템 | 42 | O |
| user_id | 입찰자 | 7 | O |
| bid_price | 입찰 금액 (원) | 780000 | O |
| status | 상태 (WINNING/OUTBID/FAILED) | WINNING | O |
| created_at | 입찰 시각 | 2026-03-04 15:23 | O |

### auction_results
경매가 종료될 때 생성되는 낙찰 결과. 아이템당 1개만 존재한다.

| 필드 | 설명 | 예시 | 필수 |
|------|------|------|------|
| id | 고유 식별자 | 55 | O |
| auction_item_id | 경매 아이템 (유니크) | 42 | O |
| winner_id | 낙찰자 (users.id) | 7 | O |
| final_price | 최종 낙찰가 (원) | 780000 | O |
| created_at | 낙찰 시각 | 2026-03-05 22:00 | O |

### payments ← 신규 추가
낙찰 후 실제 결제가 이뤄진 기록. Toss Payments 연동으로 생성된다.

| 필드 | 설명 | 예시 | 필수 |
|------|------|------|------|
| id | 고유 식별자 | 200 | O |
| auction_result_id | 낙찰 결과 연동 | 55 | O |
| user_id | 결제자 | 7 | O |
| amount | 결제 금액 (원) | 780000 | O |
| method | 결제 수단 (card/kakao/toss/bank) | card | O |
| status | 결제 상태 (pending/completed/failed/refunded) | completed | O |
| toss_payment_key | Toss 거래 고유 키 | tviva... | X |
| toss_order_id | 주문 ID (UUID 생성) | BID-42-7-... | O |
| paid_at | 결제 완료 시각 | 2026-03-05 22:15 | X |
| created_at | 결제 시도 시각 | 2026-03-05 22:10 | O |

### notifications
사용자에게 발송되는 알림 기록 (텔레그램 + 알림함).

| 필드 | 설명 | 예시 | 필수 |
|------|------|------|------|
| id | 고유 식별자 | 300 | O |
| user_id | 알림 수신자 | 7 | O |
| type | 알림 종류 (BID/OUTBID/WIN/LOSE/EXPIRY_WARNING) | WIN | O |
| message | 알림 메시지 | 🎉 MacBook 낙찰! 780,000원 | O |
| is_sent | 텔레그램 발송 여부 | true | O |
| created_at | 발생 시각 | 2026-03-05 22:00 | O |

### watchlist
구매자가 관심 등록한 경매 목록.

| 필드 | 설명 | 예시 | 필수 |
|------|------|------|------|
| id | 고유 식별자 | 400 | O |
| user_id | 사용자 | 7 | O |
| auction_item_id | 관심 경매 아이템 | 42 | O |
| created_at | 등록 날짜 | 2026-03-02 | O |

---

## 관계 요약

- **users** 1명이 여러 **auction_items** (판매자)를 등록할 수 있음
- **users** 1명이 여러 **bids** (입찰 기록)를 가질 수 있음
- **auction_items** 1개에 여러 **auction_images** 가 첨부됨
- **auction_items** 1개가 종료되면 **auction_results** 1개가 생성됨
- **auction_results** 1개에 **payments** 1개가 연결됨 (결제 후)
- **watchlist** 는 users ↔ auction_items 다대다 관계를 중간 테이블로 해결

---

## 왜 이 구조인가

- **확장성**: payments 테이블을 auction_results와 1:1로 분리하여 결제 실패/재시도 상태를 깔끔하게 관리
- **단순성**: 즉시구매는 별도 테이블 없이 auction_items.buy_now_price 필드 + 별도 API로 처리 (auction_results 재사용)
- **분리된 관심사**: bid 기록은 영구 보존하되 current_price는 auction_items에서 관리하여 조회 성능 유지

---

## [NEEDS CLARIFICATION]

- [ ] payments.toss_payment_key 가 null인 경우 결제 취소/환불 흐름 정의 필요
- [ ] 동일 낙찰자가 결제 실패 후 재시도 시 payments 레코드 생성 정책 (신규 생성 vs 업데이트)
