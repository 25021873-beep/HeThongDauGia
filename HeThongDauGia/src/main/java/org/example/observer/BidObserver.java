package org.example.observer;

import java.math.BigDecimal;

/**
 * Observer interface cho các sự kiện trong phiên đấu giá.
 *
 * Ai implement:
 *   - ClientHandler (server-side): nhận event → serialize JSON → gửi qua socket
 *
 */
public interface BidObserver {

    /**
     * Gọi khi có bid mới hợp lệ.
     * Auction.notifyBidPlaced() loop qua observers và gọi method này.
     */
    void onBidPlaced(int auctionId, String bidderUsername, BigDecimal newPrice);

    /**
     * Gọi khi phiên kết thúc.
     * AuctionEngine gọi auction.notifyAuctionEnded() sau closeAuction().
     *
     * @param winnerUsername null nếu không có ai đặt giá
     */
    void onAuctionEnded(int auctionId, String auctionName,
                        String winnerUsername, BigDecimal finalPrice);
}