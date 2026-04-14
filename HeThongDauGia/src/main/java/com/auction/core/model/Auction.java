package com.auction.core.model;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


// import com.auction.model.Item;
// import com.auction.model.Bidder;

public class Auction {
    // 1. LIÊN KẾT ĐỐI TƯỢNG (Thay vì dùng String)
    private Item item;                  // Món hàng được đem ra đấu giá
    private double currentPrice;
    private Bidder currentHighestBidder; // Người đang giữ giá cao nhất
    private LocalDateTime endTime;
    private boolean isActive;

    // 2. LƯU LỊCH SỬ (Thường sơ đồ UML sẽ yêu cầu lớp BidTransaction)
    private List<BidTransaction> transactionHistory;

    // Constructor
    public Auction(Item item, double startingPrice, LocalDateTime endTime) {
        this.item = item;
        this.currentPrice = startingPrice;
        this.endTime = endTime;
        this.isActive = true;
        this.transactionHistory = new ArrayList<>();
    }

    // Vẫn giữ lại "Khóa" đa luồng, nhưng nhận vào một Đối tượng Bidder
    public synchronized boolean placeBid(Bidder bidder, double bidAmount) {
        if (!isActive) {
            System.out.println("❌ Phiên đấu giá cho [" + item.getName() + "] đã đóng!");
            return false;
        }

        if (bidAmount > currentPrice) {
            this.currentPrice = bidAmount;
            this.currentHighestBidder = bidder; // Cập nhật người thắng tạm thời

            // Lưu lại lịch sử giao dịch này
            transactionHistory.add(new BidTransaction(bidder, bidAmount, LocalDateTime.now()));

            System.out.println("✅ " + bidder.getUsername() + " đặt " + bidAmount + " cho [" + item.getName() + "]");
            return true;
        }
        return false;
    }

    // Các Getter/Setter cần thiết...
    public Item getItem() { return item; }
    public boolean isActive() { return isActive; }
    public void endAuction() { this.isActive = false; }
    // --- BỔ SUNG CÁC HÀM GETTER CÒN THIẾU ---
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public Bidder getCurrentHighestBidder() {
        return currentHighestBidder;
    }
    // ...
}













