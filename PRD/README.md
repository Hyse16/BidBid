# BidBid — 디자인 문서

> Show Me The PRD로 생성됨 (2026-03-03)

## 문서 구성

| 문서 | 내용 | 언제 읽나 |
|------|------|----------|
| [01_PRD.md](./01_PRD.md) | 뭘 만드는지, 누가 쓰는지, 성공 기준 | 프로젝트 방향 확인할 때 |
| [02_DATA_MODEL.md](./02_DATA_MODEL.md) | DB 테이블 구조, 엔티티 관계도 | payments 테이블 추가 등 DB 작업할 때 |
| [03_PHASES.md](./03_PHASES.md) | Phase별 구현 계획 + 시작 프롬프트 | 다음 기능 개발 시작할 때 |
| [04_PROJECT_SPEC.md](./04_PROJECT_SPEC.md) | 기술 스택, 코딩 규칙, 절대 하지 마 | AI에게 코드 시킬 때마다 |

---

## 현재 상태

```
Phase 1 ✅ 완료   — 인증 + 경매 CRUD + 실시간 입찰 + 알림 + 배포
Phase 2 🔜 다음   — 즉시구매 (Buy Now)
Phase 3 ⏳ 예정   — Toss Payments 결제 연동
Phase 4 ⏳ 예정   — 모바일 반응형 + PWA
```

---

## 다음 단계

**Phase 2 즉시구매 기능**을 시작하려면:
1. [03_PHASES.md](./03_PHASES.md) 의 **"Phase 2 시작 프롬프트"** 복사
2. AI에게 `@PRD/01_PRD.md`, `@PRD/02_DATA_MODEL.md`, `@PRD/04_PROJECT_SPEC.md` 함께 공유
3. 시작 프롬프트 붙여넣기 → 개발 시작

---

## 미결 사항 종합 ([NEEDS CLARIFICATION])

- [ ] Kakao OAuth 앱 등록 및 클라이언트 ID 발급
- [ ] Toss Payments 테스트 키 발급 (https://developers.tosspayments.com)
- [ ] 낙찰 후 결제 기한 정책 (예: 24시간 미결제 시 차순위 낙찰 여부)
- [ ] 즉시구매 시 진행 중 입찰자 알림 범위
- [ ] payments 결제 실패 후 재시도 정책

---

## 기술 스택 요약

```
Backend  : Spring Boot 3.3 + PostgreSQL 15 + Redis + WebSocket STOMP
Frontend : Next.js 14 + TypeScript + Tailwind CSS + TanStack Query
결제      : Toss Payments (Phase 3)
인증      : JWT + Kakao OAuth (Phase 2)
배포      : AWS EC2 + RDS + ElastiCache + S3 + ECR + Nginx
모니터링  : Prometheus + Grafana
```
