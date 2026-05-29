package org.example.service;

import org.example.entity.item.Electronics;
import org.example.exception.item.InvalidItemNameException;
import org.example.exception.item.InvalidItemPriceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {



    @Test
    void postItemRejectsBlankNameBeforeDatabaseLookup() {
        Electronics item = validItem();
        item.setName(" ");

        assertThrows(InvalidItemNameException.class, () -> ItemService.getInstance().postItem(item));
    }

    private static Electronics validItem() {
        Electronics item = new Electronics();
        item.setName("Laptop");
        item.setSellerId(1);
        return item;
    }
}
