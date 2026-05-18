package org.example.dto.request;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TopUpRequestTest {

    @Test
    void constructorAndSettersExposeValues() {
        TopUpRequest request = new TopUpRequest(2, new BigDecimal("50000"));

        assertEquals(2, request.getUserId());
        assertEquals(new BigDecimal("50000"), request.getAmount());

        request.setUserId(6);
        request.setAmount(new BigDecimal("75000"));

        assertEquals(6, request.getUserId());
        assertEquals(new BigDecimal("75000"), request.getAmount());
        assertEquals("TopUpRequest{userId=6, amount=75000}", request.toString());
    }
}
