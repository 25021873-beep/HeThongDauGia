package org.example.dto.response;

import java.math.BigDecimal;

/**
 * Response broadcast (multicast) tới TẤT CẢ viewer trong phòng khi có bid mới.
 * Đây là thay thế cho chuỗi "UPDATE|..." thủ công trong ClientHandler.
 *
 * Serialize format:
 *   "UPDATE|<auctionId>|<bidderUsername>|<newPrice>"
 *
 * Ví dụ:
 *   "UPDATE|3|alice|6000000"
 */
public class BidUpdateResponse extends BaseResponse {

    public static final String STATUS_UPDATE = "UPDATE";

    private final int        auctionId;
    private final String     bidderUsername;
    private final BigDecimal newPrice;

    public BidUpdateResponse(int auctionId, String bidderUsername, BigDecimal newPrice) {
        super(STATUS_UPDATE, "Gia moi duoc cap nhat");
        this.auctionId      = auctionId;
        this.bidderUsername = bidderUsername;
        this.newPrice       = newPrice;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int        getAuctionId()      { return auctionId; }
    public String     getBidderUsername() { return bidderUsername; }
    public BigDecimal getNewPrice()       { return newPrice; }

    // ── Serialize ─────────────────────────────────────────────────────────────

    /**
     * Giữ đúng format cũ "UPDATE|auctionId|username|price"
     * để client hiện tại không cần sửa parse logic.
     */
    @Override
    public String serialize() {
        return STATUS_UPDATE     + DELIMITER
                + auctionId      + DELIMITER
                + bidderUsername + DELIMITER
                + newPrice;
    }
}