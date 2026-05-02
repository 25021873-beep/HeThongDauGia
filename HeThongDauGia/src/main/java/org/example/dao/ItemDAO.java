package org.example.dao;

import org.example.entity.Item;
import org.example.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    // Thêm/Đăng bán sản phẩm mới
    public boolean addItem(Item item) {
        String sql = "INSERT INTO Items (name, description, starting_price, seller_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setBigDecimal(3, item.getStartingPrice());
            pstmt.setInt(4, item.getSellerId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm Item: " + e.getMessage());
            return false;
        }
    }

    // Lấy toàn bộ sản phẩm (Dành cho trang chủ của Bidder)
    public List<Item> getAllItems() {
        List<Item> itemList = new ArrayList<>();
        String sql = "SELECT * FROM Items";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Item item = new Item();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setStartingPrice(rs.getBigDecimal("starting_price"));
                item.setSellerId(rs.getInt("seller_id"));
                itemList.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách Item: " + e.getMessage());
        }
        return itemList;
    }

    // Lấy sản phẩm theo ID của Seller (Dành cho màn hình Quản lý của Seller)
    public List<Item> getItemsBySeller(int sellerId) {
        List<Item> itemList = new ArrayList<>();
        String sql = "SELECT * FROM Items WHERE seller_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sellerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Item item = new Item();
                    item.setId(rs.getInt("id"));
                    item.setName(rs.getString("name"));
                    item.setDescription(rs.getString("description"));
                    item.setStartingPrice(rs.getBigDecimal("starting_price"));
                    item.setSellerId(rs.getInt("seller_id"));
                    itemList.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy Item của Seller " + sellerId + ": " + e.getMessage());
        }
        return itemList;
    }

    // Lấy chi tiết 1 sản phẩm theo ID (Dùng khi người dùng bấm vào xem chi tiết)
    public Item getItemById(int id) {
        String sql = "SELECT * FROM Items WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Item item = new Item();
                    item.setId(rs.getInt("id"));
                    item.setName(rs.getString("name"));
                    item.setDescription(rs.getString("description"));
                    item.setStartingPrice(rs.getBigDecimal("starting_price"));
                    item.setSellerId(rs.getInt("seller_id"));
                    return item;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm Item ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    // Sửa thông tin sản phẩm
    public boolean updateItem(Item item) {
        String sql = "UPDATE Items SET name = ?, description = ?, starting_price = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setBigDecimal(3, item.getStartingPrice());
            pstmt.setInt(4, item.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật Item: " + e.getMessage());
            return false;
        }
    }

    // Xóa sản phẩm
    public boolean deleteItem(int id) {
        String sql = "DELETE FROM Items WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa Item: " + e.getMessage());
            return false;
        }
    }
}