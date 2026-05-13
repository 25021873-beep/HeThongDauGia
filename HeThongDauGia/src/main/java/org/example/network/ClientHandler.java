package org.example.network;

import org.example.service.AuctionEngine;
import org.example.entity.Auction;
import org.example.entity.user.User;
import org.example.service.AuctionService;
import org.example.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final AuctionEngine engine;
    private final UserService userService;
    private final AuctionService auctionService;

    private PrintWriter out;
    private User currentUser;

    public ClientHandler(Socket socket, AuctionEngine engine) {
        this.clientSocket = socket;
        this.engine = engine;
        this.userService = new UserService();
        this.auctionService = new AuctionService();
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            sendMessage("SUCCESS|Ket noi Server thanh cong");

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                String[] parts = clientMessage.split("\\|");
                if (parts.length == 0) continue;

                String command = parts[0].trim().toUpperCase();
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
            System.err.println("[NETWORK] Client ngat ket noi dot ngot: " + e.getMessage());
        } finally {
            cleanUp();
        }
    }

    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            sendMessage("ERROR|Sai cu phap dang nhap");
            return;
        }
        String username = parts[1];
        String password = parts[2];

        // Gọi UserService.login() thay vì AuthService.login()
        this.currentUser = userService.login(username, password);

        if (currentUser != null) {
            sendMessage("SUCCESS|Dang nhap thanh cong");
        } else {
            sendMessage("ERROR|Sai ten dang nhap hoac mat khau");
        }
    }

    private void handleJoin(String[] parts) {
        if (currentUser == null) {
            sendMessage("ERROR|Ban chua dang nhap");
            return;
        }
        if (parts.length < 2) {
            sendMessage("ERROR|Thieu ID phien");
            return;
        }

        int auctionId;
        try {
            auctionId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            sendMessage("ERROR|ID phien khong hop le, phai la so nguyen");
            return;
        }

        Auction auction = engine.findActiveAuctionById(auctionId);

        if (auction != null) {
            auction.addViewer(this);
            sendMessage("SUCCESS|Vao phong thanh cong: " + auction.getName());
        } else {
            sendMessage("ERROR|Phien dau gia khong ton tai");
        }
    }

    private void handleBid(String[] parts) {
        if (currentUser == null) {
            sendMessage("ERROR|Ban chua dang nhap");
            return;
        }
        if (parts.length < 3) {
            sendMessage("ERROR|Thieu thong tin dat gia");
            return;
        }

        int auctionId;
        try {
            auctionId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            sendMessage("ERROR|ID phien khong hop le, phai la so nguyen");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(parts[2]);
        } catch (NumberFormatException e) {
            sendMessage("ERROR|Muc gia khong hop le");
            return;
        }

        Auction auction = engine.findActiveAuctionById(auctionId);

        if (auction == null) {
            sendMessage("ERROR|Phien dau gia khong ton tai");
            return;
        }

        boolean success = auctionService.placeBid(currentUser.getId(), auctionId, amount);
        if (success) {
            sendMessage("SUCCESS|Dat gia thanh cong");


            // Multicast cho mọi người
            String updateMsg = "UPDATE|" + auctionId + "|" + currentUser.getUsername() + "|" + amount;
            for (ClientHandler viewer : auction.getViewers()) {
                viewer.sendMessage(updateMsg);
            }


        } else {
            sendMessage("ERROR|Dat gia that bai...");
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
            sb.append("|")
                    .append(a.getId()).append(":")
                    .append(a.getName()).append(":")
                    .append(a.getCurrentPrice());
        }
        sendMessage(sb.toString());
    }

    // Xóa client khỏi tất cả phòng và đóng socket khi ngắt kết nối
    private void cleanUp() {
        for (Auction auction : engine.getActiveAuctions()) {
            auction.removeViewer(this);
        }
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