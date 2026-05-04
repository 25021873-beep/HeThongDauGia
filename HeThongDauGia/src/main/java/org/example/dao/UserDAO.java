package org.example.dao;

import org.example.entity.User;
import org.example.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Hàm lấy danh sách người dùng
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM Users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));

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
        String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole()); // role là 'ADMIN', 'SELLER', hoặc 'BIDDER'

            // executeUpdate() dùng cho lệnh INSERT, UPDATE, DELETE. Trả về số dòng bị ảnh hưởng.
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Nếu > 0 tức là insert thành công

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
                    User loggedInUser = new User();
                    loggedInUser.setId(rs.getInt("id"));
                    loggedInUser.setUsername(rs.getString("username"));
                    loggedInUser.setPassword(rs.getString("password"));
                    loggedInUser.setRole(rs.getString("role"));
                    return loggedInUser;
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
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    return user;
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
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    return user;
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

}






