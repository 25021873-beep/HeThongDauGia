package org.example.network;


import org.example.service.AuctionEngine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AuctionServer {

    private final int port;
    private final AuctionEngine engine;

    public AuctionServer(int port, AuctionEngine engine) {
        this.port = port;
        this.engine = engine;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(clientSocket, engine);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Lỗi mạng: " + e.getMessage());
        }
    }
}

