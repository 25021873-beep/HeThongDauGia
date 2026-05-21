package org.example.entity.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtTest {

    @Test
    void constructorSetsTypeAndAuthorCanBeChanged() {
        Art art = new Art();

        art.setAuthor("Van Gogh");

        assertEquals("ART", art.getItemType());
        assertEquals("Van Gogh", art.getAuthor());
    }
}
