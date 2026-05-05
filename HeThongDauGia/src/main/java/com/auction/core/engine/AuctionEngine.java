package com.auction.core.engine;

import com.auction.core.model.Auction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionEngine {

    // ========================================================
    // 1. TÀI NGUYÊN HỆ THỐNG (SYSTEM RESOURCES)
    // ========================================================
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final List<Auction> activeAuctions = new CopyOnWriteArrayList<>();


    // ========================================================
    // 2. VÒNG ĐỜI VẬN HÀNH (ENGINE LIFECYCLE)
    // ========================================================

    public void startEngine() {
        System.out.println("[ENGINE] Đã khởi động. Đang giám sát thời gian...");

        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            List<Auction> finishedAuctions = new ArrayList<>();

            for (Auction auction : activeAuctions) {
                if (auction.isActive() && now.isAfter(auction.getEndTime())) {
                    auction.endAuction();
                    System.out.println("[CHỐT SỔ] Đã đóng: [" + auction.getItem().getName() + "] | Giá: " + auction.getCurrentPrice());

                    // TODO (DB & Mạng): Lưu CSDL và Broadcast thông báo người thắng

                    finishedAuctions.add(auction);
                }
            }
            if (!finishedAuctions.isEmpty()) {
                activeAuctions.removeAll(finishedAuctions);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void stopEngine() {
        System.out.println("[ENGINE] Đang tắt hệ thống...");
        // TODO (DB): Lưu trạng thái các phiên đang dở dang xuống Database

        scheduler.shutdown();
        try {
            // Chờ tối đa 2 giây để các luồng hoàn tất việc đang làm dở
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow(); // Ép tắt nếu quá hạn
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }


    // ========================================================
    // 3. API CHO CÁC TẦNG KHÁC SỬ DỤNG (PUBLIC API)
    // ========================================================

    public void addAuction(Auction auction) {
        activeAuctions.add(auction);
        System.out.println("[HỆ THỐNG] Đã thêm vào hàng đợi: [" + auction.getItem().getName() + "]");
        // TODO (DB): Gọi DAO lưu trạng thái mở phiên
    }

    /**
     * Dùng cho Tầng Mạng: Trả về danh sách các phiên đang mở (chỉ đọc)
     */
    public List<Auction> getActiveAuctions() {
        return Collections.unmodifiableList(activeAuctions);
    }

    /**
     * Dùng cho Tầng Mạng: Tìm phiên đấu giá để Client Join phòng hoặc Đặt giá
     */
    public Auction findAuctionByItemName(String itemName) {
        for (Auction auction : activeAuctions) {
            if (auction.getItem().getName().equalsIgnoreCase(itemName)) {
                return auction;
            }
        }
        return null;
    }
}