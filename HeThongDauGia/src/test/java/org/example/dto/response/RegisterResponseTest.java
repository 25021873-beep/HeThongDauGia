package org.example.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterResponseTest {

    @Test
    void constructorExposesValues() {
        RegisterResponse response = new RegisterResponse(7, "bob", "BIDDER");

        assertEquals(BaseResponse.STATUS_SUCCESS, response.getStatus());
        assertEquals("Dang ky thanh cong", response.getMessage());
        assertEquals(7, response.getUserId());
        assertEquals("bob", response.getUsername());
        assertEquals("BIDDER", response.getRole());
    }
}
