package org.example.dao.item;

import org.example.entity.item.Art;
import org.example.entity.item.Electronics;
import org.example.entity.item.Item;
import org.example.entity.item.Vehicle;
import org.example.exception.database.DatabaseException;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    // Thêm/Đăng bán sản phẩm mới
    public int addItem(Item item) {
            // Tọng cả 4 cột dữ liệu đặc thù vào chung 1 lệnh INSERT
            String sql = "INSERT INTO items (name, description, starting_price, seller_id, status, item_type, warranty_months, author, engine_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS)) {

                // Tham số chung
                pstmt.setString(1, item.getName());
                pstmt.setString(2, item.getDescription());
                pstmt.setBigDecimal(3, item.getStartingPrice());
                pstmt.setInt(4, item.getSellerId());
                pstmt.setString(5, item.getStatus()); // 'AVAILABLE', 'IN_AUCTION', 'SOLD'

                if (item instanceof Electronics) {
                    pstmt.setString(6, "ELECTRONICS");
                    pstmt.setInt(7, ((Electronics) item).getWarrantyMonths());

                    pstmt.setNull(8, Types.VARCHAR);
                    pstmt.setNull(9, Types.INTEGER);
                }
                else if (item instanceof Art) {
                    pstmt.setString(6, "ART");
                    pstmt.setNull(7, Types.INTEGER);

                    pstmt.setString(8, ((Art) item).getAuthor());
                    pstmt.setNull(9, Types.VARCHAR);
                }
                else if (item instanceof Vehicle) {
                    pstmt.setString(6, "VEHICLE");
                    pstmt.setNull(7, Types.INTEGER);
                    pstmt.setNull(8, Types.VARCHAR);
                    pstmt.setString(9, ((Vehicle) item).getEngineType());
                }

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows>0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()){
                        if (rs.next()) {
                            int newItemid = rs.getInt(1);
                            return newItemid;
                        }
                    }
                }
            } catch (SQLException e) {
                throw new DatabaseException("Lỗi khi thêm Item: ",e);
            }
            return -1;
        }

    // Lấy toàn bộ sản phẩm (Dành cho trang chủ của Bidder)
    public List<Item> getAllItems() {
        List<Item> itemList = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Item item = ItemFactory.createItem(rs);
                itemList.add(item);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi lấy danh sách Item: ",e);
        }
        return itemList;
    }

    // Lấy sản phẩm theo ID của Seller (Dành cho màn hình Quản lý của Seller)
    public List<Item> getItemsBySeller(int sellerId) {
        List<Item> itemList = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE seller_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sellerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Item item = ItemFactory.createItem(rs);
                    itemList.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi lấy Item của Seller " + sellerId + ": ",e);
        }
        return itemList;
    }

    // Lấy chi tiết 1 sản phẩm theo ID (Dùng khi người dùng bấm vào xem chi tiết)
    public Item getItemById(int id) {
        String sql = "SELECT * FROM items WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Item item = ItemFactory.createItem(rs);
                    return item;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi tìm Item ID " + id + ": ",e);
        }
        return null;
    }

    // Sửa thông tin sản phẩm
    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET name = ?, description = ?, starting_price = ?, " +
                "warranty_months = ?, author = ?, engine_type = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
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
            throw new DatabaseException("Lỗi khi cập nhật Item: ",e);
        }
    }

    // Xóa sản phẩm
    public boolean deleteItem(int id) {
        String sql = "DELETE FROM items WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi xóa Item: ",e);
        }
    }

    // Tìm sản phẩm dựa theo trạng thái hiện tại
    public List<Item> getItemsByStatus(String status) {
        String sql = "SELECT * FROM items WHERE status = ?";
        ArrayList<Item> itemsList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
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
            throw new DatabaseException("Lỗi khi tìm status " + status + ": ",e);
        }
    }

    // Tìm sản phẩm theo tên
    public List<Item> searchItems(String keyword) {
        List<Item> searchResults = new ArrayList<>();

        String sql = "SELECT * FROM items WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
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
        String sql = "UPDATE items SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, itemId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi cập nhật trạng thái Item (ID: " + itemId + "): ",e);
        }
    }
}
