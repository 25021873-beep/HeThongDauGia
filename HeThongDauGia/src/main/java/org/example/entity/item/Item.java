package org.example.entity.item;

import org.example.entity.BaseEntity;

import java.math.BigDecimal;

public abstract class Item extends BaseEntity {
    protected String name;
    protected String description;
    protected BigDecimal startingPrice;
    protected int sellerId;
    protected String status;
    protected String itemType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        if (startingPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá khởi điểm không được âm!");
        }
        this.startingPrice = startingPrice;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}
