package com.auction.core.network;

import com.auction.core.engine.AuctionEngine;

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
            System.out.println("[SERVER] Đã mở cổng " + port + ". Đang chờ kết nối...");

            // Vòng lặp chính tiếp nhận kết nối từ Client
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Có kết nối mới từ IP: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(clientSocket, engine);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Lỗi mạng: " + e.getMessage());
        }
    }
}