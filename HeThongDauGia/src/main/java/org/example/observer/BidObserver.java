package org.example.observer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Observer interface cho các sự kiện trong phiên đấu giá.
 *
 * Ai implement:
 *   - ClientHandler (server-side): nhận event → serialize JSON → gửi qua socket
 */
public interface BidObserver {

    /** Gọi khi có bid mới hợp lệ */
    void onBidPlaced(int auctionId, String bidderUsername, BigDecimal newPrice);

    /** Gọi khi phiên kết thúc. winnerUsername = null nếu không có ai đặt giá */
    void onAuctionEnded(int auctionId, String auctionName,
                        String winnerUsername, BigDecimal finalPrice);

    /** Gọi khi phiên bị gia hạn do anti-snipe */
    void onAuctionExtended(int auctionId, LocalDateTime newEndTime, int addedSeconds);
}