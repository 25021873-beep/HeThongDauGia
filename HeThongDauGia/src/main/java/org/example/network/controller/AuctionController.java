package org.example.network.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.dto.request.JoinRequest;
import org.example.dto.response.*;
import org.example.entity.Auction;
import org.example.network.ClientHandler;
import org.example.network.SessionContext;
import org.example.service.AuctionEngine;

import java.util.List;
import java.util.stream.Collectors;

public class AuctionController {

    private final SessionContext session;
    private final AuctionEngine  engine;
    private final ClientHandler  handler;
    private final Gson           gson;

    public AuctionController(SessionContext session, AuctionEngine engine,
                             ClientHandler handler, Gson gson) {
        this.session = session;
        this.engine  = engine;
        this.handler = handler;
        this.gson    = gson;
    }

    // ── GET_ALL_AUCTIONS ──────────────────────────────────────────────────────

    public void handleGetAllAuctions() {
        List<Auction> active = engine.getActiveAuctions();
        if (active.isEmpty()) {
            session.send(SimpleResponse.info("Hien khong co phien dau gia nao"));
            return;
        }
        List<AuctionSummary> summaries = active.stream()
                .map(a -> new AuctionSummary(
                        a.getId(), a.getItem().getName(), a.getCurrentPrice(), a.getStatus()))
                .collect(Collectors.toList());
        session.send(new AuctionListResponse(summaries));
    }

    // ── JOIN ──────────────────────────────────────────────────────────────────

    public void handleJoin(JsonObject json) {
        if (!session.requireLogin()) return;
        JoinRequest req = gson.fromJson(json, JoinRequest.class);

        Auction auction = engine.findActiveAuctionById(req.getAuctionId());
        if (auction == null) {
            session.send(SimpleResponse.error("Phien dau gia khong ton tai hoac da ket thuc"));
            return;
        }

        // THAY ĐỔI: Đăng ký handler vào phòng thông qua RoomManager
        engine.getRoomManager().joinRoom(auction, handler);

        session.send(new JoinResponse(
                auction.getId(),
                auction.getItem().getName(),
                auction.getCurrentPrice(),
                auction.getEndTime(),
                auction.getStatus()));
    }
}