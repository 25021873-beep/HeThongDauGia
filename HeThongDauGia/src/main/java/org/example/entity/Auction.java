package org.example.entity;

import org.example.entity.item.Item;
import org.example.network.ClientHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Auction {
    private int id;
    private int itemId;
    private BigDecimal currentPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private int winnerId;

    // Thêm: trạng thái đang chạy và thông tin Item
    private volatile boolean active;
    private Item item;

    // Thêm: danh sách client đang xem phòng (dùng cho Multicast)
    private final List<ClientHandler> viewers = new CopyOnWriteArrayList<>();

    public Auction() {}

    public Auction(int id, int itemId, BigDecimal currentPrice, LocalDateTime startTime,
                   LocalDateTime endTime, String status, int winnerId) {
        this.id = id;
        this.itemId = itemId;
        this.currentPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.winnerId = winnerId;
        this.active = true; // Mặc định khi tạo mới là đang chạy
    }

    // ── Methods cho ClientHandler & AuctionEngine ────────────────────────────

    /** Kiểm tra phiên còn hoạt động không */
    public boolean isActive() {
        return active && LocalDateTime.now().isBefore(endTime);
    }

    /** Chốt phiên khi hết giờ */
    public void endAuction() {
        this.active = false;
        this.status = "FINISHED";
    }

    /** Tên phiên - ưu tiên tên Item, fallback về ID */
    public String getName() {
        return (item != null) ? item.getName() : "Phien #" + id;
    }

    /** Thêm client vào phòng xem (lệnh JOIN) */
    public void addViewer(ClientHandler handler) {
        if (!viewers.contains(handler)) {
            viewers.add(handler);
        }
    }

    /** Xóa client khi ngắt kết nối (cleanUp) */
    public void removeViewer(ClientHandler handler) {
        viewers.remove(handler);
    }

    /** Lấy danh sách người xem để Multicast (lệnh BID) */
    public List<ClientHandler> getViewers() {
        return viewers;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getWinnerId() { return winnerId; }
    public void setWinnerId(int winnerId) { this.winnerId = winnerId; }

    public boolean getActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
}