package org.example.service;

import org.example.AuctionEngine;
import org.example.dao.AuctionDAO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServiceTest {

    @Test
    void setEngineAcceptsEngineInstance() {
        AuctionService service = new AuctionService();
        AuctionEngine engine = new AuctionEngine(service, new AuctionDAO());

        assertDoesNotThrow(() -> service.setEngine(engine));
    }
}
