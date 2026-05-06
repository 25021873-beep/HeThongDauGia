package org.example.entity.item;

public class Vehicle extends Item {
    private String engine_type;

    public Vehicle() {
        this.itemType = "VEHICLE";
    }

    public String getEngine_type() {
        return engine_type;
    }

    public void setEngine_type(String engine_type) {
        this.engine_type = engine_type;
    }
}
