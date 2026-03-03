import Link from "next/link";
import Image from "next/image";
import { AuctionListResponse } from "@/types/auction";
import StatusBadge from "@/components/ui/StatusBadge";
import { formatPrice, formatRelativeTime } from "@/lib/format";

interface AuctionCardProps {
  auction: AuctionListResponse;
}

/**
 * 경매 목록 카드 컴포넌트 — 프리미엄 다크 테마
 *
 * 썸네일, 상태 배지, 현재가(골드), 종료 시간을 표시한다.
 */
export default function AuctionCard({ auction }: AuctionCardProps) {
  return (
    <Link href={`/auctions/${auction.id}`}>
      <div
        className="group bg-[#1a1a2e] rounded-xl overflow-hidden
                   border border-[rgba(212,175,55,0.12)]
                   hover:border-[rgba(212,175,55,0.5)]
                   shadow-[0_4px_24px_rgba(0,0,0,0.4)]
                   hover:shadow-[0_8px_32px_rgba(212,175,55,0.12)]
                   transition-all duration-300 cursor-pointer"
      >
        {/* 썸네일 이미지 */}
        <div className="relative w-full h-48 bg-[#12121f] overflow-hidden">
          {auction.thumbnailUrl ? (
            <Image
              src={auction.thumbnailUrl}
              alt={auction.title}
              fill
              className="object-cover group-hover:scale-105 transition-transform duration-500"
              sizes="(max-width: 768px) 100vw, 33vw"
            />
          ) : (
            <div className="flex items-center justify-center h-full text-gray-700">
              {/* 경매 망치 아이콘 */}
              <svg className="w-12 h-12 opacity-30" viewBox="0 0 24 24" fill="currentColor">
                <path d="M13.293 2.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l8-8zm-1.414 2.414L6 10.586l2 2 5.879-5.879-2-2zM3 17a1 1 0 011-1h16a1 1 0 110 2H4a1 1 0 01-1-1z"/>
              </svg>
            </div>
          )}

          {/* 골드 그라디언트 오버레이 (하단) */}
          <div className="absolute inset-x-0 bottom-0 h-12
                          bg-gradient-to-t from-[#1a1a2e] to-transparent" />

          {/* 상태 배지 */}
          <div className="absolute top-2 left-2">
            <StatusBadge status={auction.status} />
          </div>
        </div>

        {/* 카드 내용 */}
        <div className="p-4 space-y-2">
          <p className="text-xs text-gray-600 font-medium tracking-wider uppercase">
            {auction.categoryName}
          </p>
          <h3 className="font-semibold text-gray-200 truncate leading-snug">
            {auction.title}
          </h3>

          <div className="flex items-end justify-between pt-1">
            <div>
              <p className="text-[10px] text-gray-600 uppercase tracking-wider mb-0.5">현재가</p>
              <p className="text-lg font-bold text-[#f59e0b] tracking-wide">
                {formatPrice(auction.currentPrice)}
              </p>
            </div>

            {auction.buyNowPrice && (
              <div className="text-right">
                <p className="text-[10px] text-gray-600 uppercase tracking-wider mb-0.5">즉시구매</p>
                <p className="text-sm font-medium text-gray-400">
                  {formatPrice(auction.buyNowPrice)}
                </p>
              </div>
            )}
          </div>

          {/* 마감 시간 */}
          <p className="text-xs text-gray-700 pt-1 border-t border-[rgba(212,175,55,0.08)]">
            {auction.status === "ENDED"
              ? "경매 종료"
              : `⏱ ${formatRelativeTime(auction.endAt)} 마감`}
          </p>
        </div>
      </div>
    </Link>
  );
}
