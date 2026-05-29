package org.example.utils;

import org.example.exception.database.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL      = ConfigManager.getInstance().getString("db.url", "jdbc:mysql://localhost:3306/auction_system?useSSL=false");
    private static final String USER     = ConfigManager.getInstance().getString("db.user", "root");
    private static final String PASSWORD = ConfigManager.getInstance().getString("db.password", "");

    private static DatabaseConnection instance;

    private DatabaseConnection() {
        // Kiểm tra kết nối lần đầu
        try (Connection testConn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("[DB] Ket noi database thanh cong");
        } catch (SQLException e) {
            throw new DatabaseException("Khong the ket noi database: ", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Tạo connection MỚI mỗi lần gọi.
     * Caller có trách nhiệm đóng connection sau khi dùng xong (try-with-resources).
     */
    public Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            return conn;
        } catch (SQLException e) {
            throw new DatabaseException("Loi tao ket noi database: ", e);
        }
    }
}