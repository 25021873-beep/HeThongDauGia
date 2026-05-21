package org.example.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuctionListResponseTest {

    @Test
    void constructorCalculatesCountAndKeepsAuctions() {
        List<AuctionSummary> auctions = List.of(
                new AuctionSummary(1, "Phone", new BigDecimal("100"), "RUNNING"),
                new AuctionSummary(2, "Bike", new BigDecimal("200"), "OPEN"));

        AuctionListResponse response = new AuctionListResponse(auctions);

        assertEquals(BaseResponse.STATUS_LIST, response.getStatus());
        assertEquals("Danh sach phien dau gia", response.getMessage());
        assertEquals(2, response.getCount());
        assertSame(auctions, response.getAuctions());
        assertTrue(response.serialize().contains("\"count\":2"));
    }
}
