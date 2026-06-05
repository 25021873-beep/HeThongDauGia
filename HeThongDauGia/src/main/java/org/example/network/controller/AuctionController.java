package org.example.network.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.dto.request.GetAuctionDetailRequest;
import org.example.dto.request.GetBidHistoryRequest;
import org.example.dto.request.JoinRequest;
import org.example.dto.response.*;
import org.example.entity.Auction;
import org.example.entity.BidHistory;
import org.example.entity.item.Art;
import org.example.entity.item.Electronics;
import org.example.entity.item.Item;
import org.example.entity.item.Vehicle;
import org.example.network.ClientHandler;
import org.example.network.SessionContext;
import org.example.service.AuctionEngine;
import org.example.service.AuctionService;

import java.util.List;
import java.util.stream.Collectors;

public class AuctionController {

    private final SessionContext session;
    private final AuctionEngine  engine;
    private final AuctionService auctionService; // Đã thêm để đồng bộ Dependency Injection
    private final ClientHandler  handler;
    private final Gson           gson;

    // Khởi tạo thông qua Constructor giúp tránh việc gọi trực tiếp hardcode Singleton .getInstance()
    public AuctionController(SessionContext session, AuctionEngine engine,
                             AuctionService auctionService, ClientHandler handler, Gson gson) {
        this.session        = session;
        this.engine         = engine;
        this.auctionService = auctionService;
        this.handler        = handler;
        this.gson           = gson;
    }

    // ── GET_ALL_AUCTIONS ──────────────────────────────────────────────────────

