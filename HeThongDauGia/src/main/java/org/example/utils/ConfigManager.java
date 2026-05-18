package org.example.utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance;
    private Properties properties;

    private ConfigManager() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("[CONFIG] Loi: Đéo tìm thấy file application.properties");
                return;
            }
            properties.load(input);
            System.out.println("[CONFIG] Đã load xong cấu hình hệ thống!");
        } catch (Exception e) {
            System.err.println("[CONFIG] Loi khi doc file config: " + e.getMessage());
        }
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("[CONFIG] Loi parse int cho key: " + key);
            }
        }
        return defaultValue;
    }
}