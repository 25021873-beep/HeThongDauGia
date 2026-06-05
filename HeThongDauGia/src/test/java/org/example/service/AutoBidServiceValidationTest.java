package org.example.service;

import org.example.dao.AuctionDAO;
import org.example.dao.AutoBidDAO;
import org.example.dao.BidTransactionDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.Auction;
import org.example.entity.AutoBidConfig;
import org.example.entity.BidTransaction;
import org.example.entity.user.Bidder;
import org.example.entity.user.User;
import org.example.exception.auction.AuctionClosedException;
import org.example.exception.bid.InsufficientBalanceException;
import org.example.exception.bid.InvalidBidException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AutoBidServiceValidationTest {

    @Test
    void registerAutoBidHappyCaseSavesConfig() {
        RecordingAutoBidDAO autoBidDAO = new RecordingAutoBidDAO();
        AutoBidService service = newService(
                runningAuction(new BigDecimal("1000")),
                bidder(2, new BigDecimal("5000")),
                autoBidDAO);

        service.registerAutoBid(2, 1, new BigDecimal("3000"), new BigDecimal("200"));

        assertNotNull(autoBidDAO.savedConfig);
        assertEquals(1, autoBidDAO.savedConfig.getAuctionId());
        assertEquals(2, autoBidDAO.savedConfig.getBidderId());
        assertEquals(new BigDecimal("3000"), autoBidDAO.savedConfig.getMaxBid());
        assertEquals(new BigDecimal("200"), autoBidDAO.savedConfig.getIncrement());
    }

    @Test
    void registerAutoBidRejectsClosedAuction() {
        Auction closed = runningAuction(new BigDecimal("1000"));
        closed.setStatus("FINISHED");
        AutoBidService service = newService(closed, bidder(2, new BigDecimal("5000")), new RecordingAutoBidDAO());

        assertThrows(AuctionClosedException.class,
                () -> service.registerAutoBid(2, 1, new BigDecimal("3000"), new BigDecimal("200")));
    }

    @Test
    void registerAutoBidRejectsMaxBidNotGreaterThanCurrentPrice() {
        AutoBidService service = newService(
                runningAuction(new BigDecimal("1000")),
                bidder(2, new BigDecimal("5000")),
                new RecordingAutoBidDAO());

        assertThrows(InvalidBidException.class,
                () -> service.registerAutoBid(2, 1, new BigDecimal("1000"), new BigDecimal("200")));
    }

    @Test
    void registerAutoBidRejectsNonPositiveIncrement() {
        AutoBidService service = newService(
                runningAuction(new BigDecimal("1000")),
                bidder(2, new BigDecimal("5000")),
                new RecordingAutoBidDAO());

        assertThrows(InvalidBidException.class,
                () -> service.registerAutoBid(2, 1, new BigDecimal("3000"), BigDecimal.ZERO));
    }

    @Test
    void registerAutoBidRejectsInsufficientBalance() {
        AutoBidService service = newService(
                runningAuction(new BigDecimal("1000")),
                bidder(2, new BigDecimal("1500")),
                new RecordingAutoBidDAO());

        assertThrows(InsufficientBalanceException.class,
                () -> service.registerAutoBid(2, 1, new BigDecimal("3000"), new BigDecimal("200")));
    }

    @Test
    void triggerAutoBidDeactivatesConfigWhenNextPriceExceedsMaxBid() {
        RecordingAutoBidDAO autoBidDAO = new RecordingAutoBidDAO();
        autoBidDAO.activeConfigs.add(config(1, 2, new BigDecimal("1050"), new BigDecimal("100"), 1));
        AutoBidService service = newService(
                runningAuction(new BigDecimal("1000")),
                bidder(2, new BigDecimal("5000")),
                autoBidDAO);

        service.triggerAutoBid(1, 99);

        assertEquals(List.of("1:2"), autoBidDAO.deactivatedKeys);
    }

    @Test
    void triggerAutoBidDeactivatesConfigWhenBidderBalanceIsTooLow() {
        RecordingAutoBidDAO autoBidDAO = new RecordingAutoBidDAO();
        autoBidDAO.activeConfigs.add(config(1, 2, new BigDecimal("5000"), new BigDecimal("100"), 1));
        AutoBidService service = newService(
                runningAuction(new BigDecimal("1000")),
                bidder(2, new BigDecimal("500")),
                autoBidDAO);

        service.triggerAutoBid(1, 99);

        assertEquals(List.of("1:2"), autoBidDAO.deactivatedKeys);
    }

    @Test
    void triggerAutoBidSkipsTheBidderWhoTriggeredCurrentBid() {
        RecordingAutoBidDAO autoBidDAO = new RecordingAutoBidDAO();
        autoBidDAO.activeConfigs.add(config(1, 2, new BigDecimal("5000"), new BigDecimal("100"), 1));
        AutoBidService service = newService(
                runningAuction(new BigDecimal("1000")),
                bidder(2, new BigDecimal("5000")),
                autoBidDAO);

        service.triggerAutoBid(1, 2);

        assertTrue(autoBidDAO.deactivatedKeys.isEmpty(),
                "Nguoi vua bid khong duoc auto-bid lai chinh minh");
    }

    private static AutoBidService newService(Auction auction, User bidder, RecordingAutoBidDAO autoBidDAO) {
        StubUserDAO userDAO = new StubUserDAO();
        userDAO.put(bidder);
        return new AutoBidService(autoBidDAO, AuctionService.getInstance(),
                new StubAuctionDAO(auction), userDAO, new StubBidTransactionDAO(null));
    }

    private static Auction runningAuction(BigDecimal currentPrice) {
        Auction auction = new Auction();
        auction.setId(1);
        auction.setStatus("RUNNING");
        auction.setCurrentPrice(currentPrice);
        auction.setStepPrice(new BigDecimal("100"));
        auction.setEndTime(LocalDateTime.now().plusMinutes(10));
        return auction;
    }

    private static Bidder bidder(int id, BigDecimal balance) {
        Bidder bidder = new Bidder();
        bidder.setId(id);
        bidder.setUsername("bidder" + id);
        bidder.setBalance(balance);
        return bidder;
    }

    private static AutoBidConfig config(int auctionId, int bidderId, BigDecimal maxBid,
                                        BigDecimal increment, int createdOrder) {
        AutoBidConfig config = new AutoBidConfig();
        config.setAuctionId(auctionId);
        config.setBidderId(bidderId);
        config.setMaxBid(maxBid);
        config.setIncrement(increment);
        config.setCreatedAt(LocalDateTime.of(2026, 6, 4, 10, createdOrder));
        config.setActive(true);
        return config;
    }

    private static final class StubAuctionDAO extends AuctionDAO {
        private final Auction auction;

        private StubAuctionDAO(Auction auction) {
            this.auction = auction;
        }

        @Override
        public Auction getAuctionById(int id) {
            return auction != null && auction.getId() == id ? auction : null;
        }
    }

    private static final class StubUserDAO extends UserDAO {
        private final Map<Integer, User> users = new HashMap<>();

        void put(User user) {
            users.put(user.getId(), user);
        }

        @Override
        public User getUserById(int id) {
            return users.get(id);
        }
    }

    private static final class StubBidTransactionDAO extends BidTransactionDAO {
        private final BidTransaction highestBid;

        private StubBidTransactionDAO(BidTransaction highestBid) {
            this.highestBid = highestBid;
        }

        @Override
        public BidTransaction getHighestBid(int auctionId) {
            return highestBid;
        }
    }

    private static final class RecordingAutoBidDAO extends AutoBidDAO {
        private AutoBidConfig savedConfig;
        private final List<AutoBidConfig> activeConfigs = new ArrayList<>();
        private final List<String> deactivatedKeys = new ArrayList<>();

        @Override
        public void saveOrUpdate(AutoBidConfig config) {
            this.savedConfig = config;
        }

        @Override
        public List<AutoBidConfig> getActiveAutoBids(long auctionId) {
            return activeConfigs;
        }

        @Override
        public void deactivate(long auctionId, long bidderId) {
            deactivatedKeys.add(auctionId + ":" + bidderId);
        }
    }
}
