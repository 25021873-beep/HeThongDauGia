package org.example.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CreateAuctionResponseTest {

    @Test
    void constructorFormatsTimes() {
        CreateAuctionResponse response = new CreateAuctionResponse(
                5, "Phone", new BigDecimal("1000"),
                LocalDateTime.of(2026, 5, 18, 8, 0),
                LocalDateTime.of(2026, 5, 18, 10, 0));

        assertEquals(BaseResponse.STATUS_SUCCESS, response.getStatus());
        assertEquals(5, response.getAuctionId());
        assertEquals("Phone", response.getItemName());
        assertEquals(new BigDecimal("1000"), response.getStartingPrice());
        assertEquals("2026-05-18T08:00:00", response.getStartTime());
        assertEquals("2026-05-18T10:00:00", response.getEndTime());
    }
}
