package org.example.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageTest {

    @Test
    void constructorSetsFieldsAndTimestamp() {
        long before = System.currentTimeMillis();

        Message message = new Message(Message.LOGIN, "alice", "client-1");

        assertEquals(Message.LOGIN, message.getType());
        assertEquals("alice", message.getPayload());
        assertEquals("client-1", message.getSenderId());
        assertTrue(message.getTimestamp() >= before);
        assertTrue(message.getTimestamp() <= System.currentTimeMillis());
    }

    @Test
    void toJsonAndFromJsonRoundTrip() {
        Message original = new Message(Message.PLACE_BID, "{\"auctionId\":\"A1\",\"price\":100}", "alice");

        String json = original.toJson();
        Message parsed = Message.fromJson(json);

        assertNotNull(json);
        assertEquals(original.getType(), parsed.getType());
        assertEquals(original.getPayload(), parsed.getPayload());
        assertEquals(original.getSenderId(), parsed.getSenderId());
        assertEquals(original.getTimestamp(), parsed.getTimestamp());
    }

    @Test
    void messageTypeConstantsHaveExpectedValues() {
        assertEquals("LOGIN", Message.LOGIN);
        assertEquals("LOGIN_OK", Message.LOGIN_OK);
        assertEquals("LOGIN_FAIL", Message.LOGIN_FAIL);
        assertEquals("PLACE_BID", Message.PLACE_BID);
        assertEquals("BID_UPDATE", Message.BID_UPDATE);
        assertEquals("BID_REJECTED", Message.BID_REJECTED);
        assertEquals("JOIN_AUCTION", Message.JOIN_AUCTION);
        assertEquals("AUCTION_END", Message.AUCTION_END);
        assertEquals("GET_AUCTIONS", Message.GET_AUCTIONS);
        assertEquals("AUCTION_LIST", Message.AUCTION_LIST);
        assertEquals("ERROR", Message.ERROR);
    }
}
