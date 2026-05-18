package org.example.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void constructorAndSettersExposeValuesWithoutLeakingPasswordInToString() {
        LoginRequest request = new LoginRequest("alice", "secret");

        assertEquals("alice", request.getUsername());
        assertEquals("secret", request.getPassword());

        request.setUsername("bob");
        request.setPassword("hidden");

        assertEquals("bob", request.getUsername());
        assertEquals("hidden", request.getPassword());
        assertEquals("LoginRequest{username='bob'}", request.toString());
        assertFalse(request.toString().contains("hidden"));
    }
}
