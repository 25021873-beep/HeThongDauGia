package org.example.dto.request;

public class GetAuctionDetailRequest extends BaseRequest {
    private int auctionId;

    public GetAuctionDetailRequest() {}

    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }
}