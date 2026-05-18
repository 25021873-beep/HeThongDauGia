package org.example.network;

import org.example.AuctionEngine;
import org.example.dao.AuctionDAO;
import org.example.dto.response.SimpleResponse;
import org.example.service.AuctionService;
import org.junit.jupiter.api.Test;

import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {

    @Test
    void newHandlerStartsAsGuestAndCanIgnoreSendBeforeRun() {
        AuctionEngine engine = new AuctionEngine(new AuctionService(), new AuctionDAO());
        ClientHandler handler = new ClientHandler(new Socket(), engine);

        assertEquals("Guest", handler.getUsername());
        assertDoesNotThrow(() -> handler.send(SimpleResponse.info("hello")));
    }
}
