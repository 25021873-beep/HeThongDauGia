package org.example.dto.request;

public class JoinRequest extends BaseRequest {
    private int auctionId;

    public JoinRequest() {}

    public JoinRequest(int auctionId) {
        this.auctionId = auctionId;
    }

    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }

    @Override
    public String toString() {
        return "JoinRequest{auctionId=" + auctionId + ", command='" + getCommand() + "'}";
    }
}