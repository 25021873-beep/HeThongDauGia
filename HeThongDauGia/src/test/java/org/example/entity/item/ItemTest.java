package org.example.entity.item;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void commonItemFieldsCanBeSetAndRead() {
        TestItem item = new TestItem();

        item.setId(1);
        item.setName("Laptop");
        item.setDescription("Gaming laptop");
        item.setStartingPrice(new BigDecimal("1200"));
        item.setSellerId(10);
        item.setStatus("AVAILABLE");
        item.setItemType("CUSTOM");

        assertEquals(1, item.getId());
        assertEquals("Laptop", item.getName());
        assertEquals("Gaming laptop", item.getDescription());
        assertEquals(new BigDecimal("1200"), item.getStartingPrice());
        assertEquals(10, item.getSellerId());
        assertEquals("AVAILABLE", item.getStatus());
        assertEquals("CUSTOM", item.getItemType());
    }

    private static final class TestItem extends Item {
    }
}
