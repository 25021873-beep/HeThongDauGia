package org.example.dto.response;

import java.math.BigDecimal;

/**
 * Đại diện thông tin tóm tắt của MỘT phiên đấu giá trong danh sách.
 * Được dùng bên trong AuctionListResponse.
 *
 * Serialize 1 item (dùng dấu ':' phân cách field nội bộ):
 *   "<id>:<name>:<currentPrice>:<status>"
 *
 * Ví dụ:
 *   "3:Tranh Son Dau:5000000:ACTIVE"
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

    /** Serialize 1 item, dùng ':' làm field separator (không xung đột với '|' của protocol) */
    public String serialize() {
        return id + ":" + name + ":" + currentPrice + ":" + status;
    }
}