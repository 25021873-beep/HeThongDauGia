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
        assertTrue(request.toString().contains("username='bob'"));
        assertFalse(request.toString().contains("hidden"));
    }
}
