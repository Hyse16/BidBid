import { AuctionStatus } from "@/types/auction";

const STATUS_LABELS: Record<AuctionStatus, string> = {
  PENDING: "대기 중",
  ACTIVE:  "진행 중",
  ENDED:   "종료",
  CANCELLED: "취소",
};

const STATUS_CLASS: Record<AuctionStatus, string> = {
  PENDING:   "badge-pending",
  ACTIVE:    "badge-active",
  ENDED:     "badge-ended",
  CANCELLED: "badge-cancelled",
};

/**
 * 경매 상태 배지 컴포넌트 — 프리미엄 다크 테마
 */
export default function StatusBadge({ status }: { status: AuctionStatus }) {
  return (
    <span className={STATUS_CLASS[status]}>
      {STATUS_LABELS[status]}
    </span>
  );
}
