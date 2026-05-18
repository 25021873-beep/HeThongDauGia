package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BidHistoryResponse extends BaseResponse {
    private long auctionId;
    private List<BidPoint> history;

    public BidHistoryResponse(String status, String message, long auctionId, List<BidPoint> history) {
        super(status, message);
        this.auctionId = auctionId;
        this.history = history;
    }

    // Inner class — mỗi điểm trên chart
    public static class BidPoint {
        private String        bidderUsername;
        private BigDecimal price;
        private LocalDateTime bidTime;
        // constructor, getters
    }
}
