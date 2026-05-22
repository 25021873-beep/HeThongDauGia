package org.example.dao.user;

import org.example.entity.user.Bidder;
import org.example.entity.user.Seller;
import org.example.entity.user.User;
import org.example.exception.database.DatabaseException;
import org.example.utils.DatabaseConnection;

import java.math.BigDecimal;
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

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User user = UserFactory.createUser(rs);
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

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi check username: ",e);
        }
    }

    // Hàm Đăng ký người dùng mới
    public boolean addUser(User user) {
        String sql = "INSERT INTO Users (username, password, email, role, balance, rating) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
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
            throw new DatabaseException("Lỗi khi thêm User: ",e);
        }
    }

    // Hàm Đăng nhập
    public User checkLogin(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return UserFactory.createUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi đăng nhập: ",e);
        }
        return null;
    }

    // Hàm lấy thông tin User qua ID
    public User getUserById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return UserFactory.createUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi tìm User ID " + id + ": ",e);
        }
        return null;
    }

    // Hàm lấy thông tin User qua username
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return UserFactory.createUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi tìm User ID " + username + ": ",e);
        }
        return null;
    }

    // Hàm đổi mật khẩu
    public boolean changePassword(String newpass, String username) {
        String sql = "UPDATE Users SET password = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newpass);
            pstmt.setString(2, username);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi đổi mật khẩu: ",e);
        }
    }

    // Hàm thay đổi balance, phục vụ ĐỘC QUYỀN cho luồng Đặt giá (Nằm chung trong 1 Transaction)
    public boolean updateBalance(Connection conn, int userId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE Users SET balance = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, newBalance);
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;
        }
    }

    // Hàm cập nhật thông tin User
    public boolean updateUser(User user) {
        // Câu SQL update toàn bộ các cột trong bảng Users
        String sql = "UPDATE Users SET username = ?, password = ?, email = ?, role = ?, balance = ?, rating = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole()); // ADMIN, BIDDER, hoặc SELLER

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

            pstmt.setInt(7, user.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi update User (ID: " + user.getId() + "): ",e);
        }
    }

}






