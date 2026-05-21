package org.example.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChangePasswordResponseTest {

    @Test
    void constructorExposesUsername() {
        ChangePasswordResponse response = new ChangePasswordResponse("alice");

        assertEquals(BaseResponse.STATUS_SUCCESS, response.getStatus());
        assertEquals("Doi mat khau thanh cong", response.getMessage());
        assertEquals("alice", response.getUsername());
    }
}
