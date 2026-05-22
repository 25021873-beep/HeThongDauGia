package org.example.entity.user;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BidderTest {

    @Test
    void constructorSetsRoleAndBalanceCanBeChanged() {
        Bidder bidder = new Bidder();

        bidder.setBalance(new BigDecimal("900"));

        assertEquals("BIDDER", bidder.getRole());
        assertEquals(new BigDecimal("900"), bidder.getBalance());
        assertDoesNotThrow(bidder::doSomething);
    }
}
