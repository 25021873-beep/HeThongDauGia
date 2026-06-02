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

        // Bơm đủ 9 tham số, đút luôn 7, STATUS_OPEN và testItem() vào đây
        Auction auction = new Auction(
                7,                                  // id
                3,                                  // itemId
                BigDecimal.TEN,                     // currentPrice
                BigDecimal.TEN,                     // startingPrice
                BigDecimal.ONE,                     // stepPrice
                LocalDateTime.now(),                // startTime
                LocalDateTime.now().plusMinutes(5), // endTime
                AuctionEngine.STATUS_OPEN,          // status
                1,                                  // sellerId
                0,                                  // winnerId
                testItem()                          // item
        );

        // Mấy dòng auction.setId(), setStatus(), setItem() cũ xóa hết đi vì đã truyền ở trên rồi

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
