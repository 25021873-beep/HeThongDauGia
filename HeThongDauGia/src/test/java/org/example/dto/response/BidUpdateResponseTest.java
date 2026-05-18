package org.example.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BidUpdateResponseTest {

    @Test
    void constructorExposesUpdateFields() {
        BidUpdateResponse response = new BidUpdateResponse(9, "bob", new BigDecimal("3000"));

        assertEquals(BaseResponse.STATUS_UPDATE, response.getStatus());
        assertEquals("Gia moi duoc cap nhat", response.getMessage());
        assertEquals(9, response.getAuctionId());
        assertEquals("bob", response.getBidderUsername());
        assertEquals(new BigDecimal("3000"), response.getNewPrice());
    }
}
