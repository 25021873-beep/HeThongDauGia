package org.example.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServiceTest {

    @Test
    void setEngineAcceptsEngineInstance() {
        AuctionService service = AuctionService.getInstance();
        AuctionEngine engine = AuctionEngine.getInstance();

        assertDoesNotThrow(() -> service.setEngine(engine));
    }
}
