package org.example.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.exception.database.DatabaseException;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;

    private final HikariDataSource dataSource;

    private DatabaseConnection() {
        ConfigManager config = ConfigManager.getInstance();

        String url = config.getString(
                "db.url",
                "jdbc:mysql://localhost:3306/auction_system?useSSL=false");
        String user = config.getString("db.user", "root");
        String password = config.getString("db.password", "");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.setPoolName("auction-db-pool");

        hikariConfig.setMaximumPoolSize(config.getInt("db.pool.maximumPoolSize", 20));
        hikariConfig.setMinimumIdle(config.getInt("db.pool.minimumIdle", 5));
        hikariConfig.setConnectionTimeout(config.getInt("db.pool.connectionTimeout", 30000));
        hikariConfig.setIdleTimeout(config.getInt("db.pool.idleTimeout", 600000));
        hikariConfig.setMaxLifetime(config.getInt("db.pool.maxLifetime", 1800000));

        int leakDetectionThreshold = config.getInt("db.pool.leakDetectionThreshold", 0);
        if (leakDetectionThreshold > 0) {
            hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold);
        }

        try {
            dataSource = new HikariDataSource(hikariConfig);
            try (Connection ignored = dataSource.getConnection()) {
                System.out.println("[DB] Ket noi database thanh cong. Pool size toi da: "
                        + hikariConfig.getMaximumPoolSize());
            }
        } catch (SQLException e) {
            throw new DatabaseException("Khong the ket noi database: ", e);
        } catch (RuntimeException e) {
            throw new DatabaseException("Khong the khoi tao database pool: ", e);
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
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("Loi lay connection tu pool: ", e);
        }
    }

    public static synchronized void shutdownPool() {
        if (instance != null && instance.dataSource != null && !instance.dataSource.isClosed()) {
            instance.dataSource.close();
            System.out.println("[DB] Da dong database connection pool");
        }
    }
}
