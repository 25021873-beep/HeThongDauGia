package org.example.service;

import org.example.entity.Auction;
import org.example.entity.item.Electronics;
import org.example.observer.BidObserver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AuctionRoomManagerTest {

    @Test
    void getOrCreateRoomReturnsSameRoomForSameAuctionId() {
        AuctionRoomManager manager = new AuctionRoomManager();
        Auction auction = auction(1, "Laptop");

        AuctionRoom first = manager.getOrCreateRoom(auction);
        AuctionRoom second = manager.getOrCreateRoom(auction);

        assertSame(first, second,
                "Moi auctionId chi nen co mot AuctionRoom de gom dung danh sach observer");
    }

    @Test
    void joinRoomRegistersObserverForThatAuction() {
        AuctionRoomManager manager = new AuctionRoomManager();
        CountingObserver observer = new CountingObserver();

        manager.joinRoom(auction(2, "Phone"), observer);
        manager.getRoom(2).notifyBidPlaced("bidder", new BigDecimal("1000"));

        assertEquals(1, observer.bidCount.get());
    }

    @Test
    void clearObserverFromAllRoomsRemovesDisconnectedClientEverywhere() {
        AuctionRoomManager manager = new AuctionRoomManager();
        CountingObserver observer = new CountingObserver();

        manager.joinRoom(auction(3, "Camera"), observer);
        manager.joinRoom(auction(4, "Watch"), observer);

        manager.clearObserverFromAllRooms(observer);
        manager.getRoom(3).notifyBidPlaced("bidder", new BigDecimal("1000"));
        manager.getRoom(4).notifyBidPlaced("bidder", new BigDecimal("2000"));

        assertEquals(0, observer.bidCount.get(),
                "Client disconnect khong duoc nhan push message nua");
    }

    @Test
    void removeRoomDeletesFinishedAuctionRoom() {
        AuctionRoomManager manager = new AuctionRoomManager();
        manager.getOrCreateRoom(auction(5, "Book"));

        manager.removeRoom(5);

        assertNull(manager.getRoom(5));
    }

    private static Auction auction(int id, String itemName) {
        Electronics item = new Electronics();
        item.setName(itemName);

        Auction auction = new Auction();
        auction.setId(id);
        auction.setItem(item);
        auction.setStatus("RUNNING");
        return auction;
    }

    private static final class CountingObserver implements BidObserver {
        private final AtomicInteger bidCount = new AtomicInteger();

        @Override
        public void onBidPlaced(int auctionId, String bidderUsername, BigDecimal newPrice) {
            bidCount.incrementAndGet();
        }

        @Override
        public void onAuctionEnded(int auctionId, String auctionName, String winnerUsername, BigDecimal finalPrice) {
        }

        @Override
        public void onAuctionExtended(int auctionId, LocalDateTime newEndTime, int addedSeconds) {
        }

        @Override
        public void onAuctionStarted(int auctionId, String auctionName) {
        }
    }
}
