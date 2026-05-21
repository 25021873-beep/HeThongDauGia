package org.example.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    @Test
    void settersExposeValues() {
        RegisterRequest request = new RegisterRequest();

        request.setUsername("seller01");
        request.setPassword("secret");
        request.setRole("SELLER");

        assertEquals("seller01", request.getUsername());
        assertEquals("secret", request.getPassword());
        assertEquals("SELLER", request.getRole());
    }
}
