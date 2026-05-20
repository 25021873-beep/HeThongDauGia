package org.example.dto.response;

import java.math.BigDecimal;

/**
 * BUG FIX:
 *   Phiên bản cũ có 2 tham số message trong constructor (message + message1) → thừa.
 *   Override getMessage() → che khuất method của BaseResponse → Gson serialize sai.
 *   → FIX: xóa field message thừa, bỏ @Override getMessage().
 *
 * JSON output:
 * {"status":"SUCCESS","message":"Dang ky auto-bid thanh cong",
 *  "auctionId":3,"maxBid":10000000,"increment":500000}
 */
public class AutoBidResponse extends BaseResponse {

    private final long       auctionId;
    private final BigDecimal maxBid;
    private final BigDecimal increment;

    public AutoBidResponse(String status, String message,
                           long auctionId, BigDecimal maxBid, BigDecimal increment) {
        super(status, message);
        this.auctionId = auctionId;
        this.maxBid    = maxBid;
        this.increment = increment;
    }

    public long       getAuctionId() { return auctionId; }
    public BigDecimal getMaxBid()    { return maxBid; }
    public BigDecimal getIncrement() { return increment; }
}