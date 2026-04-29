package com.auction.core.network;

import com.auction.core.engine.AuctionEngine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AuctionServer {
    private final int port;
    private final AuctionEngine engine;

    // Khi khởi tạo Server, phải cung cấp Cổng (Port) và Động cơ lõi (Engine)
    public AuctionServer(int port, AuctionEngine engine) {
        this.port = port;
        this.engine = engine;
    }

    public void start() {
        // Mở cổng mạng
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🌐 [SERVER] Đã mở cổng " + port + ". Đang chờ Client kết nối tới...");

            // Vòng lặp vô tận: Luôn luôn thức để lắng nghe kết nối mới
            while (true) {
                // Lệnh accept() sẽ "đóng băng" ở đây cho đến khi có 1 Client kết nối vào
                Socket clientSocket = serverSocket.accept();
                System.out.println("🤝 [SERVER] Có Client mới kết nối từ IP: " + clientSocket.getInetAddress().getHostAddress());

                // Có khách tới -> Tạo ngay một "Giao dịch viên" (ClientHandler) và cho chạy ở một Luồng (Thread) riêng
                ClientHandler handler = new ClientHandler(clientSocket, engine);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("❌ [SERVER] Lỗi cổng mạng: " + e.getMessage());
        }
    }
}