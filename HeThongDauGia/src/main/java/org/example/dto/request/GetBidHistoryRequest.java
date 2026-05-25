package org.example.dto.request;

public class GetBidHistoryRequest extends BaseRequest {
    private long auctionId;

    public long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(long auctionId) {
        this.auctionId = auctionId;
    }
}
