package org.example.service;

import org.example.entity.item.Electronics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {

    @Test
    void postItemRejectsNonPositiveStartingPriceBeforeDatabaseLookup() {
        Electronics item = validItem();
        item.setStartingPrice(BigDecimal.ZERO);

        assertFalse(new ItemService().postItem(item));
    }

    @Test
    void postItemRejectsBlankNameBeforeDatabaseLookup() {
        Electronics item = validItem();
        item.setName(" ");

        assertFalse(new ItemService().postItem(item));
    }

    private static Electronics validItem() {
        Electronics item = new Electronics();
        item.setName("Laptop");
        item.setStartingPrice(BigDecimal.TEN);
        item.setSellerId(1);
        return item;
    }
}
