package org.example.service;

import org.example.dao.AuctionDAO;
import org.example.dao.AutoBidDAO;
import org.example.dao.BidHistoryDAO;
import org.example.dao.BidTransactionDAO;
import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.Auction;
import org.example.entity.AutoBidConfig;
import org.example.entity.BidHistory;
import org.example.entity.BidTransaction;
import org.example.entity.user.Bidder;
import org.example.entity.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AutoBidServiceExecutionTest {

    private final AuctionService auctionService = AuctionService.getInstance();

    @AfterEach
    void tearDown() {
        auctionService.resetTestDependencies();
    }

    @Test
    void triggerAutoBidPlacesNextValidBidThroughAuctionService() {
        Auction auction = runningAuction();
        RecordingConnection connection = RecordingConnection.create();
        RecordingAuctionDAO auctionDAO = new RecordingAuctionDAO(auction);
        RecordingUserDAO userDAO = new RecordingUserDAO(
                Map.of(2, bidder(2, new BigDecimal("5000"))));
        RecordingBidTransactionDAO bidDAO = new RecordingBidTransactionDAO();
        RecordingBidHistoryDAO bidHistoryDAO = new RecordingBidHistoryDAO();

        auctionService.resetTestDependencies();
        auctionService.setTestDependencies(
                auctionDAO,
                bidDAO,
                new ItemDAO(),
                userDAO,
                bidHistoryDAO,
                connection::connection);

        RecordingAutoBidDAO autoBidDAO = new RecordingAutoBidDAO();
        autoBidDAO.activeConfigs.add(config(1, 2, new BigDecimal("5000"), new BigDecimal("100"), 1));
        AutoBidService autoBidService = new AutoBidService(
                autoBidDAO, auctionService, auctionDAO, userDAO, new BidTransactionDAO());

        autoBidService.triggerAutoBid(1, 99);

        assertAll(
                () -> assertNotNull(bidDAO.addedBid),
                () -> assertEquals(2, bidDAO.addedBid.getBidderId()),
                () -> assertEquals(new BigDecimal("1100"), bidDAO.addedBid.getBidPrice()),
                () -> assertEquals(new BigDecimal("1100"), auctionDAO.updatedPrice),
                () -> assertNotNull(bidHistoryDAO.savedEntry),
                () -> assertEquals(new BigDecimal("1100"), bidHistoryDAO.savedEntry.getPrice()),
                () -> assertEquals(1, connection.commitCount),
                () -> assertTrue(autoBidDAO.deactivatedKeys.isEmpty())
        );
    }

    private static Auction runningAuction() {
        Auction auction = new Auction();
        auction.setId(1);
        auction.setSellerId(10);
        auction.setStatus("RUNNING");
        auction.setCurrentPrice(new BigDecimal("1000"));
        auction.setStepPrice(new BigDecimal("100"));
        auction.setStartTime(LocalDateTime.now().minusMinutes(5));
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

    private static final class RecordingConnection {
        private Connection connection;
        private int commitCount;

        private static RecordingConnection create() {
            RecordingConnection recording = new RecordingConnection();
            recording.connection = (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "setAutoCommit":
                            case "close":
                            case "rollback":
                                return null;
                            case "commit":
                                recording.commitCount++;
                                return null;
                            case "isClosed":
                                return false;
                            default:
                                throw new SQLException("Unexpected JDBC call in unit test: " + method.getName());
                        }
                    });
            return recording;
        }

        private Connection connection() {
            return connection;
        }
    }

    private static final class RecordingAuctionDAO extends AuctionDAO {
        private final Auction auction;
        private BigDecimal updatedPrice;

        private RecordingAuctionDAO(Auction auction) {
            this.auction = auction;
        }

        @Override
        public Auction getAuctionById(int id) {
            return auction != null && auction.getId() == id ? auction : null;
        }

        @Override
        public boolean updateCurrentPrice(Connection conn, int id, BigDecimal newPrice) {
            this.updatedPrice = newPrice;
            auction.setCurrentPrice(newPrice);
            return true;
        }
    }

    private static final class RecordingUserDAO extends UserDAO {
        private final Map<Integer, User> users;

        private RecordingUserDAO(Map<Integer, User> users) {
            this.users = users;
        }

        @Override
        public User getUserById(int id) {
            return users.get(id);
        }

        @Override
        public boolean deductBalance(Connection conn, int userId, BigDecimal amount) {
            return true;
        }
    }

    private static final class RecordingBidTransactionDAO extends BidTransactionDAO {
        private BidTransaction addedBid;

        @Override
        public BidTransaction getHighestBid(Connection conn, int auctionId) {
            return null;
        }

        @Override
        public boolean addBid(Connection conn, BidTransaction bid) {
            this.addedBid = bid;
            return true;
        }
    }

    private static final class RecordingBidHistoryDAO extends BidHistoryDAO {
        private BidHistory savedEntry;

        @Override
        public void addBidHistory(Connection conn, BidHistory entry) {
            this.savedEntry = entry;
        }
    }

    private static final class RecordingAutoBidDAO extends AutoBidDAO {
        private final List<AutoBidConfig> activeConfigs = new ArrayList<>();
        private final List<String> deactivatedKeys = new ArrayList<>();

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
