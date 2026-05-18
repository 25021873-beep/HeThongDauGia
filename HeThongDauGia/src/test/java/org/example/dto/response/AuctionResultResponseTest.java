package org.example.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionResultResponseTest {

    @Test
    void constructorFormatsWinnerResult() {
        AuctionResultResponse response = new AuctionResultResponse(
                3, "Painting", "alice", new BigDecimal("9000"),
                LocalDateTime.of(2026, 5, 18, 14, 30, 15));

        assertEquals(BaseResponse.STATUS_AUCTION_END, response.getStatus());
        assertEquals(3, response.getAuctionId());
        assertEquals("Painting", response.getAuctionName());
        assertEquals("alice", response.getWinnerUsername());
        assertEquals(new BigDecimal("9000"), response.getFinalPrice());
        assertEquals("2026-05-18T14:30:15", response.getEndTime());
        assertTrue(response.isHasWinner());
    }

    @Test
    void noWinnerFactoryMarksResultWithoutWinner() {
        AuctionResultResponse response = AuctionResultResponse.noWinner(
                4, "Clock", new BigDecimal("500"),
                LocalDateTime.of(2026, 5, 18, 15, 0));

        assertNull(response.getWinnerUsername());
        assertFalse(response.isHasWinner());
        assertEquals(new BigDecimal("500"), response.getFinalPrice());
    }
}
