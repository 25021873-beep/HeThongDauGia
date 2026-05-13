package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Response trả về sau lệnh CREATE_AUCTION thành công.
 *
 * Serialize format:
 *   "SUCCESS|Tao phien dau gia thanh cong|<auctionId>|<itemName>|<startingPrice>|<startTime>|<endTime>"
 *
 * Ví dụ:
 *   "SUCCESS|Tao phien dau gia thanh cong|5|iPhone 15|20000000|2025-12-01T18:00:00|2025-12-01T20:00:00"
 */
public class CreateAuctionResponse extends BaseResponse {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final int           auctionId;
    private final String        itemName;
    private final BigDecimal    startingPrice;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public CreateAuctionResponse(int auctionId, String itemName,
                                 BigDecimal startingPrice,
                                 LocalDateTime startTime, LocalDateTime endTime) {
        super(STATUS_SUCCESS, "Tao phien dau gia thanh cong");
        this.auctionId     = auctionId;
        this.itemName      = itemName;
        this.startingPrice = startingPrice;
        this.startTime     = startTime;
        this.endTime       = endTime;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int           getAuctionId()     { return auctionId; }
    public String        getItemName()      { return itemName; }
    public BigDecimal    getStartingPrice() { return startingPrice; }
    public LocalDateTime getStartTime()     { return startTime; }
    public LocalDateTime getEndTime()       { return endTime; }

    // ── Serialize ─────────────────────────────────────────────────────────────

    @Override
    public String serialize() {
        return STATUS_SUCCESS  + DELIMITER
                + getMessage() + DELIMITER
                + auctionId    + DELIMITER
                + itemName     + DELIMITER
                + startingPrice + DELIMITER
                + (startTime != null ? startTime.format(FORMATTER) : "N/A") + DELIMITER
                + (endTime   != null ? endTime.format(FORMATTER)   : "N/A");
    }
}