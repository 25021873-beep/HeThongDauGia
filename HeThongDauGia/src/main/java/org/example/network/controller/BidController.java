package org.example.network.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.dto.BidResult;
import org.example.dto.request.AutoBidRequest;
import org.example.dto.request.BidRequest;
import org.example.dto.response.AutoBidResponse;
import org.example.dto.response.BidResponse;
import org.example.dto.response.SimpleResponse;
import org.example.entity.Auction;
import org.example.network.ClientHandler;
import org.example.network.SessionContext;
import org.example.service.AuctionEngine;
import org.example.service.AuctionRoom;
import org.example.service.AuctionService;
import org.example.service.AutoBidService;

import java.time.LocalDateTime;

public class BidController {

    private final SessionContext session;
    private final AuctionEngine  engine;
    private final AuctionService auctionService;
    private final ClientHandler  handler;
    private final Gson           gson;
    // Thêm AutoBidService
    private AutoBidService autoBidService;

    public BidController(SessionContext session, AuctionEngine engine,
                         AuctionService auctionService, ClientHandler handler, Gson gson) {
        this.session        = session;
        this.engine         = engine;
        this.auctionService = auctionService;
        this.handler        = handler;
        this.gson           = gson;
    }

    public void setAutoBidService(AutoBidService autoBidService) {
        this.autoBidService = autoBidService;
    }

    // ── BID ───────────────────────────────────────────────────────────────────

    public void handleBid(JsonObject json) {
        if (!session.requireLogin()) return;
        BidRequest req = gson.fromJson(json, BidRequest.class);

        Auction auction = engine.findActiveAuctionById(req.getAuctionId());
        if (auction == null) {
            session.send(SimpleResponse.error("Phien dau gia khong ton tai hoac da ket thuc"));
            return;
        }

        try {
            BidResult result = auctionService.placeBid(
                    session.getCurrentUser().getId(),
                    req.getAuctionId(),
                    req.getAmount());

            if (result.isSuccess()) {
                String username = session.getCurrentUser().getUsername();

                // Lấy số dư mới sau khi bid
                java.math.BigDecimal newBalance = java.math.BigDecimal.ZERO;
                try {
                    org.example.entity.user.User updatedUser = new org.example.dao.user.UserDAO().getUserById(session.getCurrentUser().getId());
                    if (updatedUser != null) {
                        newBalance = updatedUser.getBalance();
                    }
                } catch (Exception ignored) {}

                // 1. Confirm cá nhân cho người đặt
                session.send(new BidResponse(
                        req.getAuctionId(), username,
                        req.getAmount(), LocalDateTime.now(), newBalance));

                // Broadcast đã được AuctionService xử lý nội bộ

            } else {
                session.send(SimpleResponse.error(
                        "Dat gia that bai: " + result.getErrorMessage()));
            }

        } catch (Exception e) {
            System.err.println("[BID] Loi he thong: " + e.getMessage());
            session.send(SimpleResponse.error("Loi he thong, vui long thu lai sau"));
        }
    }

    // ── AUTO-BID ──────────────────────────────────────────────────────────────

    public void handleAutoBid(JsonObject json) {
        if (!session.requireLogin()) return;

        if (autoBidService == null) {
            session.send(SimpleResponse.error("He thong Auto-bid chua duoc khoi tao."));
            return;
        }

        AutoBidRequest req = gson.fromJson(json, AutoBidRequest.class);

        try {
            // Đăng ký cấu hình Auto-bid
            autoBidService.registerAutoBid(
                    session.getCurrentUser().getId(),
                    (int) req.getAuctionId(),
                    req.getMaxBid(),
                    req.getIncrement()
            );

            // Gửi xác nhận về cho Client
            session.send(new AutoBidResponse(
                    "SUCCESS",
                    "Dang ky auto-bid thanh cong",
                    req.getAuctionId(),
                    req.getMaxBid(),
                    req.getIncrement()
            ));

        } catch (Exception e) {
            System.err.println("[BID_CTRL] Loi dang ky auto-bid: " + e.getMessage());
            session.send(SimpleResponse.error("Loi dang ky auto-bid: " + e.getMessage()));
        }
    }

    // ── GET_USER_BID_HISTORY ──────────────────────────────────────────────────

    public void handleGetUserBidHistory() {
        if (!session.requireLogin()) return;

        try {
            org.example.dao.BidHistoryDAO dao = new org.example.dao.BidHistoryDAO();
            com.google.gson.JsonArray history = dao.getUserBidHistory(session.getCurrentUser().getId());

            JsonObject response = new JsonObject();
            response.addProperty("status", "SUCCESS");
            response.add("history", history);

            session.sendRaw(response.toString());

        } catch (Exception e) {
            System.err.println("[BID_CTRL] Loi lay lich su: " + e.getMessage());
            session.send(SimpleResponse.error("Loi he thong khi lay lich su dau gia."));
        }
    }
}