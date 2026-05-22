package org.example;

import org.example.entity.Auction;
import org.example.entity.item.Electronics;
import org.example.service.AuctionEngine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionEngineTest {

    @Test
    void addAuctionAcceptsRunningAuctionAndFindsById() {
        AuctionEngine engine = AuctionEngine.getInstance();
        Auction auction = new Auction(3, BigDecimal.TEN, LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(5));
        auction.setId(7);
        auction.setStatus(AuctionEngine.STATUS_ACTIVE);
        auction.setItem(testItem());

        engine.addAuction(auction);

        assertSame(auction, engine.findActiveAuctionById(7));
    }

    @Test
    void addAuctionRejectsNullAndNonRunningAuction() {
        AuctionEngine engine = AuctionEngine.getInstance();
        Auction auction = new Auction();
        auction.setStatus("FINISHED");

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> engine.addAuction(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> engine.addAuction(auction))
        );
    }

    @Test
    void getActiveAuctionsIsReadOnly() {
        AuctionEngine engine = AuctionEngine.getInstance();

        assertThrows(UnsupportedOperationException.class,
                () -> engine.getActiveAuctions().add(new Auction()));
    }

    private static Electronics testItem() {
        Electronics item = new Electronics();
        item.setName("Laptop");
        return item;
    }
}
