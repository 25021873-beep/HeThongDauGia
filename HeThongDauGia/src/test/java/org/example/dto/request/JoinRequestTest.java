package org.example.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JoinRequestTest {

    @Test
    void constructorAndSetterExposeAuctionId() {
        JoinRequest request = new JoinRequest(4);

        assertEquals(4, request.getAuctionId());

        request.setAuctionId(8);

        assertEquals(8, request.getAuctionId());
        assertEquals("JoinRequest{auctionId=8}", request.toString());
    }
}
