package org.example.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JoinResponseTest {

    @Test
    void constructorFormatsEndTime() {
        JoinResponse response = new JoinResponse(
                4, "Auction room", new BigDecimal("700"),
                LocalDateTime.of(2026, 5, 18, 20, 45), "RUNNING");

        assertEquals(BaseResponse.STATUS_SUCCESS, response.getStatus());
        assertEquals("Vao phong thanh cong", response.getMessage());
        assertEquals(4, response.getAuctionId());
        assertEquals("Auction room", response.getAuctionName());
        assertEquals(new BigDecimal("700"), response.getCurrentPrice());
        assertEquals("2026-05-18T20:45:00", response.getEndTime());
        assertEquals("RUNNING", response.getAuctionStatus());
    }
}
