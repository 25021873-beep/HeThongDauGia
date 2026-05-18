package org.example.entity.user;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void commonUserFieldsCanBeSetAndRead() {
        TestUser user = new TestUser();

        user.setId(3);
        user.setUsername("alice");
        user.setPassword("secret");
        user.setEmail("alice@example.com");
        user.setRole("BIDDER");
        user.setBalance(new BigDecimal("1000"));

        assertEquals(3, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("BIDDER", user.getRole());
        assertEquals(new BigDecimal("1000"), user.getBalance());
    }

    private static final class TestUser extends User {
        @Override
        public void doSomething() {
        }
    }
}
