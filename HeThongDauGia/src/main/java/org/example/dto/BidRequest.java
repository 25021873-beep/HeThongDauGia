package org.example.dto;

import java.math.BigDecimal;

public class BidRequest {
    private int auctionId;
    private BigDecimal amount;

    public BidRequest() {}

    public BidRequest(int auctionId, BigDecimal amount) {
        this.auctionId = auctionId;
        this.amount = amount;
    }

    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    @Override
    public String toString() {
        return "BidRequest{auctionId=" + auctionId + ", amount=" + amount + "}";
    }
}