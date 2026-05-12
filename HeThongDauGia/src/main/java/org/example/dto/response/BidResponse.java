package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Response trả về sau lệnh BID thành công (chỉ gửi riêng cho người đặt giá).
 * Thông báo multicast cho các viewer dùng BidUpdateResponse riêng.
 *
 * Serialize format:
 *   "SUCCESS|Dat gia thanh cong|<auctionId>|<bidderUsername>|<amount>|<bidTime>"
 *
 * Ví dụ:
 *   "SUCCESS|Dat gia thanh cong|3|alice|6000000|2025-12-01T19:45:30"
 */
public class BidResponse extends BaseResponse {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final int           auctionId;
    private final String        bidderUsername;
    private final BigDecimal    amount;
    private final LocalDateTime bidTime;

    public BidResponse(int auctionId, String bidderUsername,
                       BigDecimal amount, LocalDateTime bidTime) {
        super(STATUS_SUCCESS, "Dat gia thanh cong");
        this.auctionId      = auctionId;
        this.bidderUsername = bidderUsername;
        this.amount         = amount;
        this.bidTime        = bidTime;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int           getAuctionId()      { return auctionId; }
    public String        getBidderUsername() { return bidderUsername; }
    public BigDecimal    getAmount()         { return amount; }
    public LocalDateTime getBidTime()        { return bidTime; }

    // ── Serialize ─────────────────────────────────────────────────────────────

    @Override
    public String serialize() {
        return STATUS_SUCCESS    + DELIMITER
                + getMessage()   + DELIMITER
                + auctionId      + DELIMITER
                + bidderUsername + DELIMITER
                + amount         + DELIMITER
                + (bidTime != null ? bidTime.format(FORMATTER) : "N/A");
    }
}