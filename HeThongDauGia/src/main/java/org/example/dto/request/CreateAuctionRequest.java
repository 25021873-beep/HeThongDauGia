package org.example.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateAuctionRequest {
    private int itemId;
    private BigDecimal startingPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public CreateAuctionRequest() {}

    public CreateAuctionRequest(int itemId, BigDecimal startingPrice,
                                LocalDateTime startTime, LocalDateTime endTime) {
        this.itemId = itemId;
        this.startingPrice = startingPrice;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    @Override
    public String toString() {
        return "CreateAuctionRequest{itemId=" + itemId +
                ", startingPrice=" + startingPrice +
                ", startTime=" + startTime +
                ", endTime=" + endTime + "}";
    }
}
