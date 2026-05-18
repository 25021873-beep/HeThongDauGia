package org.example.dto.request;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CreateAuctionRequestTest {

    @Test
    void constructorAndSettersExposeValues() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 18, 10, 0);
        LocalDateTime end = start.plusHours(2);
        CreateAuctionRequest request = new CreateAuctionRequest(9, new BigDecimal("1000"), start, end);

        assertEquals(9, request.getItemId());
        assertEquals(new BigDecimal("1000"), request.getStartingPrice());
        assertEquals(start, request.getStartTime());
        assertEquals(end, request.getEndTime());

        request.setItemId(10);
        request.setStartingPrice(new BigDecimal("2000"));
        request.setStartTime(start.plusDays(1));
        request.setEndTime(end.plusDays(1));

        assertEquals(10, request.getItemId());
        assertEquals(new BigDecimal("2000"), request.getStartingPrice());
        assertTrue(request.toString().contains("itemId=10"));
        assertTrue(request.toString().contains("startingPrice=2000"));
    }
}
