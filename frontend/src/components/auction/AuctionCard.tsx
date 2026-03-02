import Link from "next/link";
import Image from "next/image";
import { AuctionListResponse } from "@/types/auction";
import StatusBadge from "@/components/ui/StatusBadge";
import { formatPrice, formatRelativeTime } from "@/lib/format";

interface AuctionCardProps {
  auction: AuctionListResponse;
}

/**
 * 경매 목록 카드 컴포넌트
 *
 * 썸네일, 상태 배지, 현재가, 종료 시간을 표시한다.
 */
export default function AuctionCard({ auction }: AuctionCardProps) {
  return (
    <Link href={`/auctions/${auction.id}`}>
      <div className="card hover:shadow-md transition-shadow cursor-pointer">
        {/* 썸네일 이미지 */}
        <div className="relative w-full h-48 bg-gray-100">
          {auction.thumbnailUrl ? (
            <Image
              src={auction.thumbnailUrl}
              alt={auction.title}
              fill
              className="object-cover"
              sizes="(max-width: 768px) 100vw, 33vw"
            />
          ) : (
            <div className="flex items-center justify-center h-full text-gray-400">
              <svg
                className="w-12 h-12"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
            </div>
          )}

          {/* 상태 배지 (이미지 상단 좌측) */}
          <div className="absolute top-2 left-2">
            <StatusBadge status={auction.status} />
          </div>
        </div>

        {/* 카드 내용 */}
        <div className="p-4">
          <p className="text-xs text-blue-600 font-medium mb-1">{auction.categoryName}</p>
          <h3 className="font-semibold text-gray-900 truncate mb-2">{auction.title}</h3>

          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs text-gray-500">현재가</p>
              <p className="text-lg font-bold text-blue-600">
                {formatPrice(auction.currentPrice)}
              </p>
            </div>

            {auction.buyNowPrice && (
              <div className="text-right">
                <p className="text-xs text-gray-500">즉시 구매</p>
                <p className="text-sm font-medium text-orange-500">
                  {formatPrice(auction.buyNowPrice)}
                </p>
              </div>
            )}
          </div>

          <p className="text-xs text-gray-400 mt-2">
            {auction.status === "ENDED"
              ? "종료됨"
              : `${formatRelativeTime(auction.endAt)} 마감`}
          </p>
        </div>
      </div>
    </Link>
  );
}
