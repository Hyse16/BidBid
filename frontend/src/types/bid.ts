export type BidStatus = "WINNING" | "OUTBID" | "FAILED";

export interface BidResponse {
  id: number;
  auctionItemId: number;
  bidderNickname: string;
  bidPrice: number;
  status: BidStatus;
  createdAt: string;
}

export interface BidRequest {
  bidPrice: number;
}

export interface BidBroadcastMessage {
  auctionItemId: number;
  currentPrice: number;
  bidderNickname: string;
  timestamp: string;
}
