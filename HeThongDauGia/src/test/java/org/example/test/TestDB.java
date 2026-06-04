package org.example.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://mysql-3ccc6173-hethongdaugia1.j.aivencloud.com:11394/defaultdb?useSSL=true";
        String user = "avnadmin";
        String password = "AVNS_d8-zQN7D1h9e8-YzhcW";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("Connected to the database!");
            ResultSet rs = stmt.executeQuery("DESCRIBE auto_bidding");
            System.out.println("Table auto_bidding columns:");
            while (rs.next()) {
                System.out.println(rs.getString("Field") + " | " +
                                   rs.getString("Type") + " | " +
                                   rs.getString("Null") + " | " +
                                   rs.getString("Key") + " | " +
                                   rs.getString("Default") + " | " +
                                   rs.getString("Extra"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
