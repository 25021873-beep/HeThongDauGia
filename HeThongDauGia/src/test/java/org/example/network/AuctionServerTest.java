package org.example.network;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class AuctionServerTest {

    @Test
    void testServerStartAndConnect() throws InterruptedException {
        AuctionServer server = new AuctionServer(9999, null, null, null, null);
        new Thread(server::start).start();

        Thread.sleep(500);

        try (Socket testSocket = new Socket("localhost", 9999)) {
            assertTrue(testSocket.isConnected(), "Cam cap phai thong!");
        } catch (IOException e) {
            fail("Server sap cmnr deo ket noi duoc: " + e.getMessage());
        } finally {
            try {
                server.shutdown();
            } catch (NullPointerException e) {
            }
        }
    }
}