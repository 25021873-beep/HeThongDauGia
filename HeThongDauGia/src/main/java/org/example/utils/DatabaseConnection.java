package org.example.utils;

import org.example.exception.database.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL      = ConfigManager.getInstance().getString("db.url", "jdbc:mysql://mysql-3ccc6173-hethongdaugia1.j.aivencloud.com:11394/defaultdb?useSSL=true&trustServerCertificate=true");
    private static final String USER     = ConfigManager.getInstance().getString("db.user", "avnadmin");
    private static final String PASSWORD = ConfigManager.getInstance().getString("db.password", "AVNS_d8-zQN7D1h9e8-YzhcW");

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Ket noi database thanh cong");
        } catch (SQLException e) {
            throw new DatabaseException("Khong the ket noi database: ",e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Tai khoi tao ket noi database thanh cong");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Loi kiem tra ket noi: ", e);
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Da dong ket noi database");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Loi khi dong ket noi: " + e.getMessage());
        }
    }
}