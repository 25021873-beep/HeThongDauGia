package org.example.entity.item;

public class Art extends Item {
    private String author;

    public Art() {
        this.itemType = "ART";
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
