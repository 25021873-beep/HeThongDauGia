package com.auction.core.model;

public class Item {
    private String name;
    private String description;
    private double price;

    public Item(String name, String description, double price)  {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public String getName() {
        return name;
    }
}

