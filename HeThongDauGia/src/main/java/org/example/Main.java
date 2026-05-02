import org.example.dao.BidTransactionDAO;
import org.example.dao.AuctionDAO; // Cần thằng này để cập nhật giá hiện tại lên bảng Auctions
import org.example.entity.BidTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        BidTransactionDAO bidDAO = new BidTransactionDAO();
        AuctionDAO auctionDAO = new AuctionDAO(); // Gọi đệ cứng ra hỗ trợ

        System.out.println("--- BẮT ĐẦU TEST BID_TRANSACTION_DAO ---");

        int targetAuctionId = 2;
        int bidderId = 1;
        BigDecimal newBidAmount = new BigDecimal("700000");

        // 1. Test ghi nhận lịch sử đặt giá
        BidTransaction newBid = new BidTransaction();
        newBid.setAuctionId(targetAuctionId);
        newBid.setBidderId(bidderId);
        newBid.setBidPrice(newBidAmount);
        newBid.setBidTime(LocalDateTime.now());

        boolean isBidRecorded = bidDAO.addBid(newBid);
        System.out.println("1. Ghi nhận lịch sử đặt giá thành công? " + isBidRecorded);

        // NẾU GHI NHẬN LỊCH SỬ THÀNH CÔNG -> PHẢI ĐỔI LUÔN GIÁ HIỆN TẠI BÊN BẢNG AUCTIONS
        if (isBidRecorded) {
            boolean isAuctionUpdated = auctionDAO.updateCurrentPrice(targetAuctionId, newBidAmount);
            System.out.println(" -> Cập nhật giá hiện tại của phiên đấu giá lên " + newBidAmount + "? " + isAuctionUpdated);
        }

        // 2. Test in ra bảng điện tử (Lịch sử các người chơi đã đặt)
        System.out.println("\n2. Bảng lịch sử thả giá của Phiên ID " + targetAuctionId + ":");
        List<BidTransaction> history = bidDAO.getBidsByAuction(targetAuctionId);
        for (BidTransaction b : history) {
            System.out.println("   -> Bidder ID " + b.getBidderId() + " đã hô " + b.getBidPrice() + " đ lúc " + b.getBidTime());
        }
    }
}