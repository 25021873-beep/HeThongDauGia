package org.example.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    @Test
    void constructorCreatesRunningAuctionAndActiveStatusReflectsStatus() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(10);
        Auction auction = new Auction(2, new BigDecimal("1000"), start, end);

        assertEquals(2, auction.getItemId());
        assertEquals(new BigDecimal("1000"), auction.getCurrentPrice());
        assertEquals(start, auction.getStartTime());
        assertEquals(end, auction.getEndTime());
        assertEquals("RUNNING", auction.getStatus());
        assertTrue(auction.isActive());

        auction.setStatus("FINISHED");

        assertFalse(auction.isActive());
    }

    @Test
    void setCurrentPriceRejectsNegativePrice() {
        Auction auction = new Auction();

        assertThrows(IllegalArgumentException.class,
                () -> auction.setCurrentPrice(new BigDecimal("-1")));
    }

    @Test
    void extendTimeAddsSecondsWhenEndTimeExists() {
        LocalDateTime end = LocalDateTime.now().plusMinutes(10);
        Auction auction = new Auction();
        auction.setEndTime(end);

        auction.extendTime(30);

        assertEquals(end.plusSeconds(30), auction.getEndTime());
    }
}
