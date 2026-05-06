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

    // ========================================================
    // 1. TÀI NGUYÊN HỆ THỐNG (SYSTEM RESOURCES)
    // ========================================================
    // Tối ưu: Chỉ cần 1 công nhân (Thread) để đếm thời gian là đủ, đỡ tốn RAM
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // RAM List: Chứa các phiên đang chạy để check nhanh mà không cần query DB liên tục
    private final List<Auction> activeAuctions = new CopyOnWriteArrayList<>();

    // Các Service/DAO được tiêm (Inject) vào để Engine có thể thao tác với Database
    private final AuctionService auctionService;
    private final AuctionDAO auctionDAO;

    // Khởi tạo Engine bắt buộc phải truyền Service và DAO vào
    public AuctionEngine(AuctionService auctionService, AuctionDAO auctionDAO) {
        this.auctionService = auctionService;
        this.auctionDAO = auctionDAO;
    }

    // ========================================================
    // 2. VÒNG ĐỜI VẬN HÀNH (ENGINE LIFECYCLE)
    // ========================================================

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
                // Nếu thời gian hiện tại đã vượt qua hạn chót của phiên
                if (now.isAfter(auction.getEndTime())) {
                    System.out.println("[ENGINE - CHỐT SỔ] Đã hết giờ phiên ID: " + auction.getId());

                    // Gọi Tầng Service để xử lý tiền bạc, cộng trừ, update Database
                    boolean isClosed = auctionService.closeAuction(auction.getId());

                    if (isClosed) {
                        // TODO: Chỗ này sau cắm Socket thì bắn sự kiện (Broadcast) báo có người thắng
                        finishedAuctions.add(auction);
                    } else {
                        System.err.println("[ENGINE - LỖI] Không thể chốt phiên ID: " + auction.getId());
                    }
                }
            }

            // Dọn dẹp: Xóa các phiên đã chốt khỏi bộ nhớ RAM
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

    // ========================================================
    // 3. API CHO CÁC TẦNG KHÁC SỬ DỤNG (PUBLIC API)
    // ========================================================

    /**
     * Tầng Service gọi hàm này ngay sau khi tạo phiên đấu giá mới thành công
     */
    public void addAuction(Auction auction) {
        activeAuctions.add(auction);
        System.out.println("[ENGINE] Đã nạp phiên đấu giá mới vào hệ thống giám sát: ID " + auction.getId());
    }

    /**
     * Dùng cho Tầng Mạng: Trả về danh sách các phiên đang mở (Chỉ đọc, không cho sửa)
     */
    public List<Auction> getActiveAuctions() {
        return Collections.unmodifiableList(activeAuctions);
    }

    /**
     * Dùng cho Tầng Mạng: Tìm kiếm phiên bằng ID chuẩn xác
     */
    public Auction findAuctionById(int auctionId) {
        for (Auction auction : activeAuctions) {
            if (auction.getId() == auctionId) {
                return auction;
            }
        }
        return null;
    }
}