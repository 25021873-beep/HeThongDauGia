package org.example.entity;

import org.example.entity.item.Art;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    void endAuctionMarksAuctionFinished() {
        Auction auction = new Auction();
        auction.setActive(true);
        auction.setStatus("RUNNING");

        auction.endAuction();

        assertFalse(auction.getActive());
        assertEquals("FINISHED", auction.getStatus());
    }

    @Test
    void getNameUsesItemNameOrFallbackIdAndViewersAvoidDuplicates() {
        Auction auction = new Auction();
        auction.setId(12);
        assertEquals("Phien #12", auction.getName());

        Art item = new Art();
        item.setName("Landscape");
        auction.setItem(item);
        assertEquals("Landscape", auction.getName());

        auction.addViewer(null);
        auction.addViewer(null);
        assertEquals(1, auction.getViewers().size());
        auction.removeViewer(null);
        assertTrue(auction.getViewers().isEmpty());
    }
}
