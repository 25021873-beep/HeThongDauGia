package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JSON output:
 * {"status":"SUCCESS","message":"Tao phien dau gia thanh cong",
 *  "auctionId":5,"itemName":"iPhone 15","startingPrice":20000000,
 *  "startTime":"2025-12-01T18:00:00","endTime":"2025-12-01T20:00:00"}
 */
public class CreateAuctionResponse extends BaseResponse {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final int        auctionId;
    private final String     itemName;
    private final BigDecimal startingPrice;
    private final String     startTime;
    private final String     endTime;

    public CreateAuctionResponse(int auctionId, String itemName,
                                 BigDecimal startingPrice,
                                 LocalDateTime startTime, LocalDateTime endTime) {
        super(STATUS_SUCCESS, "Tao phien dau gia thanh cong");
        this.auctionId     = auctionId;
        this.itemName      = itemName;
        this.startingPrice = startingPrice;
        this.startTime     = startTime != null ? startTime.format(FMT) : null;
        this.endTime       = endTime   != null ? endTime.format(FMT)   : null;
    }

    public int        getAuctionId()     { return auctionId; }
    public String     getItemName()      { return itemName; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public String     getStartTime()     { return startTime; }
    public String     getEndTime()       { return endTime; }
}