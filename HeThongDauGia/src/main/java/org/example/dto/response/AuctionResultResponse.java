package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Broadcast khi phiên đấu giá kết thúc.
 *
 * JSON output (có winner):
 * {"status":"AUCTION_END","message":"Phien dau gia da ket thuc",
 *  "auctionId":3,"auctionName":"Tranh Son Dau",
 *  "winnerUsername":"alice","finalPrice":6000000,
 *  "endTime":"2025-12-01T20:00:00","hasWinner":true}
 *
 * JSON output (không có winner):
 * {"status":"AUCTION_END",...,"winnerUsername":null,"hasWinner":false}
 */
public class AuctionResultResponse extends BaseResponse {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final int        auctionId;
    private final String     auctionName;
    private final String     winnerUsername; // null nếu không có ai đặt giá
    private final BigDecimal finalPrice;
    private final String     endTime;
    private final boolean    hasWinner;

    public AuctionResultResponse(int auctionId, String auctionName,
                                 String winnerUsername, BigDecimal finalPrice,
                                 LocalDateTime endTime) {
        super(STATUS_AUCTION_END, "Phien dau gia da ket thuc");
        this.auctionId      = auctionId;
        this.auctionName    = auctionName;
        this.winnerUsername = winnerUsername;
        this.finalPrice     = finalPrice;
        this.endTime        = endTime != null ? endTime.format(FMT) : null;
        this.hasWinner      = winnerUsername != null;
    }

    /** Factory: không có ai đặt giá */
    public static AuctionResultResponse noWinner(int auctionId, String auctionName,
                                                 BigDecimal startingPrice,
                                                 LocalDateTime endTime) {
        return new AuctionResultResponse(auctionId, auctionName, null, startingPrice, endTime);
    }

    public int        getAuctionId()      { return auctionId; }
    public String     getAuctionName()    { return auctionName; }
    public String     getWinnerUsername() { return winnerUsername; }
    public BigDecimal getFinalPrice()     { return finalPrice; }
    public String     getEndTime()        { return endTime; }
    public boolean    isHasWinner()       { return hasWinner; }
}