    public void handleGetAllAuctions() {
        List<Auction> active = engine.getActiveAuctions();
        if (active.isEmpty()) {
            session.send(SimpleResponse.info("Hien khong co phien dau gia nao"));
            return;
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        List<AuctionSummary> summaries = active.stream()
                .map(a -> {
                    // Tính status thực tế dựa trên thời gian (tránh lệch do Engine cycle 5s)
                    String effectiveStatus = a.getStatus();
                    if (("RUNNING".equals(effectiveStatus) || "OPEN".equals(effectiveStatus))
                            && a.getEndTime() != null && now.isAfter(a.getEndTime())) {
                        effectiveStatus = "FINISHED";
                    } else if ("OPEN".equals(effectiveStatus) 
                            && a.getStartTime() != null && !now.isBefore(a.getStartTime())) {
                        effectiveStatus = "RUNNING";
                    }
                    return new AuctionSummary(
                            a.getId(), a.getItem().getName(), a.getCurrentPrice(), effectiveStatus);
                })
                // Lọc bỏ các phiên đã kết thúc — Dashboard chỉ hiện phiên đang mở/sắp mở
                .filter(s -> !"FINISHED".equals(s.getStatus()) && !"CANCELED".equals(s.getStatus()))
                .collect(Collectors.toList());

        if (summaries.isEmpty()) {
            session.send(SimpleResponse.info("Hien khong co phien dau gia nao dang mo"));
            return;
        }
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

        // Fix race condition: Engine chạy mỗi 5s, nếu client JOIN đúng lúc OPEN->RUNNING
        // thì status trên RAM có thể chưa cập nhật
        String effectiveStatus = auction.getStatus();
        if ("OPEN".equals(effectiveStatus) 
                && auction.getStartTime() != null 
                && !java.time.LocalDateTime.now().isBefore(auction.getStartTime())) {
            effectiveStatus = "RUNNING";
        }

        session.send(new JoinResponse(
                auction.getId(),
                auction.getItem().getName(),
                auction.getCurrentPrice(),
                auction.getStartTime(),
                auction.getEndTime(),
                effectiveStatus));
    }

    // ── GET_BID_HISTORY (Phục vụ Visualization) ───────────────────────────────

    public void handleGetBidHistory(JsonObject json) {
        if (!session.requireLogin()) return;
        GetBidHistoryRequest req = gson.fromJson(json, GetBidHistoryRequest.class);

        try {
            List<BidHistory> history = auctionService.getBidHistory((int) req.getAuctionId());

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

    // ── GET_AUCTION_DETAIL ────────────────────────────────────────────────────

    public void handleGetAuctionDetail(JsonObject json) {
        if (!session.requireLogin()) return;

        GetAuctionDetailRequest req = gson.fromJson(json, GetAuctionDetailRequest.class);

        try {
            // Tối ưu: Lấy thông tin phiên đấu giá từ dịch vụ đã tiêm
            Auction auction = auctionService.getAuctionById(req.getAuctionId());

            if (auction == null) {
                session.send(SimpleResponse.error("Khong tim thay phien dau gia voi ID: " + req.getAuctionId()));
                return;
            }

            Item item = auction.getItem();
            String itemType = "UNKNOWN";
            Integer warranty = null;
            String author = null;
            String engineType = null; // Đã sửa đổi tên biến từ 'engine' thành 'engineType' để tránh lỗi che khuất thuộc tính lớp

            // Áp dụng chặt chẽ tính Đa hình (Polymorphism) để bóc tách thông tin
            if (item instanceof Electronics) {
                itemType = "ELECTRONICS";
                warranty = ((Electronics) item).getWarrantyMonths();
            } else if (item instanceof Art) {
                itemType = "ART";
                author = ((Art) item).getAuthor();
            } else if (item instanceof Vehicle) {
                itemType = "VEHICLE";
                engineType = ((Vehicle) item).getEngineType();
            }

            // Tính status thực tế dựa trên thời gian (tránh lệch do Engine cycle 5s)
            String effectiveStatus = auction.getStatus();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (("RUNNING".equals(effectiveStatus) || "OPEN".equals(effectiveStatus))
                    && auction.getEndTime() != null && now.isAfter(auction.getEndTime())) {
                effectiveStatus = "FINISHED";
            } else if ("OPEN".equals(effectiveStatus) 
                    && auction.getStartTime() != null && !now.isBefore(auction.getStartTime())) {
                effectiveStatus = "RUNNING";
            }

            // Resolve tên người bán từ sellerId
            String sellerUsername = "Không rõ";
            try {
                org.example.entity.user.User seller = new org.example.dao.user.UserDAO().getUserById(auction.getSellerId());
                if (seller != null) {
                    sellerUsername = seller.getUsername();
                }
            } catch (Exception ignored) {}

            boolean hasAutoBid = false;
            java.math.BigDecimal autoBidMax = null;
            java.math.BigDecimal autoBidIncrement = null;
            try {
                java.util.List<org.example.entity.AutoBidConfig> activeBids = new org.example.dao.AutoBidDAO().getActiveAutoBids(auction.getId());
                for (org.example.entity.AutoBidConfig c : activeBids) {
                    if (c.getBidderId() == session.getCurrentUser().getId()) {
                        hasAutoBid = true;
                        autoBidMax = c.getMaxBid();
                        autoBidIncrement = c.getIncrement();
                        break;
                    }
                }
            } catch (Exception ignored) {}

            // Đóng gói dữ liệu trả về DTO Response
            AuctionDetailResponse response = new AuctionDetailResponse(
                    "SUCCESS", "Lay chi tiet thanh cong",
                    auction.getId(), auction.getCurrentPrice(), auction.getStartingPrice(), auction.getStepPrice(),
                    auction.getStartTime(), auction.getEndTime(), effectiveStatus,
                    item.getName(), item.getDescription(),
                    itemType, warranty, author, engineType,
                    sellerUsername,
                    hasAutoBid, autoBidMax, autoBidIncrement
            );

            session.send(response);

        } catch (Exception e) {
            System.err.println("[AUCTION_CTRL] Loi lay chi tiet phien: " + e.getMessage());
            session.send(SimpleResponse.error("Loi he thong khi lay chi tiet phien dau gia"));
        }
    }
}