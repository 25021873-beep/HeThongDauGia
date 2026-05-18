package org.example.dao.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemDAOTest {

    @Test
    void canBeConstructedWithoutOpeningDatabaseConnection() {
        assertDoesNotThrow(ItemDAO::new);
    }
}
