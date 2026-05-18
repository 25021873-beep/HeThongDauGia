package org.example.network;

import org.example.service.AuctionEngine;
import org.example.service.AuctionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServerTest {

    @Test
    void constructorDoesNotOpenPort() {
        AuctionEngine engine = AuctionEngine.getInstance();

        assertDoesNotThrow(() -> new AuctionServer(0, engine));
    }
}
