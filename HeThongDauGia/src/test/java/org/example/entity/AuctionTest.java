package org.example.entity;

import org.example.entity.item.Art;
import org.example.observer.BidObserver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    @Test
    void constructorAndSettersExposeValues() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(10);
        Auction auction = new Auction(1, 2, new BigDecimal("1000"), start, end, "RUNNING", 0);

        assertEquals(1, auction.getId());
        assertEquals(2, auction.getItemId());
        assertEquals(new BigDecimal("1000"), auction.getCurrentPrice());
        assertEquals(start, auction.getStartTime());
        assertEquals(end, auction.getEndTime());
        assertEquals("RUNNING", auction.getStatus());
        assertEquals(0, auction.getWinnerId());
        assertTrue(auction.getActive());
        assertTrue(auction.isActive());

        auction.setWinnerId(9);
        auction.setActive(false);

        assertEquals(9, auction.getWinnerId());
        assertFalse(auction.isActive());
    }

    @Test
    void notifyAuctionEndedMarksAuctionFinishedAndNotifiesObservers() {
        Auction auction = new Auction();
        auction.setId(3);
        auction.setActive(true);
        auction.setStatus("ACTIVE");
        RecordingObserver observer = new RecordingObserver();
        auction.addObserver(observer);

        auction.notifyAuctionEnded("alice", new BigDecimal("1500"));

        assertFalse(auction.getActive());
        assertEquals("FINISHED", auction.getStatus());
        assertEquals(List.of("ended:3:Phien #3:alice:1500"), observer.events);
    }

    @Test
    void getNameUsesItemNameOrFallbackIdAndObserversAvoidDuplicates() {
        Auction auction = new Auction();
        auction.setId(12);
        assertEquals("Phien #12", auction.getName());

        Art item = new Art();
        item.setName("Landscape");
        auction.setItem(item);
        assertEquals("Landscape", auction.getName());

        RecordingObserver observer = new RecordingObserver();
        auction.addObserver(observer);
        auction.addObserver(observer);
        assertEquals(1, auction.getObservers().size());
        auction.removeObserver(observer);
        assertTrue(auction.getObservers().isEmpty());
    }

    private static final class RecordingObserver implements BidObserver {
        private final List<String> events = new ArrayList<>();

        @Override
        public void onBidPlaced(int auctionId, String bidderUsername, BigDecimal newPrice) {
            events.add("bid:" + auctionId + ":" + bidderUsername + ":" + newPrice);
        }

        @Override
        public void onAuctionEnded(int auctionId, String auctionName,
                                   String winnerUsername, BigDecimal finalPrice) {
            events.add("ended:" + auctionId + ":" + auctionName + ":" + winnerUsername + ":" + finalPrice);
        }
    }
}
