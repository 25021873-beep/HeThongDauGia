package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gửi riêng cho người vừa đặt giá thành công.
 *
 * JSON output:
 * {"status":"SUCCESS","message":"Dat gia thanh cong",
 *  "auctionId":3,"bidderUsername":"alice","amount":6000000,
 *  "bidTime":"2025-12-01T19:45:30"}
 */
public class BidResponse extends BaseResponse {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final int        auctionId;
    private final String     bidderUsername;
    private final BigDecimal amount;
    private final String     bidTime;
    private final BigDecimal newBalance;

    public BidResponse(int auctionId, String bidderUsername,
                       BigDecimal amount, LocalDateTime bidTime, BigDecimal newBalance) {
        super(STATUS_SUCCESS, "Dat gia thanh cong");
        this.auctionId      = auctionId;
        this.bidderUsername = bidderUsername;
        this.amount         = amount;
        this.bidTime        = bidTime != null ? bidTime.format(FMT) : null;
        this.newBalance     = newBalance;
    }

    public int        getAuctionId()      { return auctionId; }
    public String     getBidderUsername() { return bidderUsername; }
    public BigDecimal getAmount()         { return amount; }
    public String     getBidTime()        { return bidTime; }
    public BigDecimal getNewBalance()     { return newBalance; }
}