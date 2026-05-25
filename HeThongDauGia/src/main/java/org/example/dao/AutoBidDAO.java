package org.example.dao;

import org.example.entity.AutoBidConfig;
import org.example.exception.database.DatabaseException;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AutoBidDAO {

    private final Connection connection;

    public AutoBidDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // Hàm lưu config mới hoặc cập nhật nếu đã tồn tại
    public void saveOrUpdate(AutoBidConfig config) {
        String sql = """
                INSERT INTO auto_bid_config (auction_id, bidder_id, max_bid, increment, created_at, is_active)
                VALUES (?, ?, ?, ?, ?, TRUE)
                ON DUPLICATE KEY UPDATE
                    max_bid   = VALUES(max_bid),
                    increment = VALUES(increment),
                    is_active = TRUE
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, config.getAuctionId());
            stmt.setInt(2, config.getBidderId());
            stmt.setBigDecimal(3, config.getMaxBid());
            stmt.setBigDecimal(4, config.getIncrement());
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi luu auto-bid config: ",e);
        }
    }

    // Hàm lấy tất cả auto-bid đang active trong một phiên
    public List<AutoBidConfig> getActiveAutoBids(long auctionId) {
        String sql = """
                SELECT * FROM auto_bid_config
                WHERE auction_id = ? AND is_active = TRUE
                ORDER BY created_at ASC
                """;

        List<AutoBidConfig> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, auctionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi lay auto-bid config: ",e);
        }
        return result;
    }

    // Hàm vô hiệu hóa auto-bid của 1 người khi họ đã thắng hoặc vượt maxBid
    public void deactivate(long auctionId, long bidderId) {
        String sql = """
                UPDATE auto_bid_config
                SET is_active = FALSE
                WHERE auction_id = ? AND bidder_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, auctionId);
            stmt.setLong(2, bidderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi deactivate auto-bid: ",e);
        }
    }

    // Hàm MapRow để rút gọn quá trình tạo config
    private AutoBidConfig mapRow(ResultSet rs) throws SQLException {
        AutoBidConfig config = new AutoBidConfig();
        config.setId(rs.getInt("id"));
        config.setAuctionId(rs.getInt("auction_id"));
        config.setBidderId(rs.getInt("bidder_id"));
        config.setMaxBid(rs.getBigDecimal("max_bid"));
        config.setIncrement(rs.getBigDecimal("increment"));
        config.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        config.setActive(rs.getBoolean("is_active"));
        return config;
    }
}
