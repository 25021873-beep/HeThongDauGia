package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JSON output:
 * {"status":"SUCCESS","message":"Vao phong thanh cong",
 *  "auctionId":3,"auctionName":"Tranh Son Dau",
 *  "currentPrice":5000000,"endTime":"2025-12-01T20:00:00","auctionStatus":"RUNNING"}
 */
public class JoinResponse extends BaseResponse {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final int        auctionId;
    private final String     auctionName;
    private final BigDecimal currentPrice;
    private final String     endTime;      // String để Gson serialize gọn
    private final String     auctionStatus;

    public JoinResponse(int auctionId, String auctionName,
                        BigDecimal currentPrice, LocalDateTime endTime, String auctionStatus) {
        super(STATUS_SUCCESS, "Vao phong thanh cong");
        this.auctionId     = auctionId;
        this.auctionName   = auctionName;
        this.currentPrice  = currentPrice;
        this.endTime       = endTime != null ? endTime.format(FMT) : null;
        this.auctionStatus = auctionStatus;
    }

    public int        getAuctionId()    { return auctionId; }
    public String     getAuctionName()  { return auctionName; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public String     getEndTime()      { return endTime; }
    public String     getAuctionStatus(){ return auctionStatus; }
}