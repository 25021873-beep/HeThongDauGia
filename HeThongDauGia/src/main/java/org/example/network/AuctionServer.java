package org.example.network;


import org.example.service.AuctionEngine;
import org.example.service.AuctionService;
import org.example.service.UserService;

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
        UserService userService = UserService.getInstance();
        AuctionService auctionService = AuctionService.getInstance();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(clientSocket, this.engine, userService, auctionService);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Lỗi mạng: " + e.getMessage());
        }
    }
}

