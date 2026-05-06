package org.example.dao;

import org.example.entity.BidTransaction;
import org.example.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BidTransactionDAO {

    // Thêm lượt trả giá mới
    public boolean addBid(BidTransaction bid) {
        String sql = "INSERT INTO Bid_Transactions (auction_id, bidder_id, bid_price, bid_time) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bid.getAuctionId());
            pstmt.setInt(2, bid.getBidderId());
            pstmt.setBigDecimal(3, bid.getBidPrice());
            pstmt.setObject(4, bid.getBidTime());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi ghi nhận lịch sử đặt giá: " + e.getMessage());
            return false;
        }
    }

    // Thêm lượt trả giá mới nhưng nhận Connection từ ngoài truyền vào, có throws SQLException
    public boolean addBid(Connection conn, BidTransaction bid) throws SQLException {
        String sql = "INSERT INTO Bid_Transactions (auction_id, bidder_id, bid_price, bid_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bid.getAuctionId());
            pstmt.setInt(2, bid.getBidderId());
            pstmt.setBigDecimal(3, bid.getBidPrice());
            pstmt.setObject(4, bid.getBidTime());
            return pstmt.executeUpdate() > 0;
        }
    }

    // Lấy lịch sử trả giá của một phiên đấu giá
    public List<BidTransaction> getBidsByAuction(int auctionId) {
        List<BidTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM Bid_Transactions WHERE auction_id = ? ORDER BY bid_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, auctionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BidTransaction bid = new BidTransaction();
                    bid.setId(rs.getInt("id"));
                    bid.setAuctionId(rs.getInt("auction_id"));
                    bid.setBidderId(rs.getInt("bidder_id"));
                    bid.setBidPrice(rs.getBigDecimal("bid_price"));
                    bid.setBidTime(rs.getObject("bid_time", LocalDateTime.class));
                    list.add(bid);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử giá của phiên " + auctionId + ": " + e.getMessage());
        }
        return list;
    }

    // Lấy lịch sử trả giá của một Bidder
    public List<BidTransaction> getBidsByUser(int bidderId) {
        List<BidTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM Bid_Transactions WHERE bidder_id = ? ORDER BY bid_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bidderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BidTransaction bid = new BidTransaction();
                    bid.setId(rs.getInt("id"));
                    bid.setAuctionId(rs.getInt("auction_id"));
                    bid.setBidderId(rs.getInt("bidder_id"));
                    bid.setBidPrice(rs.getBigDecimal("bid_price"));
                    bid.setBidTime(rs.getObject("bid_time", LocalDateTime.class));
                    list.add(bid);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử đi bid của User " + bidderId + ": " + e.getMessage());
        }
        return list;
    }

    // Lấy lượt trả giá cao nhất (Dùng để check xem giá mới nhập vào có hợp lệ không)
    public BidTransaction getHighestBid(int auctionId) {
        // Sắp xếp theo giá giảm dần (DESC) và chỉ lấy đúng 1 dòng đầu tiên (LIMIT 1)
        String sql = "SELECT * FROM Bid_Transactions WHERE auction_id = ? ORDER BY bid_price DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, auctionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BidTransaction bid = new BidTransaction();
                    bid.setId(rs.getInt("id"));
                    bid.setAuctionId(rs.getInt("auction_id"));
                    bid.setBidderId(rs.getInt("bidder_id"));
                    bid.setBidPrice(rs.getBigDecimal("bid_price"));
                    bid.setBidTime(rs.getObject("bid_time", LocalDateTime.class));
                    return bid;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm người trả giá cao nhất của phiên " + auctionId + ": " + e.getMessage());
        }
        return null;
    }
}
