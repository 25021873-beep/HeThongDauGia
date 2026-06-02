package org.example.network;

import org.example.service.AuctionEngine;
import org.example.service.AuctionService;
import org.example.service.AutoBidService;
import org.example.service.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuctionServer {
    private final int port;
    private final AuctionEngine engine;
    private final AuctionService auctionService;
    private final AutoBidService autoBidService;
    private final UserService userService;

    private final ExecutorService clientPool = Executors.newFixedThreadPool(50);

    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public AuctionServer(int port, AuctionEngine engine,
                         AuctionService auctionService,
                         AutoBidService autoBidService,
                         UserService userService) {
        this.port = port;
        this.engine = engine;
        this.auctionService = auctionService;
        this.autoBidService = autoBidService;
        this.userService = userService;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("[SERVER] Dang chay tren cong " + port);

            // Bắt sự kiện tắt máy để dọn rác
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] Client ket noi: " + clientSocket.getInetAddress());

                    // Đã dùng Handler xịn gộp chung
                    clientPool.submit(new ClientHandler(
                            clientSocket, engine, userService, auctionService, autoBidService));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[SERVER] Loi chap nhan ket noi: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Khong the khoi dong server: " + e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
        System.out.println("[SERVER] Dang tat server...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Loi khi dong ServerSocket: " + e.getMessage());
        }

        clientPool.shutdown();
        try {
            if (!clientPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        autoBidService.shutdown();
        System.out.println("[SERVER] Server da tat hoan toan");
    }
}