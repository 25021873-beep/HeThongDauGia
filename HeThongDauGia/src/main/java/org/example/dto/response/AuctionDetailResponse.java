package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionDetailResponse extends BaseResponse {
    // Thông tin phiên đấu giá
    private final int auctionId;
    private final BigDecimal currentPrice;
    private final BigDecimal stepPrice;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String auctionStatus;
    private final BigDecimal startingPrice;


    // Thông tin chi tiết sản phẩm
    private final String itemName;
    private final String itemDescription;

    // Các trường đa hình (tuỳ loại sản phẩm sẽ có giá trị hoặc null)
    private final String itemType; // "ART", "ELECTRONICS", "VEHICLE"
    private final Integer warrantyMonths;
    private final String author;
    private final String engineType;
    private final String sellerUsername;

    public AuctionDetailResponse(String status, String message, int auctionId, BigDecimal currentPrice, BigDecimal startingPrice, BigDecimal stepPrice,
                                 LocalDateTime startTime, LocalDateTime endTime, String auctionStatus,
                                 String itemName, String itemDescription,
                                 String itemType, Integer warrantyMonths, String author, String engineType,
                                 String sellerUsername) {
        super(status, message);
        this.auctionId = auctionId;
        this.currentPrice = currentPrice;
        this.stepPrice = stepPrice;
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
        this.sellerUsername = sellerUsername;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getStepPrice() {
        return stepPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getAuctionStatus() {
        return auctionStatus;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public String getItemType() {
        return itemType;
    }

    public Integer getWarrantyMonths() {
        return warrantyMonths;
    }

    public String getAuthor() {
        return author;
    }

    public String getEngineType() {
        return engineType;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }
}