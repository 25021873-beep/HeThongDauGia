package org.example.dao;

import org.example.entity.Auction;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.math.BigDecimal;

public class AuctionDAO {

    // Tạo phiên đấu giá mới
    public boolean createAuction(Auction auction) {
        String sql = "INSERT INTO Auctions (item_id, start_time, end_time, current_price, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, auction.getItemId());
            pstmt.setObject(2, auction.getStartTime());
            pstmt.setObject(3, auction.getEndTime());
            pstmt.setBigDecimal(4, auction.getCurrentPrice());
            pstmt.setString(5, auction.getStatus());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo phiên đấu giá: " + e.getMessage());
            return false;
        }
    }

    // Lấy chi tiết một phiên
    public Auction getAuctionById(int id) {
        String sql = "SELECT * FROM Auctions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Auction auction = new Auction();
                    auction.setId(rs.getInt("id"));
                    auction.setItemId(rs.getInt("item_id"));
                    auction.setStartTime(rs.getObject("start_time", LocalDateTime.class));
                    auction.setEndTime(rs.getObject("end_time", LocalDateTime.class));
                    auction.setCurrentPrice(rs.getBigDecimal("current_price"));
                    auction.setStatus(rs.getString("status"));
                    auction.setWinnerId(rs.getInt("winner_id")); // Có thể null nếu chưa kết thúc
                    return auction;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết phiên " + id + ": " + e.getMessage());
        }
        return null;
    }

    // Lấy danh sách các phiên đang chạy
    public List<Auction> getActiveAuctions() {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM Auctions WHERE status = 'RUNNING' OR status = 'OPEN'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Auction auction = new Auction();
                auction.setId(rs.getInt("id"));
                auction.setItemId(rs.getInt("item_id"));
                auction.setStartTime(rs.getObject("start_time", LocalDateTime.class));
                auction.setEndTime(rs.getObject("end_time", LocalDateTime.class));
                auction.setCurrentPrice(rs.getBigDecimal("current_price"));
                auction.setStatus(rs.getString("status"));
                // Đang chạy thì chắc chắn chưa có winner_id nên bỏ qua cũng được
                list.add(auction);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách phiên đang chạy: " + e.getMessage());
        }
        return list;
    }

    // Cập nhật giá hiện tại (BidTransactionDAO sẽ gọi hàm này liên tục)
    public boolean updateCurrentPrice(int auctionId, BigDecimal newPrice) {
        String sql = "UPDATE Auctions SET current_price = ? WHERE id = ? AND status IN ('OPEN', 'RUNNING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, newPrice);
            pstmt.setInt(2, auctionId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật giá: " + e.getMessage());
            return false;
        }
    }

    // Chốt phiên đấu giá (Khi hết giờ, hàm này sẽ được gọi để đổi trạng thái và ghi nhận người thắng)
    public boolean closeAuction(int auctionId, int winnerId) {
            String sql = "UPDATE Auctions SET status = 'FINISHED', winner_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, winnerId);
            pstmt.setInt(2, auctionId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi chốt phiên đấu giá: " + e.getMessage());
            return false;
        }
    }

    // Lấy danh sách các phiên đã thắng
    public List<Auction> getWonAuctions(int userId) {
        List<Auction> list = new ArrayList<>();
        // Chỉ lấy những phiên đã chốt (CLOSED) và người thắng trùng với ID truyền vào
        String sql = "SELECT * FROM Auctions WHERE winner_id = ? AND status = 'CLOSED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Auction auction = new Auction();
                    auction.setId(rs.getInt("id"));
                    auction.setItemId(rs.getInt("item_id"));
                    auction.setStartTime(rs.getObject("start_time", LocalDateTime.class));
                    auction.setEndTime(rs.getObject("end_time", LocalDateTime.class));
                    auction.setCurrentPrice(rs.getBigDecimal("current_price"));
                    auction.setStatus(rs.getString("status"));
                    auction.setWinnerId(rs.getInt("winner_id"));
                    list.add(auction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách trúng đấu giá của user " + userId + ": " + e.getMessage());
        }
        return list;
    }

    // Lấy lịch sử đấu giá của một Item
    public List<Auction> getAuctionsByItem(int itemId) {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM Auctions WHERE item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Auction auction = new Auction();
                    auction.setId(rs.getInt("id"));
                    auction.setItemId(rs.getInt("item_id"));
                    auction.setStartTime(rs.getObject("start_time", LocalDateTime.class));
                    auction.setEndTime(rs.getObject("end_time", LocalDateTime.class));
                    auction.setCurrentPrice(rs.getBigDecimal("current_price"));
                    auction.setStatus(rs.getString("status"));
                    auction.setWinnerId(rs.getInt("winner_id"));
                    list.add(auction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm lịch sử đấu giá của món hàng " + itemId + ": " + e.getMessage());
        }
        return list;
    }

    // Xóa phiên đấu giá
    public boolean deleteAuction(int id) {
        String sql = "DELETE FROM Auctions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa phiên đấu giá: " + e.getMessage());
            return false;
        }
    }
}