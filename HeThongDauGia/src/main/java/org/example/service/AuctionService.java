package org.example.service;

import org.example.AuctionEngine;
import org.example.dao.*;
import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.Auction;
import org.example.entity.BidTransaction;
import org.example.entity.item.Item;
import org.example.entity.Auction.*;
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


    // Hàm đưa Item lên sàn và tạo phiên đấu giá
    public boolean openAuction(int itemId, LocalDateTime endTime) {
        Item item = itemDAO.getItemById(itemId);
        if (item == null || !item.getStatus().equals("AVAILABLE")) {
            System.err.println("Lỗi: Món hàng không tồn tại hoặc đã bị đem đi đấu giá chỗ khác!");
            return false;
        }

        // Tạo đối tượng Auction
        Auction newAuction = new Auction();
        newAuction.setItemId(itemId);
        newAuction.setStartTime(LocalDateTime.now());
        newAuction.setEndTime(endTime);
        newAuction.setCurrentPrice(item.getStartingPrice());
        newAuction.setStatus("RUNNING");

        if (auctionDAO.createAuction(newAuction)) {
            itemDAO.updateItemStatus(itemId, "IN_AUCTION");
            if (this.engine != null) {
                this.engine.addAuction(newAuction);
            } else {
                System.err.println("Lỗi: Quên chưa tiêm (inject) AuctionEngine vào AuctionService!");
            }
            return true;
        }
        return false;
    }

    // Hàm đặt giá
    public boolean placeBid(int bidderId, int auctionId, BigDecimal bidAmount) {
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // I. KIỂM TRA ĐIỀU KIỆN
            Auction auction = auctionDAO.getAuctionById(auctionId);
            if (auction == null || !auction.getStatus().equals("RUNNING")) {
                throw new RuntimeException("Phiên đấu giá đã kết thúc hoặc không tồn tại.");
            }
            if (auction.getEndTime().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Hết giờ rồi, m không được phép đặt nữa!");
            }
            if (bidAmount.compareTo(auction.getCurrentPrice()) <= 0) {
                throw new RuntimeException("Tiền mỏng thế? Phải trả cao hơn giá hiện tại!");
            }

            User bidder = userDAO.getUserById(bidderId);
            if (bidder.getBalance().compareTo(bidAmount) < 0) {
                throw new RuntimeException("Ví không đủ tiền! Đi nạp thêm đi đại gia.");
            }

            // II. XỬ LÝ TIỀN NONG

            // 1. Trừ tiền thằng mới bấm đặt giá
            BigDecimal newBalance = bidder.getBalance().subtract(bidAmount);
            userDAO.updateBalance(conn, bidderId, newBalance);

            // 2. Hoàn cọc cho highest bidder cũ
            BidTransaction highestBid = bidDAO.getHighestBid(auctionId);
            if (highestBid != null) {
                User oldBidder = userDAO.getUserById(highestBid.getBidderId());
                BigDecimal refundedBalance = oldBidder.getBalance().add(highestBid.getBidPrice());
                userDAO.updateBalance(conn, oldBidder.getId(), refundedBalance);
            }

            // 3. Cập nhật lại giá cao nhất của phiên
            auctionDAO.updateCurrentPrice(conn, auctionId, bidAmount);

            // 4. Ghi lại lịch sử giao dịch (Audit Log)
            BidTransaction newTransaction = new BidTransaction();
            newTransaction.setAuctionId(auctionId);
            newTransaction.setBidderId(bidderId);
            newTransaction.setBidPrice(bidAmount);
            newTransaction.setBidTime(LocalDateTime.now());
            bidDAO.addBid(conn, newTransaction);

            // III. CHỐT GIAO DỊCH
            conn.commit();
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Đã Rollback an toàn dữ liệu vì lỗi: " + e.getMessage());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Hàm chốt/đóng phiên đấu giá (Dành cho AuctionEngine gọi)
    public boolean closeAuction(int auctionId) {
        Auction auction = auctionDAO.getAuctionById(auctionId);
        if (auction == null || !auction.getStatus().equals("RUNNING")) return false;

        BidTransaction highestBid = bidDAO.getHighestBid(auctionId);

        if (highestBid != null) {
            auctionDAO.closeAuction(auctionId, highestBid.getBidderId());
            itemDAO.updateItemStatus(auction.getItemId(), "SOLD");
            return true;
        } else {
            auctionDAO.updateAuctionStatus("CANCELED", auctionId);
            itemDAO.updateItemStatus(auction.getItemId(), "AVAILABLE");
            return true;
        }
    }
}