# BidBid — 프리미엄 다크 UI 리디자인

> 생성일: 2026-03-03

## 방향
크리스티·소더비 경매장 느낌 — 짙은 다크 배경 + 골드 포인트

## 색상 팔레트
- Body 배경: #0f0f0f
- Header/Footer 배경: #12121f / #0a0a0a
- 카드 배경: #1a1a2e
- 골드 포인트: #d4af37 ~ #f59e0b
- 텍스트: #e5e7eb (밝은 회색), #9ca3af (서브)
- 테두리: rgba(212,175,55,0.3) (반투명 골드)

## 수정 파일 (6개)
1. `globals.css` — 디자인 토큰, btn/card/input/badge 클래스 전면 교체
2. `layout.tsx` — body/main/footer 다크 배경
3. `Header.tsx` — 골드 로고, 골드 테두리, 골드 hover
4. `AuctionCard.tsx` — 다크 카드, 골드 현재가, hover 골드 테두리
5. `StatusBadge.tsx` — 골드/다크 배지 색상
6. `auctions/[id]/page.tsx` — 골드 현재가, 골드 입찰 버튼, 다크 가격카드
