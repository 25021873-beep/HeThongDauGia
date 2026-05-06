package org.example.dao;

import org.example.entity.user.Admin;
import org.example.entity.user.Bidder;
import org.example.entity.user.Seller;
import org.example.entity.user.User;
import org.example.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Helper method để tránh lặp code khởi tạo
    private User mapUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User user;

        // Đúc đúng loại object dựa trên role trong DB
        switch (role) {
            case "ADMIN": user = new Admin(); break;
            case "SELLER":
                user = new Seller();
                ((Seller) user).setRating(rs.getDouble("rating"));
                break;
            case "BIDDER":
                user = new Bidder();
                ((Bidder) user).setBalance(rs.getBigDecimal("balance"));
                break;
            default: return null;
        }

        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        return user;
    }

    // Hàm lấy danh sách người dùng
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM Users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User user = mapUser(rs);
                userList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    // Hàm kiểm tra xem Username đã tồn tại chưa (Dùng trước khi Đăng ký)
    public boolean checkUsernameExists(String username) {
        String sql = "SELECT id FROM Users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi check username: " + e.getMessage());
        }
        return false;
    }

    // Hàm Đăng ký người dùng mới
    public boolean addUser(User user) {
        String sql = "INSERT INTO Users (username, password, email, role, balance, rating) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole());

            if (user instanceof Bidder) {
                pstmt.setBigDecimal(5, ((Bidder) user).getBalance());
                pstmt.setNull(6, java.sql.Types.DOUBLE);
            } else if (user instanceof Seller) {
                pstmt.setNull(5, java.sql.Types.DECIMAL);
                pstmt.setDouble(6, ((Seller) user).getRating());
            } else {
                pstmt.setNull(5, java.sql.Types.DECIMAL);
                pstmt.setNull(6, java.sql.Types.DOUBLE);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm User: " + e.getMessage());
            return false;
        }
    }

    // Hàm Đăng nhập
    public User checkLogin(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đăng nhập: " + e.getMessage());
        }
        return null;
    }

    // Hàm lấy thông tin User qua ID
    public User getUserById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm User ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    // Hàm lấy thông tin User qua username
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm User ID " + username + ": " + e.getMessage());
        }
        return null;
    }

    // Hàm đổi mật khẩu
    public boolean changePassword(String newpass, String username) {
        String sql = "UPDATE Users SET password = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newpass);
            pstmt.setString(2, username);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi đổi mật khẩu: " + e.getMessage());
            return false;
        }
    }

    // Hàm nạp tiền
    public boolean addBalance(BigDecimal amount, int userId) {
        String sql = "UPDATE Users SET balance = balance + ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, amount);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi đổi mật khẩu: " + e.getMessage());
            return false;
        }
    }

}






