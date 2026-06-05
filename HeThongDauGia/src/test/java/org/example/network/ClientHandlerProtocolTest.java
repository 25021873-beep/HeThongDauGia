package org.example.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.service.AuctionEngine;
import org.example.service.AuctionService;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerProtocolTest {

    @Test
    void handlerReturnsErrorForInvalidJsonMissingCommandAndUnknownCommand() throws Exception {
        try (SocketPair pair = SocketPair.open()) {
            ClientHandler handler = new ClientHandler(
                    pair.serverSide,
                    AuctionEngine.getInstance(),
                    UserService.getInstance(),
                    AuctionService.getInstance());

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(handler);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    pair.clientSide.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(
                    pair.clientSide.getOutputStream(), StandardCharsets.UTF_8), true);

            JsonObject welcome = readJson(in);
            assertEquals("SUCCESS", welcome.get("status").getAsString());

            out.println("khong-phai-json");
            JsonObject invalidJson = readJson(in);
            assertEquals("ERROR", invalidJson.get("status").getAsString());
            assertTrue(invalidJson.get("message").getAsString().contains("Dinh dang JSON"));

            out.println("{}");
            JsonObject missingCommand = readJson(in);
            assertEquals("ERROR", missingCommand.get("status").getAsString());
            assertTrue(missingCommand.get("message").getAsString().contains("command"));

            out.println("{\"command\":\"LENH_KHONG_TON_TAI\"}");
            JsonObject unknownCommand = readJson(in);
            assertEquals("ERROR", unknownCommand.get("status").getAsString());
            assertTrue(unknownCommand.get("message").getAsString().contains("Lenh khong hop le"));

            out.println("{\"command\":\"LOGOUT\"}");
            JsonObject logout = readJson(in);
            assertEquals("SUCCESS", logout.get("status").getAsString());
            assertTrue(logout.get("message").getAsString().contains("Dang xuat"));

            executor.shutdown();
            assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    private static JsonObject readJson(BufferedReader in) throws IOException {
        String line = in.readLine();
        assertNotNull(line, "Server phai gui response JSON mot dong");
        return JsonParser.parseString(line).getAsJsonObject();
    }

    private static final class SocketPair implements AutoCloseable {
        private final Socket clientSide;
        private final Socket serverSide;

        private SocketPair(Socket clientSide, Socket serverSide) {
            this.clientSide = clientSide;
            this.serverSide = serverSide;
        }

        static SocketPair open() throws IOException {
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                Socket client = new Socket("127.0.0.1", serverSocket.getLocalPort());
                Socket server = serverSocket.accept();
                client.setSoTimeout(2000);
                server.setSoTimeout(2000);
                return new SocketPair(client, server);
            }
        }

        @Override
        public void close() throws IOException {
            clientSide.close();
            if (!serverSide.isClosed()) {
                serverSide.close();
            }
        }
    }
}
