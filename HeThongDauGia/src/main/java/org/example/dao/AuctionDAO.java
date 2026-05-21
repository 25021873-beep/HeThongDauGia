package org.example.dao;

import org.example.entity.Auction;
import org.example.entity.item.Item;
import org.example.exception.database.DatabaseException;
import org.example.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.example.dao.item.ItemFactory.createItem;

public class AuctionDAO {

    public boolean createAuction(Auction auction) {
        String sql = "INSERT INTO Auctions (item_id, start_time, end_time, current_price, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, auction.getItemId());
            pstmt.setObject(2, auction.getStartTime());
            pstmt.setObject(3, auction.getEndTime());
            pstmt.setBigDecimal(4, auction.getCurrentPrice());
            pstmt.setString(5, auction.getStatus());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        auction.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Loi database khi tao auction moi", e);
        }
    }

    public Auction getAuctionById(int id) {
        String sql = """
            SELECT a.*,
                   i.id AS item_id, i.item_type AS item_item_type,
                   i.name AS item_name, i.description AS item_description,
                   i.starting_price AS item_starting_price, i.status AS item_status,
                   i.warranty_months AS item_warranty_months, i.author AS item_author,
                   i.engine_type AS item_engine_type
            FROM Auctions a
            JOIN Items i ON a.item_id = i.id
            WHERE a.id = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Loi database khi lay auction tu Id", e);
        }
        return null;
    }

    public List<Auction> getActiveAuctions() {
        List<Auction> list = new ArrayList<>();
        String sql = """
            SELECT a.*,
                   i.id AS item_id, i.item_type AS item_item_type,
                   i.name AS item_name, i.description AS item_description,
                   i.starting_price AS item_starting_price, i.status AS item_status,
                   i.warranty_months AS item_warranty_months, i.author AS item_author,
                   i.engine_type AS item_engine_type
            FROM Auctions a
            JOIN Items i ON a.item_id = i.id
            WHERE a.status = 'RUNNING'
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi lay danh sach phien dang chay", e);
        }
        return list;
    }

    public boolean updateCurrentPrice(int auctionId, BigDecimal newPrice) {
        String sql = "UPDATE Auctions SET current_price = ? WHERE id = ? AND status IN ('OPEN', 'RUNNING')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, newPrice);
            pstmt.setInt(2, auctionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi cap nhat gia", e);
        }
    }

    public boolean updateCurrentPrice(Connection conn, int id, BigDecimal newPrice) throws SQLException {
        String sql = "UPDATE Auctions SET current_price = ? WHERE id = ? AND status IN ('OPEN', 'RUNNING')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, newPrice);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── MỚI: Anti-snipe ──────────────────────────────────────────────────────

    /**
     * Cập nhật end_time xuống DB sau khi gia hạn anti-snipe.
     * Chỉ update khi phiên đang RUNNING để tránh race condition.
     */
    public boolean updateEndTime(int auctionId, LocalDateTime newEndTime) {
        String sql = "UPDATE Auctions SET end_time = ? WHERE id = ? AND status = 'RUNNING'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, newEndTime);
            pstmt.setInt(2, auctionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi gia han thoi gian phien dau gia", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    public boolean closeAuction(int auctionId, int winnerId) {
        String sql = "UPDATE Auctions SET status = 'FINISHED', winner_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, winnerId);
            pstmt.setInt(2, auctionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi chot phien dau gia", e);
        }
    }

    public boolean updateAuctionStatus(String status, int auctionId) {
        String sql = "UPDATE Auctions SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, auctionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi doi trang thai auction", e);
        }
    }

    public List<Auction> getWonAuctions(int userId) {
        List<Auction> list = new ArrayList<>();
        String sql = """
            SELECT a.*,
                   i.id AS item_id, i.item_type AS item_item_type,
                   i.name AS item_name, i.description AS item_description,
                   i.starting_price AS item_starting_price, i.status AS item_status,
                   i.warranty_months AS item_warranty_months, i.author AS item_author,
                   i.engine_type AS item_engine_type
            FROM Auctions a
            JOIN Items i ON a.item_id = i.id
            WHERE a.winner_id = ? AND a.status = 'FINISHED'
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi lay danh sach trung dau gia cua user: " + userId, e);
        }
        return list;
    }

    public List<Auction> getAuctionsByItem(int itemId) {
        List<Auction> list = new ArrayList<>();
        String sql = """
            SELECT a.*,
                   i.id AS item_id, i.item_type AS item_item_type,
                   i.name AS item_name, i.description AS item_description,
                   i.starting_price AS item_starting_price, i.status AS item_status,
                   i.warranty_months AS item_warranty_months, i.author AS item_author,
                   i.engine_type AS item_engine_type
            FROM Auctions a
            JOIN Items i ON a.item_id = i.id
            WHERE a.item_id = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi tim lich su dau gia cua mon hang " + itemId, e);
        }
        return list;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Auction mapRow(ResultSet rs) throws SQLException {
        Auction auction = new Auction();
        auction.setId(rs.getInt("id"));
        auction.setItemId(rs.getInt("item_id"));
        auction.setStartTime(rs.getObject("start_time", LocalDateTime.class));
        auction.setEndTime(rs.getObject("end_time", LocalDateTime.class));
        auction.setCurrentPrice(rs.getBigDecimal("current_price"));
        auction.setStatus(rs.getString("status"));
        auction.setWinnerId(rs.getInt("winner_id"));
        auction.setItem(createItem(rs, "item_"));
        return auction;
    }
}