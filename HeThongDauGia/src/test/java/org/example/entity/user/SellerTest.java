package org.example.entity.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SellerTest {

    @Test
    void constructorSetsRoleAndRatingCanBeChanged() {
        Seller seller = new Seller();

        seller.setRating(4.5);

        assertEquals("SELLER", seller.getRole());
        assertEquals(4.5, seller.getRating());
        assertDoesNotThrow(seller::doSomething);
    }
}
