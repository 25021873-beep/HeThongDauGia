package org.example.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BidTransactionTest {

    @Test
    void constructorAndSettersExposeValues() {
        LocalDateTime bidTime = LocalDateTime.of(2026, 5, 18, 11, 30);
        BidTransaction bid = new BidTransaction(1, 2, 3, new BigDecimal("4500"), bidTime);

        assertEquals(1, bid.getId());
        assertEquals(2, bid.getAuctionId());
        assertEquals(3, bid.getBidderId());
        assertEquals(new BigDecimal("4500"), bid.getBidPrice());
        assertEquals(bidTime, bid.getBidTime());

        bid.setId(9);
        bid.setAuctionId(8);
        bid.setBidderId(7);
        bid.setBidPrice(new BigDecimal("5500"));
        bid.setBidTime(bidTime.plusMinutes(5));

        assertEquals(9, bid.getId());
        assertEquals(8, bid.getAuctionId());
        assertEquals(7, bid.getBidderId());
        assertEquals(new BigDecimal("5500"), bid.getBidPrice());
        assertEquals(bidTime.plusMinutes(5), bid.getBidTime());
    }
}
