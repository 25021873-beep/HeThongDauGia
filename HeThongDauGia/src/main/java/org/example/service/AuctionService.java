package org.example.service;

import org.example.dao.*;
import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.Auction;
import org.example.entity.BidTransaction;
import org.example.entity.item.Item;
import org.example.entity.user.User;
import org.example.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;


public class AuctionService {
    private AuctionDAO auctionDAO = new AuctionDAO();
    private BidTransactionDAO bidDAO = new BidTransactionDAO();
    private ItemDAO itemDAO = new ItemDAO();
    private UserDAO userDAO = new UserDAO();
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

    public boolean openAuction(int itemId, LocalDateTime endTime) {
        Item item = itemDAO.getItemById(itemId);
        if (item == null || !item.getStatus().equals("AVAILABLE")) {
            System.err.println("Lỗi: Món hàng không tồn tại hoặc đã bị đem đi đấu giá chỗ khác!");
            return false;
        }

        Auction newAuction = new Auction();
        newAuction.setItemId(itemId);
        newAuction.setStartTime(LocalDateTime.now());
        newAuction.setEndTime(endTime);
        newAuction.setCurrentPrice(item.getStartingPrice());
        newAuction.setStatus("RUNNING");

        if (!auctionDAO.createAuction(newAuction)) {
            return false;
        }

        itemDAO.updateItemStatus(itemId, "IN_AUCTION");

        if (this.engine != null) {
            this.engine.addAuction(newAuction);
        } else {
            System.err.println("[AUCTION] Canh bao: AuctionEngine chua duoc inject!");
        }
        return true;
    }

    // ── Đặt giá ───────────────────────────────────────────────────────────────

    public boolean placeBid(int bidderId, int auctionId, BigDecimal bidAmount) {
        Connection conn = null;
        try {
            // Lấy 1 connection riêng cho toàn bộ transaction này
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // I. KIỂM TRA ĐIỀU KIỆN ─────────────────────────────────────────

            Auction auction = auctionDAO.getAuctionById(auctionId);
            if (auction == null || !"RUNNING".equals(auction.getStatus())) {
                throw new RuntimeException("Phien dau gia da ket thuc hoac khong ton tai.");
            }
            if (auction.getEndTime().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Het gio! Khong the dat gia them.");
            }
            if (bidAmount.compareTo(auction.getCurrentPrice()) <= 0) {
                throw new RuntimeException("Gia dat phai cao hon gia hien tai: " + auction.getCurrentPrice());
            }

            // BUG FIX 3: kiểm tra null trước khi dùng bidder
            User bidder = userDAO.getUserById(bidderId);
            if (bidder == null) {
                throw new RuntimeException("Khong tim thay nguoi dung ID: " + bidderId);
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

            // III. COMMIT ────────────────────────────────────────────────────
            conn.commit();
            return true;

        } catch (Exception e) {
            System.err.println("[BID] Loi: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[BID] Da rollback giao dich.");
                } catch (SQLException ex) {
                    System.err.println("[BID] Rollback that bai: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("[BID] Khong dong duoc connection: " + e.getMessage());
                }
            }
        }
    }

    // ── Đóng phiên đấu giá ───────────────────────────────────────────────────

    public boolean closeAuction(int auctionId) {
        Auction auction = auctionDAO.getAuctionById(auctionId);
        if (auction == null || !"RUNNING".equals(auction.getStatus())) return false;

        BidTransaction highestBid = bidDAO.getHighestBid(auctionId);

        if (highestBid != null) {
            auctionDAO.closeAuction(auctionId, highestBid.getBidderId());
            itemDAO.updateItemStatus(auction.getItemId(), "SOLD");
        } else {
            // Không ai đặt giá → hủy phiên, trả hàng về AVAILABLE
            auctionDAO.updateAuctionStatus("CANCELED", auctionId);
            itemDAO.updateItemStatus(auction.getItemId(), "AVAILABLE");
        }
        return true;
    }
}