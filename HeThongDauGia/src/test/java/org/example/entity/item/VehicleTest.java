package org.example.entity.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    @Test
    void constructorSetsTypeAndEngineTypeCanBeChanged() {
        Vehicle vehicle = new Vehicle();

        vehicle.setEngineType("Hybrid");

        assertEquals("VEHICLE", vehicle.getItemType());
        assertEquals("Hybrid", vehicle.getEngineType());
    }
}
