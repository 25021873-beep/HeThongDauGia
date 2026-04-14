package com.auction.core.engine;

import com.auction.core.model.Auction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionEngine {

    // 1. THREAD POOL: Đội ngũ 5 "nhân viên" chạy ngầm để quét thời gian
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    // 2. DANH SÁCH ĐỒNG BỘ (Thread-Safe List):
    // Dùng CopyOnWriteArrayList thay vì ArrayList bình thường.
    // Tác dụng: Ngăn chặn lỗi sập Server (ConcurrentModificationException)
    // khi có người đang thêm đồ mới vào sàn trong lúc hệ thống đang quét thời gian.
    private final List<Auction> activeAuctions = new CopyOnWriteArrayList<>();

    /**
     * Hàm dùng để nhận một phiên đấu giá mới vào hệ thống quản lý
     */
    public void addAuction(Auction auction) {
        activeAuctions.add(auction);
        System.out.println("📦 [HỆ THỐNG] Đã thêm phiên đấu giá [" + auction.getItem().getName() + "] vào hàng đợi.");
    }

    /**
     * Khởi động bộ máy điều hành
     */
    public void startEngine() {
        System.out.println("🚀 [ENGINE] Auction Engine đã khởi động. Đang giám sát thời gian...");

        // Cài đặt lịch trình: Bắt đầu ngay (0s), lặp lại mỗi 5 giây
        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();

            // Quét qua toàn bộ các phiên đấu giá đang có trên sàn
            for (Auction auction : activeAuctions) {
                // Nếu phiên vẫn đang mở VÀ thời gian hiện tại đã vượt qua hạn chót
                if (auction.isActive() && now.isAfter(auction.getEndTime())) {

                    // Khóa phiên lại, không cho ai đặt giá nữa
                    auction.endAuction();

                    System.out.println("🔔 [CHỐT SỔ] Đã đóng phiên đấu giá [" + auction.getItem().getName() + "].");
                    System.out.println("   -> Giá chốt: " + auction.getCurrentPrice());

                    // (Tùy chọn) Có thể in thêm người thắng cuộc nếu cần
                    // System.out.println("   -> Người thắng: " + auction.getCurrentHighestBidder().getUsername());
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Dừng bộ máy, giải phóng bộ nhớ khi tắt Server
     */
    public void stopEngine() {
        System.out.println("🛑 [ENGINE] Đang tắt hệ thống...");
        scheduler.shutdown();
    }
}