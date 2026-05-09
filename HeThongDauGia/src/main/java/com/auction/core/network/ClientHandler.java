package com.auction.core.network;

import com.auction.core.engine.AuctionEngine;
import com.auction.core.model.Auction;
import com.auction.core.model.User;
import com.auction.core.service.AuthService;
import com.auction.core.service.AuctionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * TẦNG 4 & 5: Xử lý giao tiếp Socket và điều phối luồng dữ liệu
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final AuctionEngine engine;
    private final AuthService authService;     // Task 3.2
    private final AuctionService auctionService; // Task 3.3

    private PrintWriter out;
    private User currentUser; // Lưu thông tin sau khi LOGIN thành công[cite: 1]

    public ClientHandler(Socket socket, AuctionEngine engine) {
        this.clientSocket = socket;
        this.engine = engine;
        this.authService = new AuthService();
        this.auctionService = new AuctionService(engine);
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            sendMessage("SUCCESS|Ket noi Server thanh cong");

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                // Task 4.3: Bộ giải mã (Parser)[cite: 1]
                String[] parts = clientMessage.split("\\|");
                if (parts.length == 0) continue;

                String command = parts[0].toUpperCase();
                switch (command) {
                    case "LOGIN":
                        handleLogin(parts);
                        break;
                    case "GET_ALL_AUCTIONS":
                        handleGetAllAuctions();
                        break;
                    case "JOIN":
                        handleJoin(parts);
                        break;
                    case "BID":
                        handleBid(parts);
                        break;
                    case "LOGOUT":
                        return;
                    default:
                        sendMessage("ERROR|Lenh khong hop le");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("[NETWORK] Client ngắt kết nối đột ngột: " + e.getMessage());
        } finally {
            cleanUp(); // Task 5.1[cite: 1]
        }
    }

    /**
     * Task 4.4: Xử lý Đăng nhập[cite: 1]
     */
    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            sendMessage("ERROR|Sai cu phap dang nhap");
            return;
        }
        // TODO: Kết nối logic AuthService.login() tại đây[cite: 1]
        String username = parts[1];
        String password = parts[2];

        this.currentUser = authService.login(username, password);

        if (currentUser != null) {
            sendMessage("SUCCESS|Dang nhap thanh cong");
        } else {
            sendMessage("ERROR|Sai mat khau");[cite: 1]
        }
    }

    /**
     * Task 4.5: Xử lý Join Phòng (Tính năng giảng viên yêu cầu)[cite: 1]
     */
    private void handleJoin(String[] parts) {
        if (currentUser == null) {
            sendMessage("ERROR|Ban chua dang nhap");
            return;
        }
        if (parts.length < 2) {
            sendMessage("ERROR|Thieu ID_Phien");
            return;
        }

        String auctionId = parts[1];
        Auction auction = engine.findAuctionByItemName(auctionId);

        if (auction != null) {
            // TODO: Triển khai Task 2.3 - Thêm ClientHandler vào danh sách viewers[cite: 1]
            auction.addViewer(this);
            sendMessage("SUCCESS|Vao phong thanh cong");
        } else {
            sendMessage("ERROR|Phien dau gia khong ton tai");[cite: 1]
        }
    }

    /**
     * Task 4.6: Xử lý Đặt Giá & Multicast[cite: 1]
     */
    private void handleBid(String[] parts) {
        if (currentUser == null) {
            sendMessage("ERROR|Ban chua dang nhap");
            return;
        }
        if (parts.length < 3) {
            sendMessage("ERROR|Thieu thong tin dat gia");
            return;
        }

        String auctionId = parts[1];
        double amount = Double.parseDouble(parts[2]);
        Auction auction = engine.findAuctionByItemName(auctionId);

        if (auction == null) {
            sendMessage("ERROR|Phien dau gia khong ton tai");
            return;
        }

        // Gọi Service xử lý logic kiểm tra (Task 3.3)[cite: 1]
        String result = auctionService.placeBid(currentUser, auction, amount);
        sendMessage(result);

        // Nếu thành công -> Gửi thông báo cho mọi người trong phòng (Multicast)[cite: 1]
        if (result.startsWith("SUCCESS")) {
            String updateMsg = "UPDATE|" + auctionId + "|" + currentUser.getUsername() + "|" + amount;
            for (ClientHandler viewer : auction.getViewers()) {
                viewer.sendMessage(updateMsg);[cite: 1]
            }
        }
    }

    private void handleGetAllAuctions() {
        var auctions = engine.getActiveAuctions();
        if (auctions.isEmpty()) {
            sendMessage("INFO|Hien khong co phien dau gia nao");
            return;
        }

        StringBuilder sb = new StringBuilder("LIST_SUCCESS");
        for (Auction a : auctions) {
            sb.append("|").append(a.getItem().getName()).append(":").append(a.getCurrentPrice());
        }
        sendMessage(sb.toString());
    }

    /**
     * Task 5.1: Xử lý dọn dẹp khi ngắt kết nối[cite: 1]
     */
    private void cleanUp() {
        // TODO: Phải lặp qua tất cả Auction để gọi auction.removeViewer(this)[cite: 1]
        // Điều này đảm bảo Server không gửi tin nhắn vào luồng đã chết.
        try {
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }
}