package org.example.service;

import org.example.dao.AuctionDAO;
import org.example.dao.BidHistoryDAO;
import org.example.dao.BidTransactionDAO;
import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.Auction;
import org.example.entity.BidTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServiceCloseAuctionTest {

    private final AuctionService service = AuctionService.getInstance();

    @AfterEach
    void restoreRealDaos() throws Exception {
        setField("auctionDAO", new AuctionDAO());
        setField("bidDAO", new BidTransactionDAO());
        setField("itemDAO", new ItemDAO());
        setField("userDAO", new UserDAO());
        setField("bidHistoryDAO", new BidHistoryDAO());
    }

    @Test
    void closeAuctionWithHighestBidMarksAuctionFinishedAndItemSold() throws Exception {
        RecordingAuctionDAO auctionDAO = new RecordingAuctionDAO(runningAuction());
        RecordingBidTransactionDAO bidDAO = new RecordingBidTransactionDAO(highestBid(77));
        RecordingItemDAO itemDAO = new RecordingItemDAO();
        setField("auctionDAO", auctionDAO);
        setField("bidDAO", bidDAO);
        setField("itemDAO", itemDAO);

        assertTrue(service.closeAuction(1));

        assertEquals(1, auctionDAO.closedAuctionId);
        assertEquals(77, auctionDAO.winnerId);
        assertEquals(10, itemDAO.updatedItemId);
        assertEquals("SOLD", itemDAO.updatedStatus);
    }

    @Test
    void closeAuctionWithoutBidCancelsAuctionAndMakesItemAvailableAgain() throws Exception {
        RecordingAuctionDAO auctionDAO = new RecordingAuctionDAO(runningAuction());
        RecordingBidTransactionDAO bidDAO = new RecordingBidTransactionDAO(null);
        RecordingItemDAO itemDAO = new RecordingItemDAO();
        setField("auctionDAO", auctionDAO);
        setField("bidDAO", bidDAO);
        setField("itemDAO", itemDAO);

        assertTrue(service.closeAuction(1));

        assertEquals("CANCELED", auctionDAO.updatedStatus);
        assertEquals(1, auctionDAO.updatedAuctionId);
        assertEquals(10, itemDAO.updatedItemId);
        assertEquals("AVAILABLE", itemDAO.updatedStatus);
    }

    @Test
    void closeAuctionReturnsFalseWhenAuctionAlreadyClosed() throws Exception {
        Auction finished = runningAuction();
        finished.setStatus("FINISHED");
        setField("auctionDAO", new RecordingAuctionDAO(finished));
        setField("bidDAO", new RecordingBidTransactionDAO(null));
        setField("itemDAO", new RecordingItemDAO());

        assertFalse(service.closeAuction(1));
    }

    private void setField(String name, Object value) throws Exception {
        Field field = AuctionService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }

    private static Auction runningAuction() {
        Auction auction = new Auction();
        auction.setId(1);
        auction.setItemId(10);
        auction.setSellerId(5);
        auction.setStatus("RUNNING");
        auction.setCurrentPrice(new BigDecimal("1000"));
        auction.setStepPrice(new BigDecimal("100"));
        auction.setEndTime(LocalDateTime.now().plusMinutes(5));
        return auction;
    }

    private static BidTransaction highestBid(int bidderId) {
        BidTransaction bid = new BidTransaction();
        bid.setAuctionId(1);
        bid.setBidderId(bidderId);
        bid.setBidPrice(new BigDecimal("1500"));
        bid.setBidTime(LocalDateTime.now());
        return bid;
    }

    private static final class RecordingAuctionDAO extends AuctionDAO {
        private final Auction auction;
        private int closedAuctionId = -1;
        private int winnerId = -1;
        private String updatedStatus;
        private int updatedAuctionId = -1;

        private RecordingAuctionDAO(Auction auction) {
            this.auction = auction;
        }

        @Override
        public Auction getAuctionById(int id) {
            return auction != null && auction.getId() == id ? auction : null;
        }

        @Override
        public boolean closeAuction(int auctionId, int winnerId) {
            this.closedAuctionId = auctionId;
            this.winnerId = winnerId;
            return true;
        }

        @Override
        public boolean updateAuctionStatus(String status, int auctionId) {
            this.updatedStatus = status;
            this.updatedAuctionId = auctionId;
            return true;
        }
    }

    private static final class RecordingBidTransactionDAO extends BidTransactionDAO {
        private final BidTransaction highestBid;

        private RecordingBidTransactionDAO(BidTransaction highestBid) {
            this.highestBid = highestBid;
        }

        @Override
        public BidTransaction getHighestBid(int auctionId) {
            return highestBid;
        }
    }

    private static final class RecordingItemDAO extends ItemDAO {
        private int updatedItemId = -1;
        private String updatedStatus;

        @Override
        public boolean updateItemStatus(int itemId, String newStatus) {
            this.updatedItemId = itemId;
            this.updatedStatus = newStatus;
            return true;
        }
    }
}
