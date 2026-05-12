package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Response trả về sau lệnh JOIN thành công.
 * Gửi đầy đủ thông tin phòng đấu giá để client hiển thị.
 *
 * Serialize format:
 *   "SUCCESS|Vao phong thanh cong|<auctionId>|<auctionName>|<currentPrice>|<endTime>|<status>"
 *
 * Ví dụ:
 *   "SUCCESS|Vao phong thanh cong|3|Tranh Son Dau|5000000|2025-12-01T20:00:00|ACTIVE"
 */
public class JoinResponse extends BaseResponse {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final int           auctionId;
    private final String        auctionName;
    private final BigDecimal    currentPrice;
    private final LocalDateTime endTime;
    private final String        status;

    public JoinResponse(int auctionId, String auctionName,
                        BigDecimal currentPrice, LocalDateTime endTime, String status) {
        super(STATUS_SUCCESS, "Vao phong thanh cong");
        this.auctionId    = auctionId;
        this.auctionName  = auctionName;
        this.currentPrice = currentPrice;
        this.endTime      = endTime;
        this.status       = status;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int           getAuctionId()   { return auctionId; }
    public String        getAuctionName() { return auctionName; }
    public BigDecimal    getCurrentPrice(){ return currentPrice; }
    public LocalDateTime getEndTime()     { return endTime; }
    public String        getStatus()      { return status; }

    // ── Serialize ─────────────────────────────────────────────────────────────

    @Override
    public String serialize() {
        return STATUS_SUCCESS  + DELIMITER
                + getMessage() + DELIMITER
                + auctionId    + DELIMITER
                + auctionName  + DELIMITER
                + currentPrice + DELIMITER
                + (endTime != null ? endTime.format(FORMATTER) : "N/A") + DELIMITER
                + status;
    }
}