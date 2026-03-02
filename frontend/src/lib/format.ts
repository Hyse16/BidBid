/**
 * 가격을 한국 원화 형식으로 포맷한다.
 * 예: 1234567 → "1,234,567원"
 */
export function formatPrice(price: number): string {
  return price.toLocaleString("ko-KR") + "원";
}

/**
 * 날짜/시간을 상대적인 시간으로 표시한다.
 * 예: "3시간 후", "2일 후", "5분 전"
 */
export function formatRelativeTime(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = date.getTime() - now.getTime();
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (diffMs < 0) {
    // 이미 지난 시간
    const absDiffMin = Math.abs(diffMin);
    const absDiffHour = Math.abs(diffHour);
    if (absDiffMin < 60) return `${absDiffMin}분 전`;
    if (absDiffHour < 24) return `${absDiffHour}시간 전`;
    return `${Math.abs(diffDay)}일 전`;
  }

  // 남은 시간
  if (diffMin < 60) return `${diffMin}분`;
  if (diffHour < 24) return `${diffHour}시간`;
  return `${diffDay}일`;
}

/**
 * 날짜/시간을 사람이 읽기 좋은 형식으로 포맷한다.
 * 예: "2024년 3월 15일 14:30"
 */
export function formatDateTime(dateString: string): string {
  return new Date(dateString).toLocaleString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}
