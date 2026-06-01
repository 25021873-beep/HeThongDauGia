package org.example.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance;
    private final Properties properties;

    private ConfigManager() {
        properties = new Properties();

        if (loadExternalConfig()) {
            return;
        }
        if (loadClasspathConfig("application.properties")) {
            return;
        }
        if (loadClasspathConfig("application-template.properties")) {
            System.err.println("[CONFIG] Dang dung application-template.properties. Hay tao file application.properties khi deploy.");
        } else {
            System.err.println("[CONFIG] Khong tim thay file cau hinh. He thong se dung gia tri mac dinh.");
        }
    }

    private boolean loadExternalConfig() {
        String configuredPath = System.getProperty("auction.config", "application.properties");
        Path configPath = Path.of(configuredPath);

        if (!Files.isRegularFile(configPath)) {
            return false;
        }

        try (InputStream input = new FileInputStream(configPath.toFile())) {
            properties.load(input);
            System.out.println("[CONFIG] Da load cau hinh tu file: " + configPath.toAbsolutePath());
            return true;
        } catch (Exception e) {
            System.err.println("[CONFIG] Loi khi doc file cau hinh ngoai jar: " + e.getMessage());
            return false;
        }
    }

    private boolean loadClasspathConfig(String resourceName) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) {
                return false;
            }
            properties.load(input);
            System.out.println("[CONFIG] Da load cau hinh tu classpath: " + resourceName);
            return true;
        } catch (Exception e) {
            System.err.println("[CONFIG] Loi khi doc " + resourceName + ": " + e.getMessage());
            return false;
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
