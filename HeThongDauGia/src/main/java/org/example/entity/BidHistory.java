package org.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidHistory extends BaseEntity {
    private int auctionId;
    private int bidderId;
    private String bidderUsername;
    private BigDecimal price;
    private LocalDateTime bidTime;

    public BidHistory(int auctionId, int bidderId, String bidderUsername, BigDecimal price, LocalDateTime bidTime) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidderUsername = bidderUsername;
        this.price = price;
        this.bidTime = bidTime;
    }

    public BidHistory(){};

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public int getBidderId() {
        return bidderId;
    }

    public void setBidderId(int bidderId) {
        this.bidderId = bidderId;
    }

    public String getBidderUsername() {
        return bidderUsername;
    }

    public void setBidderUsername(String bidderUsername) {
        this.bidderUsername = bidderUsername;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }
}
