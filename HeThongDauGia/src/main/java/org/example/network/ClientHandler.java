package org.example.network;

import org.example.service.AuctionEngine;
import org.example.dto.request.RegisterRequest;
import org.example.dto.response.*;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    // ── Gửi response ──────────────────────────────────────────────────────────

    public void send(BaseResponse response) {
        if (out != null) out.println(response.serialize());
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    // ── Vòng lặp chính ────────────────────────────────────────────────────────

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            send(SimpleResponse.success("Ket noi Server thanh cong"));

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                String[] parts = clientMessage.split("\\|");
                if (parts.length == 0) continue;

                String command = parts[0].trim().toUpperCase();
                switch (command) {
                    case "LOGIN":            handleLogin(parts);          break;
                    case "REGISTER":         handleRegister(parts);       break;
                    case "GET_ALL_AUCTIONS": handleGetAllAuctions();      break;
                    case "JOIN":             handleJoin(parts);           break;
                    case "BID":              handleBid(parts);            break;
                    case "CHANGE_PASSWORD":  handleChangePassword(parts); break;
                    case "TOP_UP":           handleTopUp(parts);          break;
                    case "LOGOUT":           handleLogout(); return;
                    default:
                        send(SimpleResponse.error("Lenh khong hop le: " + command));
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("[NETWORK] Client ngat ket noi: " + e.getMessage());
        } finally {
            cleanUp();
        }
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            send(SimpleResponse.error("Sai cu phap: LOGIN|username|password"));
            return;
        }
        // Chặn đăng nhập kép trong cùng session
        if (currentUser != null) {
            send(SimpleResponse.error("Ban da dang nhap roi. Hay LOGOUT truoc."));
            return;
        }
        this.currentUser = userService.login(parts[1], parts[2]);
        if (currentUser != null) {
            send(new LoginResponse(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getRole(),
                    currentUser.getBalance()
            ));
        } else {
            send(SimpleResponse.error("Sai ten dang nhap hoac mat khau"));
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length < 3) {
            send(SimpleResponse.error("Sai cu phap: REGISTER|username|password[|role]"));
            return;
        }
        RegisterRequest request = new RegisterRequest();
        request.setUsername(parts[1]);
        request.setPassword(parts[2]);
        request.setRole(parts.length >= 4 ? parts[3] : "BIDDER");

        boolean success = userService.register(request);
        if (success) {
            // BUG FIX: dùng getUserByUsername() thay vì gọi login() 2 lần
            User newUser = userService.getUserByUsername(request.getUsername());
            if (newUser != null) {
                send(new RegisterResponse(newUser.getId(), newUser.getUsername(), newUser.getRole()));
            } else {
                send(SimpleResponse.success("Dang ky thanh cong. Vui long dang nhap."));
            }
        } else {
            send(SimpleResponse.error("Ten dang nhap da ton tai"));
        }
    }

    private void handleJoin(String[] parts) {
        if (!requireLogin()) return;
        if (parts.length < 2) {
            send(SimpleResponse.error("Sai cu phap: JOIN|auctionId"));
            return;
        }
        int auctionId = parseIntOrError(parts[1], "ID phien");
        if (auctionId < 0) return;

        Auction auction = engine.findActiveAuctionById(auctionId);
        if (auction != null) {
            auction.addViewer(this);
            send(new JoinResponse(
                    auction.getId(),
                    auction.getName(),
                    auction.getCurrentPrice(),
                    auction.getEndTime(),
                    auction.getStatus()
            ));
        } else {
            send(SimpleResponse.error("Phien dau gia khong ton tai hoac da ket thuc"));
        }
    }

    private void handleBid(String[] parts) {
        if (!requireLogin()) return;
        if (parts.length < 3) {
            send(SimpleResponse.error("Sai cu phap: BID|auctionId|amount"));
            return;
        }
        int auctionId = parseIntOrError(parts[1], "ID phien");
        if (auctionId < 0) return;

        BigDecimal amount = parseBigDecimalOrError(parts[2], "Muc gia");
        if (amount == null) return;

        Auction auction = engine.findActiveAuctionById(auctionId);
        if (auction == null) {
            send(SimpleResponse.error("Phien dau gia khong ton tai hoac da ket thuc"));
            return;
        }

        boolean success = auctionService.placeBid(currentUser.getId(), auctionId, amount);
        if (success) {
            LocalDateTime bidTime = LocalDateTime.now();
            send(new BidResponse(auctionId, currentUser.getUsername(), amount, bidTime));

            BidUpdateResponse update = new BidUpdateResponse(auctionId, currentUser.getUsername(), amount);
            for (ClientHandler viewer : auction.getViewers()) {
                viewer.send(update);
            }
        } else {
            send(SimpleResponse.error("Dat gia that bai: gia phai cao hon gia hien tai hoac vi khong du tien"));
        }
    }

    private void handleGetAllAuctions() {
        List<Auction> activeAuctions = engine.getActiveAuctions();
        if (activeAuctions.isEmpty()) {
            send(SimpleResponse.info("Hien khong co phien dau gia nao"));
            return;
        }
        List<AuctionSummary> summaries = activeAuctions.stream()
                .map(a -> new AuctionSummary(a.getId(), a.getName(), a.getCurrentPrice(), a.getStatus()))
                .collect(Collectors.toList());
        send(new AuctionListResponse(summaries));
    }

    private void handleChangePassword(String[] parts) {
        if (!requireLogin()) return;
        if (parts.length < 3) {
            send(SimpleResponse.error("Sai cu phap: CHANGE_PASSWORD|oldPassword|newPassword"));
            return;
        }
        boolean ok = userService.changePassword(currentUser.getUsername(), parts[1], parts[2]);
        if (ok) {
            send(new ChangePasswordResponse(currentUser.getUsername()));
        } else {
            send(SimpleResponse.error("Mat khau cu khong chinh xac"));
        }
    }

    private void handleTopUp(String[] parts) {
        if (!requireLogin()) return;
        if (parts.length < 2) {
            send(SimpleResponse.error("Sai cu phap: TOP_UP|amount"));
            return;
        }
        BigDecimal amount = parseBigDecimalOrError(parts[1], "So tien");
        if (amount == null) return;

        boolean ok = userService.topUpBalance(currentUser.getId(), amount);
        if (ok) {
            User updated = userService.getUserProfile(currentUser.getId());
            if (updated != null) currentUser = updated;
            send(new TopUpResponse(currentUser.getId(), amount, currentUser.getBalance()));
        } else {
            send(SimpleResponse.error("Nap tien that bai: chi Bidder moi duoc nap tien"));
        }
    }

    private void handleLogout() {
        String name = currentUser != null ? currentUser.getUsername() : "Guest";
        send(SimpleResponse.success("Dang xuat thanh cong. Tam biet " + name + "!"));
        this.currentUser = null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean requireLogin() {
        if (currentUser == null) {
            send(SimpleResponse.error("Ban chua dang nhap"));
            return false;
        }
        return true;
    }

    private int parseIntOrError(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            send(SimpleResponse.error(fieldName + " khong hop le, phai la so nguyen"));
            return -1;
        }
    }

    private BigDecimal parseBigDecimalOrError(String value, String fieldName) {
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            send(SimpleResponse.error(fieldName + " khong hop le"));
            return null;
        }
    }

// Hàm đóng Socket
    private void cleanUp() {
        for (Auction auction : engine.getActiveAuctions()) {
            auction.removeViewer(this);
        }
        try {
            if (out != null)   out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("[NETWORK] Loi khi dong ket noi: " + e.getMessage());
        }
    }

    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }
}