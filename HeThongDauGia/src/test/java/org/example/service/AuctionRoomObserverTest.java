package org.example.service;

import org.example.observer.BidObserver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuctionRoomObserverTest {

    @Test
    void notifyBidPlacedSendsUpdateToAllObservers() {
        AuctionRoom room = new AuctionRoom(10, "Laptop Dell");
        RecordingObserver first = new RecordingObserver();
        RecordingObserver second = new RecordingObserver();

        room.addObserver(first);
        room.addObserver(second);

        room.notifyBidPlaced("bidderA", new BigDecimal("1500000"));

        assertEquals(List.of("bid:10:bidderA:1500000"), first.events);
        assertEquals(List.of("bid:10:bidderA:1500000"), second.events);
    }

    @Test
    void addObserverDoesNotRegisterDuplicateObserver() {
        AuctionRoom room = new AuctionRoom(11, "Phone");
        RecordingObserver observer = new RecordingObserver();

        room.addObserver(observer);
        room.addObserver(observer);
        room.notifyBidPlaced("bidderB", new BigDecimal("2000000"));

        assertEquals(1, observer.events.size(),
                "Mot client JOIN lai khong duoc nhan thong bao lap nhieu lan");
    }

    @Test
    void removeObserverStopsFutureNotifications() {
        AuctionRoom room = new AuctionRoom(12, "Camera");
        RecordingObserver observer = new RecordingObserver();

        room.addObserver(observer);
        room.notifyBidPlaced("bidderC", new BigDecimal("3000000"));
        room.removeObserver(observer);
        room.notifyBidPlaced("bidderD", new BigDecimal("3500000"));

        assertEquals(List.of("bid:12:bidderC:3000000"), observer.events);
    }

    @Test
    void brokenObserverDoesNotBlockOtherObservers() {
        AuctionRoom room = new AuctionRoom(13, "Watch");
        RecordingObserver goodObserver = new RecordingObserver();

        room.addObserver(new ThrowingObserver());
        room.addObserver(goodObserver);

        assertDoesNotThrow(() -> room.notifyBidPlaced("bidderE", new BigDecimal("4000000")));
        assertEquals(List.of("bid:13:bidderE:4000000"), goodObserver.events);
    }

    @Test
    void notifyLifecycleEventsToObservers() {
        AuctionRoom room = new AuctionRoom(14, "Painting");
        RecordingObserver observer = new RecordingObserver();
        LocalDateTime newEndTime = LocalDateTime.of(2026, 6, 4, 20, 1);

        room.addObserver(observer);
        room.notifyAuctionStarted();
        room.notifyAuctionExtended(newEndTime, 120);
        room.notifyAuctionEnded("winner1", new BigDecimal("9000000"));

        assertEquals(List.of(
                "started:14:Painting",
                "extended:14:2026-06-04T20:01:120",
                "ended:14:Painting:winner1:9000000"
        ), observer.events);
    }

    private static final class RecordingObserver implements BidObserver {
        private final List<String> events = new ArrayList<>();

        @Override
        public void onBidPlaced(int auctionId, String bidderUsername, BigDecimal newPrice) {
            events.add("bid:" + auctionId + ":" + bidderUsername + ":" + newPrice);
        }

        @Override
        public void onAuctionEnded(int auctionId, String auctionName, String winnerUsername, BigDecimal finalPrice) {
            events.add("ended:" + auctionId + ":" + auctionName + ":" + winnerUsername + ":" + finalPrice);
        }

        @Override
        public void onAuctionExtended(int auctionId, LocalDateTime newEndTime, int addedSeconds) {
            events.add("extended:" + auctionId + ":" + newEndTime + ":" + addedSeconds);
        }

        @Override
        public void onAuctionStarted(int auctionId, String auctionName) {
            events.add("started:" + auctionId + ":" + auctionName);
        }
    }

    private static final class ThrowingObserver implements BidObserver {
        @Override
        public void onBidPlaced(int auctionId, String bidderUsername, BigDecimal newPrice) {
            throw new RuntimeException("Client socket da dong");
        }

        @Override
        public void onAuctionEnded(int auctionId, String auctionName, String winnerUsername, BigDecimal finalPrice) {
            throw new RuntimeException("Client socket da dong");
        }

        @Override
        public void onAuctionExtended(int auctionId, LocalDateTime newEndTime, int addedSeconds) {
            throw new RuntimeException("Client socket da dong");
        }

        @Override
        public void onAuctionStarted(int auctionId, String auctionName) {
            throw new RuntimeException("Client socket da dong");
        }
    }
}
