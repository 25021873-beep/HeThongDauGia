package org.example.entity.item;

public class Vehicle extends Item {
    private String engineType;

    public Vehicle() {
        this.itemType = "VEHICLE";
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }
}