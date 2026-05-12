package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Response broadcast tới TẤT CẢ viewer khi phiên đấu giá kết thúc.
 * Gọi từ AuctionEngine khi endAuction() được trigger.
 *
 * Serialize format:
 *   "AUCTION_END|<auctionId>|<auctionName>|<winnerUsername>|<finalPrice>|<endTime>"
 *
 * Trường hợp không có ai đặt giá (không có winner):
 *   "AUCTION_END|<auctionId>|<auctionName>|NONE|<startingPrice>|<endTime>"
 *
 * Ví dụ:
 *   "AUCTION_END|3|Tranh Son Dau|alice|6000000|2025-12-01T20:00:00"
 *   "AUCTION_END|3|Tranh Son Dau|NONE|5000000|2025-12-01T20:00:00"
 */
public class AuctionResultResponse extends BaseResponse {

    public static final String STATUS_AUCTION_END = "AUCTION_END";
    public static final String NO_WINNER          = "NONE";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final int           auctionId;
    private final String        auctionName;
    private final String        winnerUsername; // "NONE" nếu không có ai đặt giá
    private final BigDecimal    finalPrice;
    private final LocalDateTime endTime;

    /** Constructor khi có người thắng */
    public AuctionResultResponse(int auctionId, String auctionName,
                                 String winnerUsername, BigDecimal finalPrice,
                                 LocalDateTime endTime) {
        super(STATUS_AUCTION_END, "Phien dau gia da ket thuc");
        this.auctionId      = auctionId;
        this.auctionName    = auctionName;
        this.winnerUsername = winnerUsername != null ? winnerUsername : NO_WINNER;
        this.finalPrice     = finalPrice;
        this.endTime        = endTime;
    }

    /** Constructor khi KHÔNG có người thắng (không ai đặt giá) */
    public static AuctionResultResponse noWinner(int auctionId, String auctionName,
                                                 BigDecimal startingPrice,
                                                 LocalDateTime endTime) {
        return new AuctionResultResponse(auctionId, auctionName, NO_WINNER, startingPrice, endTime);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int           getAuctionId()      { return auctionId; }
    public String        getAuctionName()    { return auctionName; }
    public String        getWinnerUsername() { return winnerUsername; }
    public BigDecimal    getFinalPrice()     { return finalPrice; }
    public LocalDateTime getEndTime()        { return endTime; }
    public boolean       hasWinner()         { return !NO_WINNER.equals(winnerUsername); }

    // ── Serialize ─────────────────────────────────────────────────────────────

    @Override
    public String serialize() {
        return STATUS_AUCTION_END + DELIMITER
                + auctionId       + DELIMITER
                + auctionName     + DELIMITER
                + winnerUsername  + DELIMITER
                + finalPrice      + DELIMITER
                + (endTime != null ? endTime.format(FORMATTER) : "N/A");
    }
}
