package org.example.service;

import org.example.dao.AuctionDAO;
import org.example.dao.AutoBidDAO;
import org.example.dao.BidHistoryDAO;
import org.example.dao.BidTransactionDAO;
import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.dto.BidResult;
import org.example.entity.Auction;
import org.example.entity.BidHistory;
import org.example.entity.BidTransaction;
import org.example.entity.user.Bidder;
import org.example.entity.user.Seller;
import org.example.entity.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServicePlaceBidTest {

    private final AuctionService service = AuctionService.getInstance();

    @AfterEach
    void tearDown() {
        service.resetTestDependencies();
    }

    @Test
    void placeBidHappyCasePersistsTransactionAndCommits() {
        TestFixture fixture = installFixture(runningAuction(), null, bidder(2, new BigDecimal("5000")));

        BidResult result = service.placeBid(2, 1, new BigDecimal("1200"));

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertFalse(result.isExtended()),
                () -> assertEquals(1, fixture.connection.commitCount),
                () -> assertEquals(0, fixture.connection.rollbackCount),
                () -> assertTrue(fixture.connection.closed),
                () -> assertEquals(1, fixture.auctionDAO.updatedAuctionId),
                () -> assertEquals(new BigDecimal("1200"), fixture.auctionDAO.updatedPrice),
                () -> assertEquals(2, fixture.userDAO.deductedUserId),
                () -> assertEquals(new BigDecimal("1200"), fixture.userDAO.deductedAmount),
                () -> assertEquals(-1, fixture.userDAO.refundedUserId),
                () -> assertNotNull(fixture.bidDAO.addedBid),
                () -> assertEquals(1, fixture.bidDAO.addedBid.getAuctionId()),
                () -> assertEquals(2, fixture.bidDAO.addedBid.getBidderId()),
                () -> assertEquals(new BigDecimal("1200"), fixture.bidDAO.addedBid.getBidPrice()),
                () -> assertNotNull(fixture.bidHistoryDAO.savedEntry),
                () -> assertEquals(new BigDecimal("1200"), fixture.bidHistoryDAO.savedEntry.getPrice()),
                () -> assertEquals(1, fixture.autoBidService.triggeredAuctionId),
                () -> assertEquals(2, fixture.autoBidService.triggeredBidderId)
        );
    }

    @Test
    void placeBidRefundsPreviousHighestBidderBeforeCommitting() {
        BidTransaction previousHighest = previousBid(3, new BigDecimal("1100"));
        TestFixture fixture = installFixture(
                runningAuction(),
                previousHighest,
                bidder(2, new BigDecimal("5000")),
                bidder(3, new BigDecimal("1000")));

        BidResult result = service.placeBid(2, 1, new BigDecimal("1300"));

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(1, fixture.connection.commitCount),
                () -> assertEquals(3, fixture.userDAO.refundedUserId),
                () -> assertEquals(new BigDecimal("1100"), fixture.userDAO.refundedAmount),
                () -> assertEquals(new BigDecimal("2100"), fixture.userDAO.users.get(3).getBalance())
        );
    }

    @Test
    void placeBidNearEndTimeExtendsAuctionForAntiSnipe() {
        Auction auction = runningAuction();
        LocalDateTime originalEndTime = LocalDateTime.now().plusSeconds(30);
        auction.setEndTime(originalEndTime);
        TestFixture fixture = installFixture(auction, null, bidder(2, new BigDecimal("5000")));

        BidResult result = service.placeBid(2, 1, new BigDecimal("1200"));

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertTrue(result.isExtended()),
                () -> assertEquals(120, result.getExtendedSeconds()),
                () -> assertEquals(originalEndTime.plusSeconds(120), result.getNewEndTime()),
                () -> assertEquals(originalEndTime.plusSeconds(120), fixture.auctionDAO.updatedEndTime),
                () -> assertEquals(1, fixture.connection.commitCount)
        );
    }

    @Test
    void placeBidRejectsBidBelowStepPriceAndRollsBack() {
        TestFixture fixture = installFixture(runningAuction(), null, bidder(2, new BigDecimal("5000")));

        BidResult result = service.placeBid(2, 1, new BigDecimal("1050"));

        assertAll(
                () -> assertFalse(result.isSuccess()),
                () -> assertEquals(0, fixture.connection.commitCount),
                () -> assertEquals(1, fixture.connection.rollbackCount),
                () -> assertEquals(-1, fixture.userDAO.deductedUserId),
                () -> assertNull(fixture.bidDAO.addedBid)
        );
    }

    @Test
    void placeBidRejectsSellerSelfBid() {
        TestFixture fixture = installFixture(runningAuction(), null, bidder(10, new BigDecimal("5000")));

        BidResult result = service.placeBid(10, 1, new BigDecimal("1200"));

        assertAll(
                () -> assertFalse(result.isSuccess()),
                () -> assertEquals(1, fixture.connection.rollbackCount),
                () -> assertEquals(-1, fixture.userDAO.deductedUserId)
        );
    }

    @Test
    void placeBidRejectsNonBidderUser() {
        Seller seller = new Seller();
        seller.setId(4);
        seller.setUsername("seller");
        TestFixture fixture = installFixture(runningAuction(), null, seller);

        BidResult result = service.placeBid(4, 1, new BigDecimal("1200"));

        assertAll(
                () -> assertFalse(result.isSuccess()),
                () -> assertEquals(1, fixture.connection.rollbackCount),
                () -> assertEquals(-1, fixture.userDAO.deductedUserId)
        );
    }

    @Test
    void placeBidRejectsClosedAuction() {
        Auction auction = runningAuction();
        auction.setStatus("FINISHED");
        TestFixture fixture = installFixture(auction, null, bidder(2, new BigDecimal("5000")));

        BidResult result = service.placeBid(2, 1, new BigDecimal("1200"));

        assertAll(
                () -> assertFalse(result.isSuccess()),
                () -> assertEquals(1, fixture.connection.rollbackCount),
                () -> assertEquals(-1, fixture.userDAO.deductedUserId)
        );
    }

    @Test
    void placeBidReturnsFailureWhenBalanceDeductionFails() {
        TestFixture fixture = installFixture(runningAuction(), null, bidder(2, new BigDecimal("5000")));
        fixture.userDAO.deductResult = false;

        BidResult result = service.placeBid(2, 1, new BigDecimal("1200"));

        assertAll(
                () -> assertFalse(result.isSuccess()),
                () -> assertEquals(0, fixture.connection.commitCount),
                () -> assertEquals(1, fixture.connection.rollbackCount),
                () -> assertNull(fixture.bidDAO.addedBid),
                () -> assertNull(fixture.bidHistoryDAO.savedEntry)
        );
    }

    private TestFixture installFixture(Auction auction, BidTransaction previousHighest, User... users) {
        service.resetTestDependencies();
        TestFixture fixture = new TestFixture();
        fixture.connection = RecordingConnection.create();
        fixture.auctionDAO = new RecordingAuctionDAO(auction);
        fixture.bidDAO = new RecordingBidTransactionDAO(previousHighest);
        fixture.userDAO = new RecordingUserDAO(users);
        fixture.bidHistoryDAO = new RecordingBidHistoryDAO();
        fixture.autoBidService = new RecordingAutoBidService();

        service.setTestDependencies(
                fixture.auctionDAO,
                fixture.bidDAO,
                new ItemDAO(),
                fixture.userDAO,
                fixture.bidHistoryDAO,
                fixture.connection::connection);
        service.setAutoBidService(fixture.autoBidService);
        service.setEngine(null);

        return fixture;
    }

    private static Auction runningAuction() {
        Auction auction = new Auction();
        auction.setId(1);
        auction.setItemId(100);
        auction.setSellerId(10);
        auction.setStatus("RUNNING");
        auction.setCurrentPrice(new BigDecimal("1000"));
        auction.setStartingPrice(new BigDecimal("1000"));
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

    private static BidTransaction previousBid(int bidderId, BigDecimal price) {
        BidTransaction bid = new BidTransaction();
        bid.setAuctionId(1);
        bid.setBidderId(bidderId);
        bid.setBidPrice(price);
        bid.setBidTime(LocalDateTime.now().minusMinutes(1));
        return bid;
    }

    private static final class TestFixture {
        private RecordingConnection connection;
        private RecordingAuctionDAO auctionDAO;
        private RecordingBidTransactionDAO bidDAO;
        private RecordingUserDAO userDAO;
        private RecordingBidHistoryDAO bidHistoryDAO;
        private RecordingAutoBidService autoBidService;
    }

    private static final class RecordingConnection {
        private Connection connection;
        private int commitCount;
        private int rollbackCount;
        private boolean closed;

        private static RecordingConnection create() {
            RecordingConnection recording = new RecordingConnection();
            recording.connection = (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "setAutoCommit":
                                return null;
                            case "commit":
                                recording.commitCount++;
                                return null;
                            case "rollback":
                                recording.rollbackCount++;
                                return null;
                            case "close":
                                recording.closed = true;
                                return null;
                            case "isClosed":
                                return recording.closed;
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
        private int updatedAuctionId = -1;
        private BigDecimal updatedPrice;
        private LocalDateTime updatedEndTime;

        private RecordingAuctionDAO(Auction auction) {
            this.auction = auction;
        }

        @Override
        public Auction getAuctionById(int id) {
            return auction != null && auction.getId() == id ? auction : null;
        }

        @Override
        public boolean updateCurrentPrice(Connection conn, int id, BigDecimal newPrice) {
            this.updatedAuctionId = id;
            this.updatedPrice = newPrice;
            if (auction != null) {
                auction.setCurrentPrice(newPrice);
            }
            return true;
        }

        @Override
        public void updateEndTime(Connection conn, int auctionId, LocalDateTime newEndTime) {
            this.updatedEndTime = newEndTime;
            if (auction != null) {
                auction.setEndTime(newEndTime);
            }
        }
    }

    private static final class RecordingBidTransactionDAO extends BidTransactionDAO {
        private final BidTransaction highestBid;
        private BidTransaction addedBid;

        private RecordingBidTransactionDAO(BidTransaction highestBid) {
            this.highestBid = highestBid;
        }

        @Override
        public BidTransaction getHighestBid(Connection conn, int auctionId) {
            return highestBid;
        }

        @Override
        public boolean addBid(Connection conn, BidTransaction bid) {
            this.addedBid = bid;
            return true;
        }
    }

    private static final class RecordingUserDAO extends UserDAO {
        private final Map<Integer, User> users = new HashMap<>();
        private boolean deductResult = true;
        private int deductedUserId = -1;
        private BigDecimal deductedAmount;
        private int refundedUserId = -1;
        private BigDecimal refundedAmount;

        private RecordingUserDAO(User... users) {
            for (User user : users) {
                this.users.put(user.getId(), user);
            }
        }

        @Override
        public User getUserById(int id) {
            return users.get(id);
        }

        @Override
        public boolean deductBalance(Connection conn, int userId, BigDecimal amount) {
            this.deductedUserId = userId;
            this.deductedAmount = amount;
            if (!deductResult) {
                return false;
            }
            User user = users.get(userId);
            if (user instanceof Bidder) {
                Bidder bidder = (Bidder) user;
                bidder.setBalance(bidder.getBalance().subtract(amount));
            }
            return true;
        }

        @Override
        public boolean addBalance(Connection conn, int userId, BigDecimal amount) {
            this.refundedUserId = userId;
            this.refundedAmount = amount;
            User user = users.get(userId);
            if (user instanceof Bidder) {
                Bidder bidder = (Bidder) user;
                bidder.setBalance(bidder.getBalance().add(amount));
            }
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

    private static final class RecordingAutoBidService extends AutoBidService {
        private int triggeredAuctionId = -1;
        private int triggeredBidderId = -1;

        private RecordingAutoBidService() {
            super(new AutoBidDAO(), AuctionService.getInstance(), new AuctionDAO(), new UserDAO());
        }

        @Override
        public void triggerAsync(int auctionId, int triggerBidderId) {
            this.triggeredAuctionId = auctionId;
            this.triggeredBidderId = triggerBidderId;
        }
    }
}
