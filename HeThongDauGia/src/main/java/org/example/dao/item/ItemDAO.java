package org.example.dao.item;

import org.example.entity.item.Art;
import org.example.entity.item.Electronics;
import org.example.entity.item.Item;
import org.example.entity.item.Vehicle;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    // Thêm/Đăng bán sản phẩm mới
    public boolean addItem(Item item) {
            // Tọng cả 4 cột dữ liệu đặc thù vào chung 1 lệnh INSERT
            String sql = "INSERT INTO Items (name, description, starting_price, status, item_type, warranty_months, author, engine_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Tham số chung
                pstmt.setString(1, item.getName());
                pstmt.setString(2, item.getDescription());
                pstmt.setBigDecimal(3, item.getStartingPrice());
                pstmt.setString(4, item.getStatus()); // 'AVAILABLE', 'IN_AUCTION', 'SOLD'

                if (item instanceof Electronics) {
                    pstmt.setString(5, "ELECTRONICS");
                    pstmt.setInt(6, ((Electronics) item).getWarrantyMonths());

                    pstmt.setNull(7, Types.VARCHAR);
                    pstmt.setNull(8, Types.INTEGER);
                }
                else if (item instanceof Art) {
                    pstmt.setString(5, "ART");
                    pstmt.setNull(6, Types.INTEGER);

                    pstmt.setString(7, ((Art) item).getAuthor());
                    pstmt.setNull(8, Types.VARCHAR);
                }
                else if (item instanceof Vehicle) {
                    pstmt.setString(5, "VEHICLE");
                    pstmt.setNull(6, Types.INTEGER);
                    pstmt.setNull(7, Types.VARCHAR);
                    pstmt.setString(8, ((Vehicle) item).getEngineType());
                }

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
                Item item = ItemFactory.createItem(rs);
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
                    Item item = ItemFactory.createItem(rs);
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
                    Item item = ItemFactory.createItem(rs);
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
        String sql = "UPDATE Items SET name = ?, description = ?, starting_price = ?, " +
                "warranty_months = ?, author = ?, engine_type = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setBigDecimal(3, item.getStartingPrice());

            if (item instanceof Electronics) {
                pstmt.setInt(4, ((Electronics) item).getWarrantyMonths());
                pstmt.setNull(5, java.sql.Types.VARCHAR);
                pstmt.setNull(6, java.sql.Types.VARCHAR);
            }
            else if (item instanceof Art) {
                pstmt.setNull(4, java.sql.Types.INTEGER);
                pstmt.setString(5, ((Art) item).getAuthor());
                pstmt.setNull(6, java.sql.Types.VARCHAR);
            }
            else if (item instanceof Vehicle) {
                pstmt.setNull(4, java.sql.Types.INTEGER);
                pstmt.setNull(5, java.sql.Types.VARCHAR);
                pstmt.setString(6, ((Vehicle) item).getEngineType());
            }
            else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
                pstmt.setNull(5, java.sql.Types.VARCHAR);
                pstmt.setNull(6, java.sql.Types.VARCHAR);
            }

            pstmt.setInt(7, item.getId());

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

    // Tìm sản phẩm dựa theo trạng thái hiện tại
    public List<Item> getItemsByStatus(String status) {
        String sql = "SELECT * FROM Items WHERE status = ?";
        ArrayList<Item> itemsList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Item item = ItemFactory.createItem(rs);
                    itemsList.add(item);
                }
                return itemsList;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm status " + status + ": " + e.getMessage());
        }
        return null;
    }

    // Tìm sản phẩm theo tên
    public List<Item> searchItems(String keyword) {
        List<Item> searchResults = new ArrayList<>();

        String sql = "SELECT * FROM Items WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, '%' + keyword + '%');

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Item item = ItemFactory.createItem(rs);
                    searchResults.add(item);
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi!");
            e.printStackTrace();
        }

        return searchResults;
    }

    // Hàm thay đổi trạng thái Item
    public boolean updateItemStatus(int itemId, String newStatus) {
        String sql = "UPDATE Items SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, itemId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái Item (ID: " + itemId + "): " + e.getMessage());
            return false;
        }
    }
}