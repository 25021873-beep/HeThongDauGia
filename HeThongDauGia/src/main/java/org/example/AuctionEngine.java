package org.example;

import org.example.dao.AuctionDAO;
import org.example.service.AuctionService;
import org.example.entity.Auction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionEngine {

    //Khởi tạo tài nguyên hệ thống
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final List<Auction> activeAuctions = new CopyOnWriteArrayList<>();

    private final AuctionService auctionService;
    private final AuctionDAO auctionDAO;

    public AuctionEngine(AuctionService auctionService, AuctionDAO auctionDAO) {
        this.auctionService = auctionService;
        this.auctionDAO = auctionDAO;
    }


    public void startEngine() {
        System.out.println("[ENGINE] Đang khởi động hệ thống...");

        // BƯỚC QUAN TRỌNG: Phục hồi trí nhớ từ Database khi Server vừa bật
        List<Auction> runningAuctions = auctionDAO.getActiveAuctions();
        if (runningAuctions != null && !runningAuctions.isEmpty()) {
            activeAuctions.addAll(runningAuctions);
        }
        System.out.println("[ENGINE] Đã nạp " + activeAuctions.size() + " phiên đấu giá đang chạy vào bộ nhớ.");

        // Bắt đầu vòng lặp thời gian (Chạy ngầm mỗi 5 giây 1 lần)
        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            List<Auction> finishedAuctions = new ArrayList<>();

            for (Auction auction : activeAuctions) {
                if (now.isAfter(auction.getEndTime())) {
                    boolean isClosed = auctionService.closeAuction(auction.getId());
                    if (isClosed) {
                        // TODO: Chỗ này sau cắm Socket thì bắn sự kiện (Broadcast) báo có người thắng
                        finishedAuctions.add(auction);
                    } else {
                        System.err.println("[ENGINE - LỖI] Không thể chốt phiên ID: " + auction.getId());
                    }
                }
            }

            if (!finishedAuctions.isEmpty()) {
                activeAuctions.removeAll(finishedAuctions);
            }

        }, 0, 5, TimeUnit.SECONDS);
    }

    public void stopEngine() {
        System.out.println("[ENGINE] Đang tắt hệ thống, đợi các luồng hoàn tất...");
        scheduler.shutdown();
        try {
            // Cho phép công nhân làm nốt việc dở dang trong tối đa 2 giây
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                System.out.println("[ENGINE] Hết thời gian chờ, ép buộc tắt luồng!");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt(); // Phục hồi trạng thái ngắt
        }
        System.out.println("[ENGINE] Đã tắt an toàn.");
    }


    // Các hàm public API để liên kết Engine với các tầng khác

    // Dùng cho Tầng Service: Nạp thêm Auction
    public void addAuction(Auction auction) {
        if (auction == null || !"RUNNING".equals(auction.getStatus())){
            throw new IllegalArgumentException("[ENGINE TỪ CHỐI] Chỉ được phép nạp phiên đấu giá có trạng thái RUNNING!");
        }
        activeAuctions.add(auction);
        System.out.println("[ENGINE] Đã nạp phiên đấu giá mới vào hệ thống giám sát: ID " + auction.getId());
    }


   // Dùng cho Tầng Mạng: Trả về danh sách các phiên đang mở (Chỉ đọc, không cho sửa)
    public List<Auction> getActiveAuctions() {
        return Collections.unmodifiableList(activeAuctions);
    }


     // Dùng cho Tầng Mạng: Tìm kiếm phiên bằng ID chuẩn xác
    public Auction findActiveAuctionById(int auctionId) {
        for (Auction auction : activeAuctions) {
            if (auction.getId() == auctionId) {
                return auction;
            }
        }
        return null;
    }
}