package org.example.entity;

import org.example.entity.item.Item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Auction extends BaseEntity {

    private int           id;
    private int           itemId;
    private BigDecimal    currentPrice;
    private BigDecimal    startingPrice;
    private BigDecimal    stepPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String        status;   // OPEN, RUNNING, FINISHED, PAID, CANCELED
    private int           sellerId;
    private int           winnerId;
    private Item          item;

    // --- CONSTRUCTORS ---
    public Auction() {}

    public Auction(int id, int itemId, BigDecimal currentPrice, BigDecimal startingPrice, BigDecimal stepPrice, LocalDateTime startTime, LocalDateTime endTime, String status, int sellerId, int winnerId, Item item) {
        this.id = id;
        this.itemId = itemId;
        this.currentPrice = currentPrice;
        this.startingPrice = startingPrice;
        this.stepPrice = stepPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.sellerId = sellerId;
        this.winnerId = winnerId;
        this.item = item;
    }

    // --- BUSINESS METHODS ---

    public boolean isActive() {
        return "RUNNING".equals(this.status);
    }

    public void extendTime(int addedSeconds) {
        if (this.endTime != null) {
            this.endTime = this.endTime.plusSeconds(addedSeconds);
        }
    }



    // --- GETTERS & SETTERS ---

    public int getId()                           { return id; }
    public void setId(int id)                    { this.id = id; }

    public String getStatus()                    { return status; }
    public void setStatus(String status)         { this.status = status; }

    public LocalDateTime getEndTime()            { return endTime; }
    public void setEndTime(LocalDateTime endTime){ this.endTime = endTime; }

    public int getItemId()                       { return itemId; }
    public void setItemId(int itemId)            { this.itemId = itemId; }

    public BigDecimal getCurrentPrice()          { return currentPrice; }
    public void setCurrentPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Gia dau khong duoc am!");
        }
        this.currentPrice = price;
    }

    public LocalDateTime getStartTime()                { return startTime; }
    public void setStartTime(LocalDateTime startTime)  { this.startTime = startTime; }

    public int getWinnerId()                     { return winnerId; }
    public void setWinnerId(int winnerId)        { this.winnerId = winnerId; }

    public Item getItem()                        { return item; }
    public void setItem(Item item)               { this.item = item; }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public BigDecimal getStepPrice() {
        return stepPrice;
    }

    public void setStepPrice(BigDecimal stepPrice) {
        this.stepPrice = stepPrice;
    }
}