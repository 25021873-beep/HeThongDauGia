package org.example.network.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.dto.request.GetBidHistoryRequest;
import org.example.dto.request.JoinRequest;
import org.example.dto.response.*;
import org.example.entity.Auction;
import org.example.entity.BidHistory;
import org.example.network.ClientHandler;
import org.example.network.SessionContext;
import org.example.service.AuctionEngine;
import org.example.service.AuctionService;

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

        engine.getRoomManager().joinRoom(auction, handler);

        session.send(new JoinResponse(
                auction.getId(),
                auction.getItem().getName(),
                auction.getCurrentPrice(),
                auction.getEndTime(),
                auction.getStatus()));
    }

    // ── GET_BID_HISTORY (Phục vụ Visualization) ───────────────────────────────

    public void handleGetBidHistory(JsonObject json) {
        if (!session.requireLogin()) return;
        GetBidHistoryRequest req = gson.fromJson(json, GetBidHistoryRequest.class);

        try {
            // Lấy service thông qua instance
            List<BidHistory> history = AuctionService.getInstance().getBidHistory((int) req.getAuctionId());

            // Chuyển đổi Entity sang DTO cho Response
            List<BidHistoryResponse.BidPoint> points = history.stream()
                    .map(h -> new BidHistoryResponse.BidPoint(
                            h.getBidderUsername(),
                            h.getPrice(),
                            h.getBidTime()))
                    .collect(Collectors.toList());

            session.send(new BidHistoryResponse("SUCCESS", "Lay lich su thanh cong",
                    req.getAuctionId(), points));

        } catch (Exception e) {
            System.err.println("[AUCTION_CTRL] Loi lay lich su bid: " + e.getMessage());
            session.send(SimpleResponse.error("Khong the lay lich su dau gia. " + e.getMessage()));
        }
    }
}