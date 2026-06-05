package org.example.network;

import org.example.dao.AuctionDAO;
import org.example.dao.AutoBidDAO;
import org.example.dao.user.UserDAO;
import org.example.service.AuctionEngine;
import org.example.service.AuctionService;
import org.example.service.AutoBidService;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class AuctionServerTest {

    @Test
    void serverStartsOnEphemeralPortAndAcceptsConnection() throws InterruptedException {
        AuctionService auctionService = AuctionService.getInstance();
        AutoBidService autoBidService = new AutoBidService(
                new AutoBidDAO(), auctionService, new AuctionDAO(), new UserDAO());
        AuctionServer server = new AuctionServer(
                0, AuctionEngine.getInstance(), auctionService, autoBidService, UserService.getInstance());

        Thread serverThread = new Thread(server::start, "auction-server-test");
        serverThread.start();

        int port = waitForBoundPort(server);

        try (Socket testSocket = new Socket("127.0.0.1", port)) {
            assertTrue(testSocket.isConnected(), "Server should accept a local TCP connection");
        } catch (IOException e) {
            fail("Server did not accept local connection: " + e.getMessage());
        } finally {
            server.shutdown();
            serverThread.join(1000);
            autoBidService.shutdown();
        }
    }

    private static int waitForBoundPort(AuctionServer server) throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            int port = server.getBoundPortForTesting();
            if (port > 0) {
                return port;
            }
            Thread.sleep(50);
        }
        fail("Server did not bind a port in time");
        return -1;
    }
}
