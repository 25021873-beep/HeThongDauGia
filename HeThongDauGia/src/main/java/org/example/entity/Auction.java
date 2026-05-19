package org.example.entity;

import org.example.entity.BaseEntity;
import org.example.entity.item.Item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Auction extends BaseEntity {
    // --- THUỘC TÍNH CỐT LÕI (Mapping 1-1 với Database) ---
    private int id;
    private int itemId;
    private BigDecimal currentPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // Trạng thái: RUNNING, FINISHED, PAID, CANCELED
    private int winnerId;

    private Item item;

    // --- CONSTRUCTORS ---
    public Auction() {}

    public Auction(int itemId, BigDecimal startingPrice, LocalDateTime startTime, LocalDateTime endTime) {
        this.itemId = itemId;
        this.currentPrice = startingPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "RUNNING"; // Tạo mới thì mặc định chưa chạy
    }

    // --- GETTER & SETTER CƠ BẢN (Mày tự dùng IDE generate thêm cho đủ) ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public boolean isActive() {
        return "RUNNING".equals(this.status);
    }

    public void extendTime(int addedSeconds) {
        if (this.endTime != null) {
            this.endTime = this.endTime.plusSeconds(addedSeconds);
        }
    }

    public void setCurrentPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Lỗi: Giá đấu không được âm!");
        }
        this.currentPrice = price;
    }
}