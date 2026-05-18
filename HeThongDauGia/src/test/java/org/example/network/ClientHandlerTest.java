package org.example.network;

import org.example.dto.response.SimpleResponse;
import org.example.service.AuctionEngine;
import org.example.service.AuctionService;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;

import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {

    @Test
    void newHandlerStartsAsGuestAndCanIgnoreSendBeforeRun() {
        ClientHandler handler = new ClientHandler(
                new Socket(),
                AuctionEngine.getInstance(),
                UserService.getInstance(),
                AuctionService.getInstance());

        assertDoesNotThrow(() -> handler.send(SimpleResponse.info("hello")));
    }
}
