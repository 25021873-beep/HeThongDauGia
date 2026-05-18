package org.example.service;

import org.example.dao.*;
import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.Auction;
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
import org.example.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AuctionService {

    private final AuctionDAO        auctionDAO = new AuctionDAO();
    private final BidTransactionDAO bidDAO     = new BidTransactionDAO();
    private final ItemDAO           itemDAO    = new ItemDAO();
    private final UserDAO           userDAO    = new UserDAO();

    private AuctionEngine engine;

    private static AuctionService instance;

    private AuctionService() {}

    public static synchronized AuctionService getInstance() {
        if (instance == null) instance = new AuctionService();
        return instance;
    }

    public void setEngine(AuctionEngine engine) {
        this.engine = engine;
    }

    // ── Mở phiên đấu giá ─────────────────────────────────────────────────────

    public void openAuction(int itemId, LocalDateTime endTime) {
        Item item = itemDAO.getItemById(itemId);
        if (item == null) {
            throw new ItemNotFoundException("Lỗi: Không thấy món hàng có ID = " + itemId);
        }
        if (!item.getStatus().equals("AVAILABLE")) {
            throw new InvalidItemStateException("Lỗi: Sản phẩm hiện đang không available");
        }

        Auction newAuction = new Auction();
        newAuction.setItemId(itemId);
        newAuction.setStartTime(LocalDateTime.now());
        newAuction.setEndTime(endTime);
        newAuction.setCurrentPrice(item.getStartingPrice());

        // FIX Bug 3: Dùng AuctionEngine.STATUS_ACTIVE ("ACTIVE") thay vì "RUNNING"
        // để khớp với addAuction() — sai status → IllegalArgumentException
        newAuction.setStatus(AuctionEngine.STATUS_ACTIVE);

        if (!auctionDAO.createAuction(newAuction)) {
            throw new AuctionSystemException("Lỗi khi mở Auction");
        }
        itemDAO.updateItemStatus(itemId, "IN_AUCTION");

        if (this.engine != null) {
            this.engine.addAuction(newAuction);
        } else {
            throw new AuctionSystemException("[AUCTION] AuctionEngine chua duoc inject!");
        }
    }

    // ── Đặt giá ───────────────────────────────────────────────────────────────

    /**
     * FIX Bug 1: Đổi return type từ void → boolean.
     *   - true  : đặt giá thành công
     *   - false : lỗi nghiệp vụ (ví dụ giá thấp, hết tiền)
     *
     * BidController gọi: boolean success = auctionService.placeBid(...)
     * Nếu vẫn để void → compile error.
     *
     * Exception kỹ thuật (DatabaseException) vẫn được ném ra để
     * BidController có thể log và gửi lỗi hệ thống về client.
     */
    public boolean placeBid(int bidderId, int auctionId, BigDecimal bidAmount) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // I. KIỂM TRA ĐIỀU KIỆN ─────────────────────────────────────────

            Auction auction = auctionDAO.getAuctionById(auctionId);
            if (auction == null) {
                throw new AuctionNotFoundException("Phien dau gia da ket thuc hoac khong ton tai.");
            }
            if (auction.getEndTime().isBefore(LocalDateTime.now())) {
                throw new AuctionClosedException("Het gio! Khong the dat gia them.");
            }
            if (bidAmount.compareTo(auction.getCurrentPrice()) <= 0) {
                throw new InvalidBidException(
                        "Gia dat phai cao hon gia hien tai: " + auction.getCurrentPrice());
            }

            User bidder = userDAO.getUserById(bidderId);
            if (bidder == null) {
                throw new UserNotFoundException("Khong tim thay nguoi dung ID: " + bidderId);
            }

            BigDecimal bidderBalance = bidder.getBalance() != null
                    ? bidder.getBalance() : BigDecimal.ZERO;
            if (bidderBalance.compareTo(bidAmount) < 0) {
                throw new InvalidBidException(
                        "Vi khong du tien! Can: " + bidAmount + " | Co: " + bidderBalance);
            }

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

            conn.commit();
            return true; // ← thành công

        } catch (AuctionSystemException e) {
            // Lỗi nghiệp vụ (giá thấp, hết giờ, hết tiền...) → trả false, không crash
            System.err.println("[BID] Loi nghiep vu: " + e.getMessage());
            rollback(conn);
            return false;

        } catch (Exception e) {
            // Lỗi kỹ thuật (DB, mạng...) → rollback rồi ném lên để BidController xử lý
            System.err.println("[BID] Loi ky thuat: " + e.getMessage());
            rollback(conn);
            throw new DatabaseException("Lỗi hệ thống trong giao dịch đặt giá", e);

        } finally {
            // FIX Bug 5: finally không được throw — nếu throw, exception gốc bị nuốt
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Chỉ log, KHÔNG throw trong finally
                    System.err.println("[BID] Loi khi dong connection: " + e.getMessage());
                }
            }
        }
    }

    // ── Đóng phiên đấu giá ───────────────────────────────────────────────────

    /**
     * FIX Bug 2: Đổi return type từ void → boolean.
     *   - true  : đóng thành công
     *   - false : lỗi (không tìm thấy, sai trạng thái)
     *
     * AuctionEngine gọi: boolean isClosed = auctionService.closeAuction(...)
     * Nếu vẫn để void → compile error.
     */
    public boolean closeAuction(int auctionId) {
        try {
            Auction auction = auctionDAO.getAuctionById(auctionId);
            if (auction == null) {
                throw new AuctionNotFoundException("Lỗi: Không tồn tại auction");
            }
            // FIX Bug 4: Khớp với status thực tế "ACTIVE" thay vì "RUNNING"
            if (!AuctionEngine.STATUS_ACTIVE.equals(auction.getStatus())) {
                throw new AuctionClosedException("Lỗi: Auction hiện đang không mở");
            }

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
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("[BID] Loi rollback: " + ex.getMessage());
            }
        }
    }
}