package org.example.entity.user;

public class Seller extends User {
    private double rating;

    public Seller() {
        this.role = "SELLER";
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public void doSomething() {
        System.out.println("T là Seller");
    }
}
