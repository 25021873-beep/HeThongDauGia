package org.example.entity;

import org.example.entity.item.Item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Auction {

    private int           id;
    private int           itemId;
    private BigDecimal    currentPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String        status;
    private int           winnerId;
    private volatile boolean active;
    private Item          item;

    public Auction() {}

    public Auction(int id, int itemId, BigDecimal currentPrice,
                   LocalDateTime startTime, LocalDateTime endTime,
                   String status, int winnerId) {
        this.id           = id;
        this.itemId       = itemId;
        this.currentPrice = currentPrice;
        this.startTime    = startTime;
        this.endTime      = endTime;
        this.status       = status;
        this.winnerId     = winnerId;
        this.active       = true;
    }

    // ── Business methods ──────────────────────────────────────────────────────

    public boolean isActive() {
        return active && endTime != null && LocalDateTime.now().isBefore(endTime);
    }

    public String getName() {
        return (item != null) ? item.getName() : "Phien #" + id;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int           getId()                              { return id; }
    public void          setId(int id)                        { this.id = id; }

    public int           getItemId()                          { return itemId; }
    public void          setItemId(int itemId)                { this.itemId = itemId; }

    public BigDecimal    getCurrentPrice()                    { return currentPrice; }
    public void          setCurrentPrice(BigDecimal p)        { this.currentPrice = p; }

    public LocalDateTime getStartTime()                       { return startTime; }
    public void          setStartTime(LocalDateTime t)        { this.startTime = t; }

    public LocalDateTime getEndTime()                         { return endTime; }
    public void          setEndTime(LocalDateTime t)          { this.endTime = t; }

    public String        getStatus()                          { return status; }
    public void          setStatus(String status)             { this.status = status; }

    public int           getWinnerId()                        { return winnerId; }
    public void          setWinnerId(int winnerId)            { this.winnerId = winnerId; }

    public boolean       getActive()                          { return active; }
    public void          setActive(boolean active)            { this.active = active; }

    public Item          getItem()                            { return item; }
    public void          setItem(Item item)                   { this.item = item; }
}