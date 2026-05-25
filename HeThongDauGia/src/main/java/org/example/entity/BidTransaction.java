package org.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidTransaction extends BaseEntity {
    private int auctionId;
    private int bidderId;
    private BigDecimal bidPrice;
    private LocalDateTime bidTime;

    public BidTransaction() {}

    public BidTransaction(int id, int auctionId, int bidderId, BigDecimal bidPrice, LocalDateTime bidTime) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidPrice = bidPrice;
        this.bidTime = bidTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(BigDecimal bidPrice) {
        this.bidPrice = bidPrice;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }
}


