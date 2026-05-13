package org.example.dto.response;

import java.math.BigDecimal;

/**
 * Thông tin tóm tắt 1 phiên đấu giá - dùng trong AuctionListResponse.
 * Không extends BaseResponse vì đây là data object thuần túy.
 *
 * JSON của 1 item (nằm trong mảng "auctions"):
 * {"id":3,"name":"Tranh Son Dau","currentPrice":5000000,"status":"RUNNING"}
 */
public class AuctionSummary {

    private final int        id;
    private final String     name;
    private final BigDecimal currentPrice;
    private final String     status;

    public AuctionSummary(int id, String name, BigDecimal currentPrice, String status) {
        this.id           = id;
        this.name         = name;
        this.currentPrice = currentPrice;
        this.status       = status;
    }

    public int        getId()           { return id; }
    public String     getName()         { return name; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public String     getStatus()       { return status; }
}