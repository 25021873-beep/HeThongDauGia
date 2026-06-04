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

    public static final String STATUS_OPEN = "OPEN";

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
            LocalDateTime now = LocalDateTime.now();
            for (Auction a : running) {
                // Phiên đã quá hạn nhưng DB vẫn RUNNING/OPEN → đóng ngay
                if (a.getEndTime() != null && now.isAfter(a.getEndTime())) {
                    System.out.println("[ENGINE] Phien ID " + a.getId() + " da qua han, dang dong...");
                    if (auctionService != null) {
                        boolean closed = auctionService.closeAuction(a.getId());
                        if (closed) {
                            System.out.println("[ENGINE] Da dong phien qua han ID " + a.getId());
                        } else {
                            System.err.println("[ENGINE] Khong the dong phien qua han ID " + a.getId());
                        }
                    }
                } else {
                    activeAuctions.add(a);
                    roomManager.getOrCreateRoom(a);
                }
            }
        }
        System.out.println("[ENGINE] Da nap " + activeAuctions.size() + " phien dang chay.");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateAuctionStates();
            } catch (Exception e) {
                System.err.println("[ENGINE] Exception trong scheduler: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void updateAuctionStates() {
        if (auctionService == null) {
            System.err.println("[ENGINE] AuctionService chua duoc inject.");
            return;
        }

        LocalDateTime now      = LocalDateTime.now();
        List<Auction> toRemove = new ArrayList<>();

        for (Auction auction : activeAuctions) {

            // NHỊP 1: Mở phòng (OPEN → RUNNING) hoặc đóng trực tiếp nếu đã quá hạn
            if ("OPEN".equals(auction.getStatus()) && auction.getStartTime() != null) {
                if (!now.isBefore(auction.getStartTime())) {
                    // Kiểm tra nếu phiên đã quá endTime luôn → đóng trực tiếp, không cần qua RUNNING
                    if (auction.getEndTime() != null && now.isAfter(auction.getEndTime())) {
                        toRemove.add(auction);
                        try {
                            boolean closed = auctionService.closeAuction(auction.getId());
                            AuctionRoom room = roomManager.getRoom(auction.getId());
                            if (room != null) {
                                String winnerUsername = closed ? resolveWinnerUsername(auction.getId()) : null;
                                room.notifyAuctionEnded(winnerUsername, auction.getCurrentPrice());
                            }
                            roomManager.removeRoom(auction.getId());
                            System.out.println("[ENGINE] Phien OPEN ID " + auction.getId() + " da qua ca endTime, dong truc tiep.");
                        } catch (Exception e) {
                            System.err.println("[ENGINE] Loi dong phien OPEN qua han ID " + auction.getId() + ": " + e.getMessage());
                            roomManager.removeRoom(auction.getId());
                        }
                        continue; // Bỏ qua phần check RUNNING bên dưới
                    }

                    auction.setStatus("RUNNING");
                    boolean isUpdated = auctionDAO.updateAuctionStatus("RUNNING", auction.getId());

                    if (isUpdated) {
                        System.out.println("[ENGINE] Phong ID " + auction.getId() + " da den gio, chinh thuc RUNNING!");

                        // Broadcast báo cho các khách đang đợi trong phòng biết để lao vào bid
                        try {
                            AuctionRoom room = roomManager.getRoom(auction.getId());
                            if (room != null) {
                                room.notifyAuctionStarted();
                            }
                        } catch (Exception e) {
                            System.err.println("[ENGINE] Loi broadcast start phien " + auction.getId() + ": " + e.getMessage());
                        }
                    } else {
                        System.err.println("[ENGINE] Loi: Khong the update DB de mo phong ID " + auction.getId());
                    }
                }
            }

            // NHỊP 2: Đóng phòng (RUNNING và hết giờ)
            // Dùng if riêng (KHÔNG dùng else if) để phiên vừa chuyển RUNNING cũng được check
            if ("RUNNING".equals(auction.getStatus()) && auction.getEndTime() != null) {
                if (now.isAfter(auction.getEndTime())) {
                    // Luôn đánh dấu remove — dù close thành công hay thất bại
                    toRemove.add(auction);

                    try {
                        boolean closed = auctionService.closeAuction(auction.getId());

                        String winnerUsername = null;
                        BigDecimal finalPrice = auction.getCurrentPrice();

                        if (closed) {
                            winnerUsername = resolveWinnerUsername(auction.getId());
                        }

                        // Bắn Socket thông báo cho toàn bộ viewer
                        AuctionRoom room = roomManager.getRoom(auction.getId());
                        if (room != null) {
                            room.notifyAuctionEnded(winnerUsername, finalPrice);
                        }

                        roomManager.removeRoom(auction.getId());

                        System.out.println("[ENGINE] Da chot phien ID " + auction.getId()
                                + " | Winner: " + (winnerUsername != null ? winnerUsername : "Khong co ai mua"));

                    } catch (Exception e) {
                        System.err.println("[ENGINE] Loi khi dong phien ID " + auction.getId() + ": " + e.getMessage());
                        // Vẫn remove khỏi activeAuctions để không bị stuck vĩnh viễn
                        roomManager.removeRoom(auction.getId());
                    }
                }
            }
        }

        // ---------------------------------------------------------
        // NHỊP 3: DỌN DẸP BỘ NHỚ (Garbage Collection)
        // ---------------------------------------------------------
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
        if (auction == null || (!STATUS_OPEN.equals(auction.getStatus()) && !"RUNNING".equals(auction.getStatus()))) {
            throw new IllegalArgumentException(
                    "[ENGINE] Chi nap phien co trang thai: " + STATUS_OPEN + " hoac RUNNING");
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