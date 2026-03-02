export type AuctionStatus = "PENDING" | "ACTIVE" | "ENDED" | "CANCELLED";

export interface AuctionListResponse {
  id: number;
  title: string;
  currentPrice: number;
  buyNowPrice: number | null;
  status: AuctionStatus;
  endAt: string;
  categoryName: string;
  thumbnailUrl: string | null;
}

export interface AuctionImageInfo {
  url: string;
  isThumbnail: boolean;
}

export interface AuctionResponse {
  id: number;
  title: string;
  description: string;
  startPrice: number;
  currentPrice: number;
  buyNowPrice: number | null;
  minBidUnit: number;
  status: AuctionStatus;
  startAt: string;
  endAt: string;
  categoryName: string;
  sellerNickname: string;
  images: AuctionImageInfo[];
}

export interface CategoryResponse {
  id: number;
  name: string;
}

export interface AuctionSearchParams {
  categoryId?: number;
  status?: AuctionStatus;
  keyword?: string;
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface WatchlistResponse {
  watchlistId: number;
  auctionId: number;
  title: string;
  currentPrice: number;
  buyNowPrice: number | null;
  status: AuctionStatus;
  endAt: string;
  categoryName: string;
  thumbnailUrl: string | null;
  addedAt: string;
}
