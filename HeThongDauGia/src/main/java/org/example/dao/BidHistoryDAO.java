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

    private final Connection connection;

    public BidHistoryDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ── INSERT ────────────────────────────────────────────────────────────────

    // Hàm thêm lịch sử bid
    public void addBidHistory(BidHistory entry) {
        String sql = """
                INSERT INTO bid_history (auction_id, bidder_id, price, bid_time)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    // Hàm đếm số lượng bid có trong 1 phiên
    public int countByAuctionId(long auctionId) {
        String sql = "SELECT COUNT(*) FROM bid_history WHERE auction_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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