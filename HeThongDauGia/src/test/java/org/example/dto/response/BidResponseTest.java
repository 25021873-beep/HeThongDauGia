package org.example.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BidResponseTest {

    @Test
    void constructorFormatsBidTime() {
        BidResponse response = new BidResponse(
                8, "alice", new BigDecimal("1250"),
                LocalDateTime.of(2026, 5, 18, 9, 5, 7));

        assertEquals(BaseResponse.STATUS_SUCCESS, response.getStatus());
        assertEquals("Dat gia thanh cong", response.getMessage());
        assertEquals(8, response.getAuctionId());
        assertEquals("alice", response.getBidderUsername());
        assertEquals(new BigDecimal("1250"), response.getAmount());
        assertEquals("2026-05-18T09:05:07", response.getBidTime());
    }
}
