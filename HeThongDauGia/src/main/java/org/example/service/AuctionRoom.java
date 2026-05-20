package org.example.service;

import org.example.observer.BidObserver;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Đại diện cho một "phòng" đấu giá trên server.
 * Quản lý danh sách observer (ClientHandler) đang theo dõi phiên này.
 */
public class AuctionRoom {

    private final int               auctionId;
    private final String            auctionName;
    private final List<BidObserver> observers = new CopyOnWriteArrayList<>();

    public AuctionRoom(int auctionId, String auctionName) {
        this.auctionId   = auctionId;
        this.auctionName = auctionName;
    }

    // ── Observer management ───────────────────────────────────────────────────

    public void addObserver(BidObserver o) {
        if (!observers.contains(o)) observers.add(o);
    }

    public void removeObserver(BidObserver o) {
        observers.remove(o);
    }

    // ── Notify ────────────────────────────────────────────────────────────────

    public void notifyBidPlaced(String bidder, BigDecimal price) {
        for (BidObserver o : observers) {
            try { o.onBidPlaced(auctionId, bidder, price); }
            catch (Exception e) {
                System.err.println("[ROOM] notifyBidPlaced loi: " + e.getMessage());
            }
        }
    }

    public void notifyAuctionEnded(String winner, BigDecimal finalPrice) {
        for (BidObserver o : observers) {
            try { o.onAuctionEnded(auctionId, auctionName, winner, finalPrice); }
            catch (Exception e) {
                System.err.println("[ROOM] notifyAuctionEnded loi: " + e.getMessage());
            }
        }
    }

    /** Gọi sau khi anti-snipe kích hoạt — broadcast thời gian mới tới toàn phòng */
    public void notifyAuctionExtended(LocalDateTime newEndTime, int addedSeconds) {
        for (BidObserver o : observers) {
            try { o.onAuctionExtended(auctionId, newEndTime, addedSeconds); }
            catch (Exception e) {
                System.err.println("[ROOM] notifyAuctionExtended loi: " + e.getMessage());
            }
        }
    }
}