package org.example.network;

import org.example.AuctionEngine;
import org.example.dao.AuctionDAO;
import org.example.service.AuctionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServerTest {

    @Test
    void constructorDoesNotOpenPort() {
        AuctionEngine engine = new AuctionEngine(new AuctionService(), new AuctionDAO());

        assertDoesNotThrow(() -> new AuctionServer(0, engine));
    }
}
