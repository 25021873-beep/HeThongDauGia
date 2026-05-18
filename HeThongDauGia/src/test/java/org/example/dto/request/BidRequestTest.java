package org.example.dto.request;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BidRequestTest {

    @Test
    void constructorAndSettersExposeValues() {
        BidRequest request = new BidRequest(3, new BigDecimal("1500"));

        assertEquals(3, request.getAuctionId());
        assertEquals(new BigDecimal("1500"), request.getAmount());

        request.setAuctionId(5);
        request.setAmount(new BigDecimal("2500"));

        assertEquals(5, request.getAuctionId());
        assertEquals(new BigDecimal("2500"), request.getAmount());
        assertEquals("BidRequest{auctionId=5, amount=2500}", request.toString());
    }
}
