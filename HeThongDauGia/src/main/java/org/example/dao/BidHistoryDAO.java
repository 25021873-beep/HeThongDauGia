package org.example.dao;

import org.example.entity.BidHistory;
import org.example.exception.database.DatabaseException;
import org.example.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BidHistoryDAO {

    public BidHistoryDAO() {
        // Không lưu connection — mỗi method tự lấy connection mới
    }

    // ── INSERT ────────────────────────────────────────────────────────────────

    // Hàm thêm lịch sử bid (dùng trong transaction, nhận connection từ ngoài)
    public void addBidHistory(Connection conn, BidHistory entry) {
        String sql = """
                INSERT INTO bid_history (auction_id, bidder_id, price, bid_time)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, entry.getAuctionId());
            stmt.setLong(2, entry.getBidderId());
            stmt.setBigDecimal(3, entry.getPrice());
            stmt.setTimestamp(4, Timestamp.valueOf(entry.getBidTime()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    entry.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Loi khi luu bid history: ",e);
        }
    }

    // ── SELECT ────────────────────────────────────────────────────────────────

    // Hàm lấy toàn bộ lịch sử đấu giá của phiên
    public List<BidHistory> getHistoryByAuctionId(long auctionId) {
        String sql = """
                SELECT bh.id, bh.auction_id, bh.bidder_id,
                       u.username AS bidder_username,
                       bh.price, bh.bid_time
                FROM bid_history bh
                JOIN users u ON bh.bidder_id = u.id
                WHERE bh.auction_id = ?
                ORDER BY bh.bid_time ASC
                """;

        return queryList(sql, auctionId);
    }

    // Hàm lấy lịch sử đấu giá của một user cụ thể
    public com.google.gson.JsonArray getUserBidHistory(long bidderId) {
        String sql = "SELECT i.name AS product_name, bh.price, bh.bid_time, a.status, a.winner_id " +
                     "FROM bid_history bh " +
                     "JOIN auctions a ON bh.auction_id = a.id " +
                     "JOIN items i ON a.item_id = i.id " +
                     "WHERE bh.bidder_id = ? " +
                     "ORDER BY bh.bid_time DESC";
                     
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, bidderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
                    obj.addProperty("productName", rs.getString("product_name"));
                    obj.addProperty("bidAmount", rs.getBigDecimal("price"));
                    obj.addProperty("bidTime", rs.getTimestamp("bid_time").toString());
                    obj.addProperty("status", rs.getString("status"));
                    
                    int winnerId = rs.getInt("winner_id");
                    String result = "Đang đấu";
                    String status = rs.getString("status");
                    if ("CLOSED".equals(status) || "FINISHED".equals(status)) {
                        result = (winnerId == bidderId) ? "Thắng" : "Thua";
                    } else if ("CANCELED".equals(status)) {
                        result = "Hủy";
                    }
                    
                    obj.addProperty("result", result);
                    array.add(obj);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi lay lich su cua user: ", e);
        }
        return array;
    }

    // Hàm đếm số lượng bid có trong 1 phiên
    public int countByAuctionId(long auctionId) {
        String sql = "SELECT COUNT(*) FROM bid_history WHERE auction_id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, auctionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Loi khi dem bid history: ",e);
        }

        return 0;
    }


 // Hàm xóa lịch sử phiên
    public void deleteByAuctionId(long auctionId) {
        String sql = "DELETE FROM bid_history WHERE auction_id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, auctionId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Loi khi xoa bid history: ",e);
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    // Hàm dùng chung cho các hàm trả về List
    private List<BidHistory> queryList(String sql, long auctionId) {
        List<BidHistory> result = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, auctionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Loi khi truy van bid history: ",e);
        }

        return result;
    }

    // Hàm map một ResultSet thành đối tượng BidHistory
    private BidHistory mapRow(ResultSet rs) throws SQLException {
        BidHistory entry = new BidHistory();
        entry.setId(rs.getInt("id"));
        entry.setAuctionId(rs.getInt("auction_id"));
        entry.setBidderId(rs.getInt("bidder_id"));
        entry.setBidderUsername(rs.getString("bidder_username"));
        entry.setPrice(rs.getBigDecimal("price"));
        entry.setBidTime(rs.getTimestamp("bid_time").toLocalDateTime());
        return entry;
    }
}