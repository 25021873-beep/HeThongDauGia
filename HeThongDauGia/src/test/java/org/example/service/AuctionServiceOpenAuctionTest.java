package org.example.service;

import org.example.dao.AuctionDAO;
import org.example.dao.BidHistoryDAO;
import org.example.dao.BidTransactionDAO;
import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.Auction;
import org.example.entity.item.Electronics;
import org.example.entity.item.Item;
import org.example.exception.AuctionSystemException;
import org.example.exception.auction.InvalidAuctionTimeException;
import org.example.exception.item.InvalidItemPriceException;
import org.example.exception.item.InvalidItemStateException;
import org.example.exception.item.ItemNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServiceOpenAuctionTest {

    private final AuctionService service = AuctionService.getInstance();
    private final AuctionEngine engine = AuctionEngine.getInstance();

    @AfterEach
    void tearDown() {
        service.resetTestDependencies();
        engine.clearForTesting();
    }

    @Test
    void openAuctionHappyCaseCreatesAuctionMarksItemAndLoadsEngine() {
        RecordingAuctionDAO auctionDAO = new RecordingAuctionDAO(44);
        RecordingItemDAO itemDAO = new RecordingItemDAO(availableItem(10, 7));
        install(auctionDAO, itemDAO);
        service.setEngine(engine);

        LocalDateTime start = LocalDateTime.now().plusMinutes(5);
        LocalDateTime end = start.plusMinutes(30);

        service.openAuction(10, 7, new BigDecimal("1000"), new BigDecimal("100"), start, end);

        assertAll(
                () -> assertNotNull(auctionDAO.createdAuction),
                () -> assertEquals(10, auctionDAO.createdAuction.getItemId()),
                () -> assertEquals(7, auctionDAO.createdAuction.getSellerId()),
                () -> assertEquals(new BigDecimal("1000"), auctionDAO.createdAuction.getCurrentPrice()),
                () -> assertEquals(new BigDecimal("100"), auctionDAO.createdAuction.getStepPrice()),
                () -> assertEquals("OPEN", auctionDAO.createdAuction.getStatus()),
                () -> assertEquals(10, itemDAO.updatedItemId),
                () -> assertEquals("IN_AUCTION", itemDAO.updatedStatus),
                () -> assertNotNull(engine.findActiveAuctionById(44))
        );
    }

    @Test
    void openAuctionRejectsMissingItem() {
        RecordingAuctionDAO auctionDAO = new RecordingAuctionDAO(44);
        install(auctionDAO, new RecordingItemDAO(null));
        service.setEngine(engine);

        assertThrows(ItemNotFoundException.class,
                () -> service.openAuction(10, 7, new BigDecimal("1000"), new BigDecimal("100"),
                        LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(30)));

        assertNull(auctionDAO.createdAuction);
    }

    @Test
    void openAuctionRejectsUnavailableItem() {
        RecordingAuctionDAO auctionDAO = new RecordingAuctionDAO(44);
        Item item = availableItem(10, 7);
        item.setStatus("SOLD");
        install(auctionDAO, new RecordingItemDAO(item));
        service.setEngine(engine);

        assertThrows(InvalidItemStateException.class,
                () -> service.openAuction(10, 7, new BigDecimal("1000"), new BigDecimal("100"),
                        LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(30)));

        assertNull(auctionDAO.createdAuction);
    }

    @Test
    void openAuctionRejectsInvalidTimeRange() {
        RecordingAuctionDAO auctionDAO = new RecordingAuctionDAO(44);
        install(auctionDAO, new RecordingItemDAO(availableItem(10, 7)));
        service.setEngine(engine);

        LocalDateTime start = LocalDateTime.now().plusMinutes(30);
        LocalDateTime end = start.minusMinutes(1);

        assertThrows(IllegalArgumentException.class,
                () -> service.openAuction(10, 7, new BigDecimal("1000"), new BigDecimal("100"), start, end));

        assertNull(auctionDAO.createdAuction);
    }

    @Test
    void openAuctionRejectsPastStartTime() {
        RecordingAuctionDAO auctionDAO = new RecordingAuctionDAO(44);
        install(auctionDAO, new RecordingItemDAO(availableItem(10, 7)));
        service.setEngine(engine);

        assertThrows(InvalidAuctionTimeException.class,
                () -> service.openAuction(10, 7, new BigDecimal("1000"), new BigDecimal("100"),
                        LocalDateTime.now().minusMinutes(3), LocalDateTime.now().plusMinutes(30)));

        assertNull(auctionDAO.createdAuction);
    }

    @Test
    void openAuctionRejectsNonPositiveStartingPrice() {
        RecordingAuctionDAO auctionDAO = new RecordingAuctionDAO(44);
        install(auctionDAO, new RecordingItemDAO(availableItem(10, 7)));
        service.setEngine(engine);

        assertThrows(InvalidItemPriceException.class,
                () -> service.openAuction(10, 7, BigDecimal.ZERO, new BigDecimal("100"),
                        LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(30)));

        assertNull(auctionDAO.createdAuction);
    }

    @Test
    void openAuctionFailsWhenEngineIsNotInjected() {
        RecordingAuctionDAO auctionDAO = new RecordingAuctionDAO(44);
        install(auctionDAO, new RecordingItemDAO(availableItem(10, 7)));
        service.setEngine(null);

        assertThrows(AuctionSystemException.class,
                () -> service.openAuction(10, 7, new BigDecimal("1000"), new BigDecimal("100"),
                        LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(30)));

        assertNotNull(auctionDAO.createdAuction);
    }

    private void install(RecordingAuctionDAO auctionDAO, RecordingItemDAO itemDAO) {
        service.resetTestDependencies();
        service.setTestDependencies(
                auctionDAO,
                new BidTransactionDAO(),
                itemDAO,
                new UserDAO(),
                new BidHistoryDAO(),
                () -> {
                    throw new AssertionError("openAuction unit tests must not open a database connection");
                });
    }

    private static Electronics availableItem(int id, int sellerId) {
        Electronics item = new Electronics();
        item.setId(id);
        item.setSellerId(sellerId);
        item.setName("Laptop");
        item.setDescription("Gaming laptop");
        item.setStatus("AVAILABLE");
        return item;
    }

    private static final class RecordingAuctionDAO extends AuctionDAO {
        private final int createdId;
        private Auction createdAuction;

        private RecordingAuctionDAO(int createdId) {
            this.createdId = createdId;
        }

        @Override
        public int createAuction(Auction auction) {
            this.createdAuction = auction;
            return createdId;
        }
    }

    private static final class RecordingItemDAO extends ItemDAO {
        private final Item item;
        private int updatedItemId = -1;
        private String updatedStatus;

        private RecordingItemDAO(Item item) {
            this.item = item;
        }

        @Override
        public Item getItemById(int id) {
            return item != null && item.getId() == id ? item : null;
        }

        @Override
        public boolean updateItemStatus(int itemId, String newStatus) {
            this.updatedItemId = itemId;
            this.updatedStatus = newStatus;
            if (item != null) {
                item.setStatus(newStatus);
            }
            return true;
        }
    }
}
