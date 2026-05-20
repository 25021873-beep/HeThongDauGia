package org.example.service;

import org.example.dao.*;
import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.dto.BidResult;
import org.example.entity.Auction;
import org.example.entity.BidHistory;
import org.example.entity.BidTransaction;
import org.example.entity.item.Item;
import org.example.entity.user.User;
import org.example.exception.AuctionSystemException;
import org.example.exception.auction.AuctionClosedException;
import org.example.exception.auction.AuctionNotFoundException;
import org.example.exception.auth.UserNotFoundException;
import org.example.exception.bid.InvalidBidException;
import org.example.exception.database.DatabaseException;
import org.example.exception.item.InvalidItemStateException;
import org.example.exception.item.ItemNotFoundException;
import org.example.utils.ConfigManager;
import org.example.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionService {

    private AuctionDAO      auctionDAO    = new AuctionDAO();
    private BidTransactionDAO bidDAO      = new BidTransactionDAO();
    private ItemDAO         itemDAO       = new ItemDAO();
    private UserDAO         userDAO       = new UserDAO();
    private BidHistoryDAO   bidHistoryDAO = new BidHistoryDAO();
    private AuctionEngine   engine;
    private AutoBidService  autoBidService;

    private final ConcurrentHashMap<Integer, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

    // ── Anti-snipe config (đọc từ application.properties) ────────────────────
    private final int SNIPE_THRESHOLD_SECONDS;
    private final int EXTEND_SECONDS;

    private static AuctionService instance;

    private AuctionService() {
        ConfigManager cfg = ConfigManager.getInstance();
        this.SNIPE_THRESHOLD_SECONDS = cfg.getInt("auction.antisnipe.trigger_seconds", 60);
        this.EXTEND_SECONDS          = cfg.getInt("auction.antisnipe.extend_seconds",  120);
        System.out.println("[AUCTION] Anti-snipe config: trigger="
                + SNIPE_THRESHOLD_SECONDS + "s | extend=" + EXTEND_SECONDS + "s");
    }

    public static synchronized AuctionService getInstance() {
        if (instance == null) instance = new AuctionService();
        return instance;
    }

    public void setEngine(AuctionEngine engine)               { this.engine = engine; }
    public void setAutoBidService(AutoBidService s)           { this.autoBidService = s; }

    public ReentrantLock getLock(int auctionId) {
        return auctionLocks.computeIfAbsent(auctionId, id -> new ReentrantLock(true));
    }

    // ── Mở phiên đấu giá ─────────────────────────────────────────────────────

    public void openAuction(int itemId, LocalDateTime endTime) {
        Item item = itemDAO.getItemById(itemId);
        if (item == null)
            throw new ItemNotFoundException("Loi: Khong thay mon hang co ID = " + itemId);
        if (!item.getStatus().equals("AVAILABLE"))
            throw new InvalidItemStateException("Loi: San pham hien dang khong available");

        Auction newAuction = new Auction();
        newAuction.setItemId(itemId);
        newAuction.setStartTime(LocalDateTime.now());
        newAuction.setEndTime(endTime);
        newAuction.setCurrentPrice(item.getStartingPrice());
        newAuction.setStatus("RUNNING");

        if (!auctionDAO.createAuction(newAuction))
            throw new AuctionSystemException("Loi khi mo Auction");

        itemDAO.updateItemStatus(itemId, "IN_AUCTION");

        if (this.engine != null) {
            this.engine.addAuction(newAuction);
        } else {
            throw new AuctionSystemException("[AUCTION] AuctionEngine chua duoc inject!");
        }
    }

    // ── Đặt giá ───────────────────────────────────────────────────────────────

    /**
     * Đặt giá cho một phiên đấu giá.
     *
     * @return BidResult chứa kết quả thành công/thất bại và thông tin gia hạn nếu có.
     * @throws DatabaseException nếu có lỗi kỹ thuật (DB, mạng...)
     */
    public BidResult placeBid(int bidderId, int auctionId, BigDecimal bidAmount) {
        ReentrantLock lock = getLock(auctionId);
        lock.lock();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // I. KIỂM TRA ĐIỀU KIỆN ─────────────────────────────────────────

            Auction auction = auctionDAO.getAuctionById(auctionId);
            if (auction == null)
                throw new AuctionNotFoundException("Phien dau gia da ket thuc hoac khong ton tai.");
            if (auction.getEndTime().isBefore(LocalDateTime.now()))
                throw new AuctionClosedException("Het gio! Khong the dat gia them.");
            if (bidAmount.compareTo(auction.getCurrentPrice()) <= 0)
                throw new InvalidBidException("Gia dat phai cao hon gia hien tai: " + auction.getCurrentPrice());

            User bidder = userDAO.getUserById(bidderId);
            if (bidder == null)
                throw new UserNotFoundException("Khong tim thay nguoi dung ID: " + bidderId);

            BigDecimal bidderBalance = bidder.getBalance() != null ? bidder.getBalance() : BigDecimal.ZERO;
            if (bidderBalance.compareTo(bidAmount) < 0)
                throw new InvalidBidException(
                        "Vi khong du tien! Can: " + bidAmount + " | Co: " + bidderBalance);

            // II. XỬ LÝ GIAO DỊCH ───────────────────────────────────────────

            // 1. Trừ tiền người đặt mới
            userDAO.updateBalance(conn, bidderId, bidderBalance.subtract(bidAmount));

            // 2. Hoàn cọc cho người đặt cao nhất cũ
            BidTransaction highestBid = bidDAO.getHighestBid(auctionId);
            if (highestBid != null) {
                User oldBidder = userDAO.getUserById(highestBid.getBidderId());
                if (oldBidder != null) {
                    BigDecimal oldBalance = oldBidder.getBalance() != null
                            ? oldBidder.getBalance() : BigDecimal.ZERO;
                    userDAO.updateBalance(conn, oldBidder.getId(),
                            oldBalance.add(highestBid.getBidPrice()));
                }
            }

            // 3. Cập nhật giá phiên
            auctionDAO.updateCurrentPrice(conn, auctionId, bidAmount);

            // 4. Ghi lịch sử giao dịch
            BidTransaction newTx = new BidTransaction();
            newTx.setAuctionId(auctionId);
            newTx.setBidderId(bidderId);
            newTx.setBidPrice(bidAmount);
            newTx.setBidTime(LocalDateTime.now());
            bidDAO.addBid(conn, newTx);

            // 5. Ghi lịch sử visualization
            BidHistory entry = new BidHistory();
            entry.setAuctionId(auctionId);
            entry.setBidderId(bidderId);
            entry.setPrice(bidAmount);
            entry.setBidTime(LocalDateTime.now());
            bidHistoryDAO.addBidHistory(entry);

            // 6. Trigger auto-bid async
            autoBidService.triggerAsync(auctionId, bidderId);

            // III. COMMIT ────────────────────────────────────────────────────
            conn.commit();

            // IV. ANTI-SNIPE (sau commit — không ảnh hưởng giao dịch chính) ─
            long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), auction.getEndTime());
            if (secondsLeft > 0 && secondsLeft <= SNIPE_THRESHOLD_SECONDS) {

                LocalDateTime newEndTime = auction.getEndTime().plusSeconds(EXTEND_SECONDS);

                // Cập nhật DB
                auctionDAO.updateEndTime(auctionId, newEndTime);

                // Cập nhật object đang chạy trong engine (in-memory)
                if (engine != null) {
                    Auction liveAuction = engine.findActiveAuctionById(auctionId);
                    if (liveAuction != null) liveAuction.setEndTime(newEndTime);
                }

                System.out.println("[ANTI-SNIPE] Phien " + auctionId
                        + " gia han them " + EXTEND_SECONDS
                        + "s | End moi: " + newEndTime);

                return BidResult.successWithExtension(EXTEND_SECONDS, newEndTime);
            }

            return BidResult.success();

        } catch (AuctionSystemException e) {
            System.err.println("[BID] Loi nghiep vu: " + e.getMessage());
            rollback(conn);
            return BidResult.failure(e.getMessage());

        } catch (Exception e) {
            System.err.println("[BID] Loi ky thuat: " + e.getMessage());
            rollback(conn);
            throw new DatabaseException("Loi he thong trong giao dich dat gia", e);

        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); }
                catch (SQLException e) {
                    System.err.println("[BID] Loi dong connection: " + e.getMessage());
                }
            }
            lock.unlock();
        }
    }

    // ── Đóng phiên đấu giá ───────────────────────────────────────────────────

    public boolean closeAuction(int auctionId) {
        ReentrantLock lock = getLock(auctionId);
        lock.lock();
        try {
            Auction auction = auctionDAO.getAuctionById(auctionId);
            if (auction == null)
                throw new AuctionNotFoundException("Loi: Khong ton tai auction");
            if (!AuctionEngine.STATUS_ACTIVE.equals(auction.getStatus()))
                throw new AuctionClosedException("Loi: Auction hien dang khong mo");

            BidTransaction highestBid = bidDAO.getHighestBid(auctionId);
            if (highestBid != null) {
                auctionDAO.closeAuction(auctionId, highestBid.getBidderId());
                itemDAO.updateItemStatus(auction.getItemId(), "SOLD");
            } else {
                auctionDAO.updateAuctionStatus("CANCELED", auctionId);
                itemDAO.updateItemStatus(auction.getItemId(), "AVAILABLE");
            }
            return true;

        } catch (AuctionSystemException e) {
            System.err.println("[CLOSE] Loi nghiep vu: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[CLOSE] Loi ky thuat: " + e.getMessage());
            return false;
        } finally {
            lock.unlock();
            auctionLocks.remove(auctionId);
        }
    }

    // ── Lịch sử bid ──────────────────────────────────────────────────────────

    public List<BidHistory> getBidHistory(int auctionId) {
        if (auctionDAO.getAuctionById(auctionId) == null)
            throw new AuctionNotFoundException("Loi: Khong tim thay auction co ID: " + auctionId);
        return bidHistoryDAO.getHistoryByAuctionId(auctionId);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); }
            catch (SQLException ex) {
                System.err.println("[BID] Loi rollback: " + ex.getMessage());
            }
        }
    }
}