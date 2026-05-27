package org.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AutoBidConfig extends BaseEntity{
    private int auctionId;
    private int bidderId;
    private BigDecimal maxBid;
    private BigDecimal increment;
    private LocalDateTime createdAt;
    private boolean active;

    public AutoBidConfig(){};

    public AutoBidConfig(int auctionId, int bidderId, BigDecimal maxBid, BigDecimal increment, LocalDateTime createdAt, boolean active) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        String sql = "UPDATE auto_bids SET is_active = FALSE WHERE id = ?";
        this.increment = increment;
        this.createdAt = createdAt;
        this.active = active;
    }

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

    public BigDecimal getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(BigDecimal maxBid) {
        this.maxBid = maxBid;
    }

    public BigDecimal getIncrement() {
        return increment;
    }

    public void setIncrement(BigDecimal increment) {
        this.increment = increment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
