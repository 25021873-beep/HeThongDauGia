package org.example.entity;

import org.example.entity.item.Item;
import org.example.observer.BidObserver;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


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

    // Thay List<ClientHandler> bằng List<BidObserver>
    private final List<BidObserver> observers = new CopyOnWriteArrayList<>();

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

    // ── Observer management ───────────────────────────────────────────────────

    /** Đăng ký observer — gọi khi client JOIN phòng */
    public void addObserver(BidObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /** Hủy đăng ký — gọi khi client LOGOUT hoặc ngắt kết nối */
    public void removeObserver(BidObserver observer) {
        observers.remove(observer);
    }

    public List<BidObserver> getObservers() {
        return observers;
    }

    // ── Notify (gọi từ BidController và AuctionEngine) ───────────────────────

    /**
     * Broadcast bid mới tới tất cả observer.
     * Gọi sau khi placeBid() thành công trong BidController.
     */
    public void notifyBidPlaced(String bidderUsername, BigDecimal newPrice) {
        for (BidObserver observer : observers) {
            try {
                observer.onBidPlaced(id, bidderUsername, newPrice);
            } catch (Exception e) {
                // Không để 1 observer lỗi làm hỏng các observer còn lại
                System.err.println("[AUCTION] notifyBidPlaced loi: " + e.getMessage());
            }
        }
    }

    /**
     * Broadcast kết thúc phiên tới tất cả observer.
     * Gọi từ AuctionEngine sau closeAuction().
     *
     * @param winnerUsername null nếu không có ai đặt giá
     */
    public void notifyAuctionEnded(String winnerUsername, BigDecimal finalPrice) {
        this.active = false;
        this.status = "FINISHED";
        for (BidObserver observer : observers) {
            try {
                observer.onAuctionEnded(id, getName(), winnerUsername, finalPrice);
            } catch (Exception e) {
                System.err.println("[AUCTION] notifyAuctionEnded loi: " + e.getMessage());
            }
        }
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