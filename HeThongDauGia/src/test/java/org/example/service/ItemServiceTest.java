package org.example.service;

import org.example.entity.item.Electronics;
import org.example.exception.item.InvalidItemNameException;
import org.example.exception.item.InvalidItemPriceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {

    @Test
    void postItemRejectsNonPositiveStartingPriceBeforeDatabaseLookup() {
        Electronics item = validItem();
        item.setStartingPrice(BigDecimal.ZERO);

        assertThrows(InvalidItemPriceException.class, () -> ItemService.getInstance().postItem(item));
    }

    @Test
    void postItemRejectsBlankNameBeforeDatabaseLookup() {
        Electronics item = validItem();
        item.setName(" ");

        assertThrows(InvalidItemNameException.class, () -> ItemService.getInstance().postItem(item));
    }

    private static Electronics validItem() {
        Electronics item = new Electronics();
        item.setName("Laptop");
        item.setStartingPrice(BigDecimal.TEN);
        item.setSellerId(1);
        return item;
    }
}
