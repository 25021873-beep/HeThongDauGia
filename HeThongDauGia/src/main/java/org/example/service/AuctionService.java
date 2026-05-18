package org.example.service;

import org.example.dao.*;
import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
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
import org.example.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;


public class AuctionService {
    private AuctionDAO auctionDAO = new AuctionDAO();
    private BidTransactionDAO bidDAO = new BidTransactionDAO();
    private ItemDAO itemDAO = new ItemDAO();
    private UserDAO userDAO = new UserDAO();
    private BidHistoryDAO bidHistoryDAO = new BidHistoryDAO();
    private AuctionEngine engine;

    private static AuctionService instance;

    private AuctionService() {}

    public static synchronized AuctionService getInstance() {
        if (instance == null) {
            instance = new AuctionService();
        }
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
        newAuction.setStatus("RUNNING");

        if (!auctionDAO.createAuction(newAuction)) {
            throw new AuctionSystemException("Lỗi khi mở Auction");
        }

        itemDAO.updateItemStatus(itemId, "IN_AUCTION");

        if (this.engine != null) {
            this.engine.addAuction(newAuction);
        } else {
            throw new AuctionSystemException("[AUCTION] Canh bao: AuctionEngine chua duoc inject!");
        }
    }

    // ── Đặt giá ───────────────────────────────────────────────────────────────

    public void placeBid(int bidderId, int auctionId, BigDecimal bidAmount) {
        Connection conn = null;
        try {
            // Lấy 1 connection riêng cho toàn bộ transaction này
            conn = DatabaseConnection.getInstance().getConnection();
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
                throw new InvalidBidException("Gia dat phai cao hon gia hien tai: " + auction.getCurrentPrice());
            }

            // BUG FIX 3: kiểm tra null trước khi dùng bidder
            User bidder = userDAO.getUserById(bidderId);
            if (bidder == null) {
                throw new UserNotFoundException("Khong tim thay nguoi dung ID: " + bidderId);
            }

            BigDecimal bidderBalance = bidder.getBalance() != null ? bidder.getBalance() : BigDecimal.ZERO;
            if (bidderBalance.compareTo(bidAmount) < 0) {
                throw new RuntimeException("Vi khong du tien! Can: " + bidAmount + " | Co: " + bidderBalance);
            }

            // II. XỬ LÝ TIỀN NONG ───────────────────────────────────────────

            // 1. Trừ tiền người đặt mới
            BigDecimal newBalance = bidderBalance.subtract(bidAmount);
            userDAO.updateBalance(conn, bidderId, newBalance);

            // 2. Hoàn cọc cho người đặt cao nhất cũ
            BidTransaction highestBid = bidDAO.getHighestBid(auctionId);
            if (highestBid != null) {
                User oldBidder = userDAO.getUserById(highestBid.getBidderId());
                if (oldBidder != null) {
                    BigDecimal oldBalance = oldBidder.getBalance() != null
                            ? oldBidder.getBalance() : BigDecimal.ZERO;
                    BigDecimal refunded = oldBalance.add(highestBid.getBidPrice());
                    userDAO.updateBalance(conn, oldBidder.getId(), refunded);
                }
            }

            // 3. Cập nhật giá hiện tại của phiên
            auctionDAO.updateCurrentPrice(conn, auctionId, bidAmount);

            // 4. Ghi lịch sử giao dịch
            BidTransaction newTx = new BidTransaction();
            newTx.setAuctionId(auctionId);
            newTx.setBidderId(bidderId);
            newTx.setBidPrice(bidAmount);
            newTx.setBidTime(LocalDateTime.now());
            bidDAO.addBid(conn, newTx);

            // 5. Ghi lịch sử giao dịch (dùng cho Visualization)
            BidHistory entry = new BidHistory();
            entry.setAuctionId(auctionId);
            entry.setBidderId(bidderId);
            entry.setPrice(bidAmount);
            entry.setBidTime(LocalDateTime.now());
            bidHistoryDAO.addBidHistory(entry);

            // III. COMMIT ────────────────────────────────────────────────────
            conn.commit();

        } catch (Exception e) {
            System.err.println("[BID] Loi: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new DatabaseException("Lỗi: Không thể rollback",e);
                }
            }

            if (e instanceof AuctionSystemException) {
                throw (AuctionSystemException) e; // Lỗi nghiệp vụ
            } else {
                throw new DatabaseException("Lỗi hệ thống trong quá trình xử lý giao dịch đặt giá", e); // Lỗi vặt (mạng,...)
            }

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    throw new DatabaseException("Lỗi khi đóng CSDL",e);
                }
            }
        }
    }

    // Hàm lấy lịch sử (Dùng cho Visualization)
    public List<BidHistory> getBidHistory(int auctionId) {
        // Kiểm tra auction tồn tại
        if (auctionDAO.getAuctionById(auctionId) == null) {
            throw new AuctionNotFoundException("Lỗi: Không tìm thấy auction có ID: " + auctionId);
        }
        return bidHistoryDAO.getHistoryByAuctionId(auctionId);
    }

    // ── Đóng phiên đấu giá ───────────────────────────────────────────────────

    public void closeAuction(int auctionId) {
        Auction auction = auctionDAO.getAuctionById(auctionId);
        if (auction == null) {
            throw new AuctionNotFoundException("Lỗi: Không tồn tại auction");
        }

        if (!"RUNNING".equals(auction.getStatus())) {
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
    }
}