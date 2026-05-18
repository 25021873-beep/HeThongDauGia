package org.example.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AuctionSummaryTest {

    @Test
    void constructorExposesValues() {
        AuctionSummary summary = new AuctionSummary(6, "Camera", new BigDecimal("300"), "RUNNING");

        assertEquals(6, summary.getId());
        assertEquals("Camera", summary.getName());
        assertEquals(new BigDecimal("300"), summary.getCurrentPrice());
        assertEquals("RUNNING", summary.getStatus());
    }
}
