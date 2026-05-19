package org.example.dto.response;

import java.math.BigDecimal;

public class AutoBidResponse extends BaseResponse {
    private long       auctionId;
    private BigDecimal maxBid;
    private BigDecimal increment;
    private String     message;

    public AutoBidResponse(String status, String message, long auctionId, BigDecimal maxBid, BigDecimal increment, String message1) {
        super(status, message);
        this.auctionId = auctionId;
        this.maxBid = maxBid;
        this.increment = increment;
        this.message = message1;
    }

    public long getAuctionId() {
        return auctionId;
    }

    public BigDecimal getMaxBid() {
        return maxBid;
    }

    public BigDecimal getIncrement() {
        return increment;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
