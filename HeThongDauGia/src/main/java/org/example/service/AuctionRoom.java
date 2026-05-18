package org.example.service;

import org.example.entity.Auction;
import org.example.observer.BidObserver;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AuctionRoom đóng vai trò là một "vỏ bọc" (Wrapper) cho Auction entity.
 * Nó tách biệt logic quản lý người xem (Observers) ra khỏi dữ liệu lõi của Auction.
 */
public class AuctionRoom {
    private final Auction auction;
    private final List<BidObserver> observers = new CopyOnWriteArrayList<>();

    public AuctionRoom(Auction auction) {
        this.auction = auction;
    }

    public Auction getAuction() {
        return auction;
    }

    public void addObserver(BidObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(BidObserver observer) {
        observers.remove(observer);
    }

    public void notifyBidPlaced(String bidderUsername, BigDecimal newPrice) {
        for (BidObserver observer : observers) {
            try {
                observer.onBidPlaced(auction.getId(), bidderUsername, newPrice);
            } catch (Exception e) {
                System.err.println("[ROOM] notifyBidPlaced lỗi cho client: " + e.getMessage());
            }
        }
    }

    public void notifyAuctionEnded(String winnerUsername, BigDecimal finalPrice) {
        for (BidObserver observer : observers) {
            try {
                observer.onAuctionEnded(auction.getId(), auction.getName(), winnerUsername, finalPrice);
            } catch (Exception e) {
                System.err.println("[ROOM] notifyAuctionEnded lỗi cho client: " + e.getMessage());
            }
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}