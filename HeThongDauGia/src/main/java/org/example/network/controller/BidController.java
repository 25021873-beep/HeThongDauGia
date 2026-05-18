package org.example.network.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.dto.request.BidRequest;
import org.example.dto.response.BidResponse;
import org.example.dto.response.SimpleResponse;
import org.example.entity.Auction;
import org.example.exception.database.DatabaseException;
import org.example.network.ClientHandler;
import org.example.network.SessionContext;
import org.example.service.AuctionEngine;
import org.example.service.AuctionService;

import java.time.LocalDateTime;


public class BidController {

    private final SessionContext session;
    private final AuctionEngine  engine;
    private final AuctionService auctionService;
    private final ClientHandler  handler;
    private final Gson           gson;

    public BidController(SessionContext session, AuctionEngine engine,
                         AuctionService auctionService, ClientHandler handler, Gson gson) {
        this.session        = session;
        this.engine         = engine;
        this.auctionService = auctionService;
        this.handler        = handler;
        this.gson           = gson;
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
            boolean success = auctionService.placeBid(
                    session.getCurrentUser().getId(),
                    req.getAuctionId(),
                    req.getAmount());

            if (success) {
                String username = session.getCurrentUser().getUsername();

                // 1. Gửi BidResponse riêng cho người đặt (confirm cá nhân)
                session.send(new BidResponse(
                        req.getAuctionId(), username,
                        req.getAmount(), LocalDateTime.now()));

                // 2. Broadcast BidUpdateResponse tới TẤT CẢ observer trong phòng
                //    (kể cả người vừa đặt — client tự xử lý nếu muốn bỏ qua)
                //    Thay vì tự loop getViewers() → dùng Observer Pattern
                auction.notifyBidPlaced(username, req.getAmount());

            } else {
                session.send(SimpleResponse.error(
                        "Dat gia that bai: gia phai cao hon gia hien tai hoac khong du tien"));
            }

        } catch (DatabaseException e) {
            System.err.println("[BID] Loi DB: " + e.getMessage());
            session.send(SimpleResponse.error("Loi he thong, vui long thu lai sau"));
        }
    }
}