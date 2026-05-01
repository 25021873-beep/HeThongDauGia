package org.example.entity;

public class Item {
    private int id;
    private String name;
    private String description;
    private double startingPrice;
    private int sellerId; // Cái này dùng để nối với ID của User

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getStartingPrice() {
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

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }
}