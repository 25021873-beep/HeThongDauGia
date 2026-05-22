package org.example.dao.user;

import org.example.entity.user.Admin;
import org.example.entity.user.Bidder;
import org.example.entity.user.Seller;
import org.example.entity.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserFactory {
    public static User createUser(ResultSet rs) throws SQLException {
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
}
