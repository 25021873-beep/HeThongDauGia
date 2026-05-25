package org.example.dto.response;

import java.math.BigDecimal;

/**
 * Broadcast tới TẤT CẢ viewer trong phòng khi có bid mới.
 *
 * JSON output:
 * {"status":"UPDATE","message":"Gia moi duoc cap nhat",
 *  "auctionId":3,"bidderUsername":"alice","newPrice":6000000}
 */
public class BidUpdateResponse extends BaseResponse {

    private final int        auctionId;
    private final String     bidderUsername;
    private final BigDecimal newPrice;

    public BidUpdateResponse(int auctionId, String bidderUsername, BigDecimal newPrice) {
        super(STATUS_UPDATE, "Gia moi duoc cap nhat");
        this.auctionId      = auctionId;
        this.bidderUsername = bidderUsername;
        this.newPrice       = newPrice;
    }

    public int        getAuctionId()      { return auctionId; }
    public String     getBidderUsername() { return bidderUsername; }
    public BigDecimal getNewPrice()       { return newPrice; }
}