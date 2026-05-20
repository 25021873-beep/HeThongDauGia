package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Response trả về lịch sử bid cho một phiên — dùng để vẽ line chart.
 *
 * BUG FIX: BidPoint.bidTime dùng LocalDateTime → Gson serialize thành mảng số [2025,12,1,...]
 *   → Client khó parse. Fix: convert sang String ISO format trước khi trả về.
 *
 * JSON output:
 * {"status":"SUCCESS","message":"Lich su dau gia","auctionId":3,"count":3,
 *  "history":[
 *    {"bidderUsername":"alice","price":5500000,"bidTime":"2025-12-01T19:30:00"},
 *    {"bidderUsername":"bob",  "price":6000000,"bidTime":"2025-12-01T19:35:00"}
 *  ]}
 */
public class BidHistoryResponse extends BaseResponse {

    private final long          auctionId;
    private final int           count;
    private final List<BidPoint> history;

    public BidHistoryResponse(String status, String message,
                              long auctionId, List<BidPoint> history) {
        super(status, message);
        this.auctionId = auctionId;
        this.history   = history;
        this.count     = history != null ? history.size() : 0;
    }

    public long            getAuctionId() { return auctionId; }
    public int             getCount()     { return count; }
    public List<BidPoint>  getHistory()   { return history; }

    // ── Inner class: 1 điểm trên line chart ──────────────────────────────────

    public static class BidPoint {

        private static final DateTimeFormatter FMT =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        private final String     bidderUsername;
        private final BigDecimal price;
        private final String     bidTime; // String để Gson serialize gọn, client dễ parse

        public BidPoint(String bidderUsername, BigDecimal price, LocalDateTime bidTime) {
            this.bidderUsername = bidderUsername;
            this.price          = price;
            this.bidTime        = bidTime != null ? bidTime.format(FMT) : null;
        }

        public String     getBidderUsername() { return bidderUsername; }
        public BigDecimal getPrice()          { return price; }
        public String     getBidTime()        { return bidTime; }
    }
}