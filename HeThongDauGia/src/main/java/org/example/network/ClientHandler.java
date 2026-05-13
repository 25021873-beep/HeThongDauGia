package org.example.network;

import com.google.gson.*;
import org.example.service.AuctionEngine;
import org.example.dto.request.*;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final AuctionEngine engine;
    private final UserService userService;
    private final AuctionService auctionService;

    private PrintWriter out;
    private User currentUser;
    private final Gson gson;

    public ClientHandler(Socket socket, AuctionEngine engine,
                         UserService userService, AuctionService auctionService) {
        this.clientSocket = socket;
        this.engine = engine;
        this.userService = userService;
        this.auctionService = auctionService;

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                        (json, type, context) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();
    }

    public void send(BaseResponse response) {
        if (out != null) {
            out.println(gson.toJson(response)); // Tự động convert Response thành JSON
        }
    }

    // ── Vòng lặp chính ────────────────────────────────────────────────────────

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            send(SimpleResponse.success("Ket noi Server thanh cong"));

            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    // 1. Chuyển chuỗi nhận được thành JsonObject để đọc command
                    JsonObject jsonObject = JsonParser.parseString(line).getAsJsonObject();

                    if (!jsonObject.has("command")) {
                        send(SimpleResponse.error("JSON thieu truong 'command'"));
                        continue;
                    }

                    String command = jsonObject.get("command").getAsString().trim().toUpperCase();

                    // 2. Điều hướng xử lý theo command
                    switch (command) {
                        case "LOGIN":            handleLogin(jsonObject);          break;
                        case "REGISTER":         handleRegister(jsonObject);       break;
                        case "GET_ALL_AUCTIONS": handleGetAllAuctions();           break;
                        case "JOIN":             handleJoin(jsonObject);           break;
                        case "BID":              handleBid(jsonObject);            break;
                        case "CHANGE_PASSWORD":  handleChangePassword(jsonObject); break;
                        case "TOP_UP":           handleTopUp(jsonObject);          break;
                        case "LOGOUT":           handleLogout();                   return;
                        default:
                            send(SimpleResponse.error("Lenh khong hop le: " + command));
                            break;
                    }
                } catch (JsonSyntaxException | IllegalStateException e) {
                    send(SimpleResponse.error("Dinh dang JSON khong hop le: " + line));
                }
            }
        } catch (IOException e) {
            System.err.println("[NETWORK] Client ngat ket noi: " + e.getMessage());
        } finally {
            cleanUp();
        }
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleLogin(JsonObject json) {
        LoginRequest req = gson.fromJson(json, LoginRequest.class);

        if (req.getUsername() == null || req.getPassword() == null) {
            send(SimpleResponse.error("Thieu username hoac password"));
            return;
        }
        // Chặn đăng nhập kép trong cùng session
        if (currentUser != null) {
            send(SimpleResponse.error("Ban da dang nhap roi. Hay LOGOUT truoc."));
            return;
        }

        this.currentUser = userService.login(req.getUsername(), req.getPassword());
        if (currentUser != null) {
            send(new LoginResponse(currentUser.getId(), currentUser.getUsername(), currentUser.getRole(), currentUser.getBalance()));
        } else {
            send(SimpleResponse.error("Sai ten dang nhap hoac mat khau"));
        }
    }

    private void handleRegister(JsonObject json) {
        RegisterRequest req = gson.fromJson(json, RegisterRequest.class);

        if (req.getUsername() == null || req.getPassword() == null) {
            send(SimpleResponse.error("Thieu thong tin dang ky"));
            return;
        }
        if (req.getRole() == null) req.setRole("BIDDER");

        boolean success = userService.register(req);
        if (success) {
            User newUser = userService.getUserByUsername(req.getUsername());
            send(new RegisterResponse(newUser.getId(), newUser.getUsername(), newUser.getRole()));
        } else {
            send(SimpleResponse.error("Ten dang nhap da ton tai"));
        }
    }

    private void handleJoin(JsonObject json) {
        if (!requireLogin()) return;
        JoinRequest req = gson.fromJson(json, JoinRequest.class);

        Auction auction = engine.findActiveAuctionById(req.getAuctionId());
        if (auction != null) {
            auction.addViewer(this);
            send(new JoinResponse(auction.getId(), auction.getName(), auction.getCurrentPrice(), auction.getEndTime(), auction.getStatus()));
        } else {
            send(SimpleResponse.error("Phien dau gia khong ton tai hoac da ket thuc"));
        }
    }

    private void handleBid(JsonObject json) {
        if (!requireLogin()) return;
        BidRequest req = gson.fromJson(json, BidRequest.class);

        Auction auction = engine.findActiveAuctionById(req.getAuctionId());
        if (auction == null) {
            send(SimpleResponse.error("Phien dau gia khong ton tai hoac da ket thuc"));
            return;
        }

        boolean success = auctionService.placeBid(currentUser.getId(), req.getAuctionId(), req.getAmount());
        if (success) {
            // Phản hồi cho người đặt
            send(new BidResponse(req.getAuctionId(), currentUser.getUsername(), req.getAmount(), LocalDateTime.now()));

            // Broadcast cho tất cả trong phòng (Áp dụng Observer Pattern theo đề bài)
            BidUpdateResponse update = new BidUpdateResponse(req.getAuctionId(), currentUser.getUsername(), req.getAmount());
            for (ClientHandler viewer : auction.getViewers()) {
                viewer.send(update);
            }
        } else {
            send(SimpleResponse.error("Dat gia that bai: gia phai cao hon gia hien tai hoac khong du tien"));
        }
    }

    private void handleGetAllAuctions() {
        List<Auction> active = engine.getActiveAuctions();
        if (active.isEmpty()) {
            send(SimpleResponse.info("Hien khong co phien dau gia nao"));
            return;
        }
        List<AuctionSummary> summaries = active.stream()
                .map(a -> new AuctionSummary(a.getId(), a.getName(), a.getCurrentPrice(), a.getStatus()))
                .collect(Collectors.toList());
        send(new AuctionListResponse(summaries));
    }

    private void handleChangePassword(JsonObject json) {
        if (!requireLogin()) return;
        ChangePasswordRequest req = gson.fromJson(json, ChangePasswordRequest.class);

        boolean ok = userService.changePassword(currentUser.getUsername(), req.getOldPassword(), req.getNewPassword());
        if (ok) {
            send(new ChangePasswordResponse(currentUser.getUsername()));
        } else {
            send(SimpleResponse.error("Mat khau cu khong chinh xac"));
        }
    }

    private void handleTopUp(JsonObject json) {
        if (!requireLogin()) return;
        TopUpRequest req = gson.fromJson(json, TopUpRequest.class);

        boolean ok = userService.topUpBalance(currentUser.getId(), req.getAmount());
        if (ok) {
            User updated = userService.getUserProfile(currentUser.getId());
            if (updated != null) currentUser = updated;
            send(new TopUpResponse(currentUser.getId(), req.getAmount(), currentUser.getBalance()));
        } else {
            send(SimpleResponse.error("Nap tien that bai"));
        }
    }

    private void handleLogout() {
        String name = currentUser != null ? currentUser.getUsername() : "Guest";
        send(SimpleResponse.success("Dang xuat thanh cong. Tam biet " + name + "!"));
        this.currentUser = null;
    }

    private boolean requireLogin() {
        if (currentUser == null) {
            send(SimpleResponse.error("Ban chua dang nhap"));
            return false;
        }
        return true;
    }

    private void cleanUp() {
        for (Auction auction : engine.getActiveAuctions()) {
            auction.removeViewer(this);
        }
        try {
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("[NETWORK] Loi khi dong ket noi: " + e.getMessage());
        }
    }

    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }
}