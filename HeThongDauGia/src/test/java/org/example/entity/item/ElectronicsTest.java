package org.example.entity.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElectronicsTest {

    @Test
    void constructorSetsTypeAndWarrantyCanBeChanged() {
        Electronics electronics = new Electronics();

        electronics.setWarrantyMonths(24);

        assertEquals("ELECTRONICS", electronics.getItemType());
        assertEquals(24, electronics.getWarrantyMonths());
    }
}
