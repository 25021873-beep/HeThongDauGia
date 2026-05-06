package org.example.entity;

import java.math.BigDecimal;

public class Item {
    private int id;
    private String name;
    private String description;
    private BigDecimal startingPrice;
    private int sellerId; // Cái này dùng để nối với ID của User

    public Item(int id, String name, String description, BigDecimal startingPrice, int sellerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.sellerId = sellerId;
    }

    public Item() {}

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
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
}