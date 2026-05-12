package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        // Đọc từ biến môi trường nếu có, fallback về giá trị local dev
        URL      = System.getenv().getOrDefault("DB_URL",  "jdbc:mysql://localhost:3306/auction_system?useSSL=false&serverTimezone=UTC");
        USER     = System.getenv().getOrDefault("DB_USER", "root");
        PASSWORD = System.getenv().getOrDefault("DB_PASS", "MinhMonMen0510.");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Không tìm thấy MySQL JDBC Driver: " + e.getMessage());
        }
    }

    private DatabaseConnection() {}

    /**
     * Trả về một Connection MỚI mỗi lần gọi.
     * Caller (Service/DAO) có trách nhiệm đóng connection sau khi dùng xong
     * (dùng try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}