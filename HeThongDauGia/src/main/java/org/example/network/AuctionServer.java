package org.example.network;

import org.example.service.AuctionEngine;
import org.example.service.AuctionService;
import org.example.service.AutoBidService;
import org.example.service.UserService;
import org.example.utils.DatabaseConnection;
import org.example.dao.AutoBidDAO;
import org.example.dao.AuctionDAO;
import org.example.dao.user.UserDAO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuctionServer {

    private final int            port;
    private final AuctionEngine  engine;
    private final AuctionService auctionService;
    private final AutoBidService autoBidService;
    private final UserService    userService;

    // Giới hạn tối đa 50 client cùng lúc
    private final ExecutorService clientPool = Executors.newFixedThreadPool(50);

    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public AuctionServer(int port, AuctionEngine engine,
                         AuctionService auctionService,
                         AutoBidService autoBidService,
                         UserService userService) {
        this.port           = port;
        this.engine         = engine;
        this.auctionService = auctionService;
        this.autoBidService = autoBidService;
        this.userService    = userService;
    }

    /**
     * Backwards-compatible constructor used by tests / simple instantiation.
     * It will create default service/DAO instances.
     */
    public AuctionServer(int port, AuctionEngine engine) {
        this(port,
                engine,
                AuctionService.getInstance(),
                new AutoBidService(new AutoBidDAO(), AuctionService.getInstance(), new AuctionDAO(), new UserDAO()),
                UserService.getInstance());
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running      = true;
            System.out.println("[SERVER] Dang chay tren cong " + port);

            // Đăng ký shutdown hook — dọn dẹp khi Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] Client ket noi: " + clientSocket.getInetAddress());

                    ClientHandler handler = new ClientHandler(
                            clientSocket, engine,
                            userService, auctionService, autoBidService);

                    // Submit vào pool thay vì new Thread()
                    clientPool.submit(handler);

                } catch (IOException e) {
                    if (running) {
                        System.err.println("[SERVER] Loi chap nhan ket noi: " + e.getMessage());
                    }
                    // Nếu !running nghĩa là đang shutdown → thoát bình thường
                }
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Khong the khoi dong server: " + e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
        System.out.println("[SERVER] Dang tat server...");

        // 1. Đóng ServerSocket — ngắt vòng lặp accept()
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Loi khi dong ServerSocket: " + e.getMessage());
        }

        // 2. Dừng thread pool client
        clientPool.shutdown();
        try {
            if (!clientPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientPool.shutdownNow();
        }

        // 3. Dừng auto-bid executor
        autoBidService.shutdown();

        // 4. Đóng DB
        DatabaseConnection.getInstance().close();

        System.out.println("[SERVER] Server da tat hoan toan");
    }
}