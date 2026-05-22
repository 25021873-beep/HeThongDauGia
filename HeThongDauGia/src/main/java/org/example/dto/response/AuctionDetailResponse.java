package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionDetailResponse extends BaseResponse {
    // Thông tin phiên đấu giá
    private int auctionId;
    private BigDecimal currentPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String auctionStatus;

    // Thông tin chi tiết sản phẩm
    private String itemName;
    private String itemDescription;
    private BigDecimal startingPrice;

    // Các trường đa hình (tuỳ loại sản phẩm sẽ có giá trị hoặc null)
    private String itemType; // "ART", "ELECTRONICS", "VEHICLE"
    private Integer warrantyMonths;
    private String author;
    private String engineType;

    public AuctionDetailResponse(String status, String message, int auctionId, BigDecimal currentPrice,
                                 LocalDateTime startTime, LocalDateTime endTime, String auctionStatus,
                                 String itemName, String itemDescription, BigDecimal startingPrice,
                                 String itemType, Integer warrantyMonths, String author, String engineType) {
        super(status, message);
        this.auctionId = auctionId;
        this.currentPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.auctionStatus = auctionStatus;

        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.startingPrice = startingPrice;
        this.itemType = itemType;
        this.warrantyMonths = warrantyMonths;
        this.author = author;
        this.engineType = engineType;
    }
}