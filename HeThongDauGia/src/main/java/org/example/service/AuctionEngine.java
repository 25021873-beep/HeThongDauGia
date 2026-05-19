package org.example.service;

import org.example.dao.AuctionDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.Auction;
import org.example.entity.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionEngine {

    private final ScheduledExecutorService scheduler      = Executors.newSingleThreadScheduledExecutor();
    private final List<Auction>            activeAuctions = new CopyOnWriteArrayList<>();
    private final AuctionDAO               auctionDAO;
    private final UserDAO                  userDAO;

    private final AuctionRoomManager       roomManager    = new AuctionRoomManager();

    private AuctionService auctionService;

    public static final String STATUS_ACTIVE = "ACTIVE";

    private static AuctionEngine instance;

    private AuctionEngine() {
        this.auctionDAO = new AuctionDAO();
        this.userDAO    = new UserDAO();
    }

    public static synchronized AuctionEngine getInstance() {
        if (instance == null) instance = new AuctionEngine();
        return instance;
    }

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    // Lấy instance của RoomManager để các Controller gọi tới
    public AuctionRoomManager getRoomManager() {
        return roomManager;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void startEngine() {
        System.out.println("[ENGINE] Dang khoi dong he thong...");

        List<Auction> running = auctionDAO.getActiveAuctions();
        if (running != null && !running.isEmpty()) {
            activeAuctions.addAll(running);
            // Khởi tạo phòng cho các phiên đang chạy
            for (Auction a : running) {
                roomManager.getOrCreateRoom(a);
            }
        }
        System.out.println("[ENGINE] Da nap " + activeAuctions.size() + " phien dang chay.");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndCloseExpiredAuctions();
            } catch (Exception e) {
                System.err.println("[ENGINE] Exception trong scheduler: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void checkAndCloseExpiredAuctions() {
        if (auctionService == null) {
            System.err.println("[ENGINE] AuctionService chua duoc inject.");
            return;
        }

        LocalDateTime now      = LocalDateTime.now();
        List<Auction> toRemove = new ArrayList<>();

        for (Auction auction : activeAuctions) {
            if (auction.getEndTime() == null) continue;

            if (now.isAfter(auction.getEndTime())) {
                boolean closed = auctionService.closeAuction(auction.getId());
                if (closed) {
                    String     winnerUsername = resolveWinnerUsername(auction.getId());
                    BigDecimal finalPrice     = auction.getCurrentPrice();

                    // THAY ĐỔI: Broadcast thông qua RoomManager
                    AuctionRoom room = roomManager.getRoom(auction.getId());
                    if (room != null) {
                        room.notifyAuctionEnded(winnerUsername, finalPrice);
                    }

                    toRemove.add(auction);
                    // Dọn dẹp phòng khi phiên kết thúc
                    roomManager.removeRoom(auction.getId());

                    System.out.println("[ENGINE] Da chot phien ID " + auction.getId()
                            + " | Winner: " + (winnerUsername != null ? winnerUsername : "Khong co"));
                } else {
                    System.err.println("[ENGINE] Khong the chot phien ID: " + auction.getId());
                }
            }
        }

        if (!toRemove.isEmpty()) {
            activeAuctions.removeAll(toRemove);
        }
    }

    public void stopEngine() {
        System.out.println("[ENGINE] Dang tat he thong...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("[ENGINE] Da tat an toan.");
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void addAuction(Auction auction) {
        if (auction == null || !STATUS_ACTIVE.equals(auction.getStatus())) {
            throw new IllegalArgumentException(
                    "[ENGINE] Chi nap phien co trang thai: " + STATUS_ACTIVE);
        }
        activeAuctions.add(auction);
        roomManager.getOrCreateRoom(auction); // Tạo phòng mới
        System.out.println("[ENGINE] Nap phien moi ID: " + auction.getId());
    }

    public List<Auction> getActiveAuctions() {
        return Collections.unmodifiableList(activeAuctions);
    }

    public Auction findActiveAuctionById(int auctionId) {
        for (Auction auction : activeAuctions) {
            if (auction.getId() == auctionId) return auction;
        }
        return null;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String resolveWinnerUsername(int auctionId) {
        try {
            Auction updated = auctionDAO.getAuctionById(auctionId);
            if (updated == null || updated.getWinnerId() == 0) return null;
            User winner = userDAO.getUserById(updated.getWinnerId());
            return winner != null ? winner.getUsername() : null;
        } catch (Exception e) {
            System.err.println("[ENGINE] Khong lay duoc winner: " + e.getMessage());
            return null;
        }
    }
}