package org.example.dao.user;

import org.example.entity.user.Admin;
import org.example.entity.user.Bidder;
import org.example.entity.user.Seller;
import org.example.entity.user.User;
import org.example.testutil.ResultSetStub;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserFactoryTest {

    @Test
    void createsAdminFromResultSet() throws SQLException {
        User user = UserFactory.createUser(resultSet("ADMIN", Map.of()));

        assertInstanceOf(Admin.class, user);
        assertCommonFields(user);
    }

    @Test
    void createsSellerFromResultSet() throws SQLException {
        User user = UserFactory.createUser(resultSet("SELLER", Map.of("rating", 4.7)));

        assertInstanceOf(Seller.class, user);
        assertEquals(4.7, ((Seller) user).getRating());
        assertCommonFields(user);
    }

    @Test
    void createsBidderFromResultSet() throws SQLException {
        User user = UserFactory.createUser(resultSet("BIDDER", Map.of("balance", new BigDecimal("1000"))));

        assertInstanceOf(Bidder.class, user);
        assertEquals(new BigDecimal("1000"), ((Bidder) user).getBalance());
        assertCommonFields(user);
    }

    @Test
    void returnsNullForUnknownRole() throws SQLException {
        assertNull(UserFactory.createUser(resultSet("GUEST", Map.of())));
    }

    private static ResultSet resultSet(String role, Map<String, Object> extra) {
        Map<String, Object> values = new HashMap<>();
        values.put("role", role);
        values.put("id", 11);
        values.put("username", "alice");
        values.put("password", "secret");
        values.put("email", "alice@example.com");
        values.putAll(extra);
        return ResultSetStub.from(values);
    }

    private static void assertCommonFields(User user) {
        assertEquals(11, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("alice@example.com", user.getEmail());
    }
}
