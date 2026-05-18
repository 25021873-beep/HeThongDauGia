package org.example.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {

    @Test
    void constructorExposesValuesAndDefaultsNullBalanceToZero() {
        LoginResponse response = new LoginResponse(1, "alice", "BIDDER", new BigDecimal("100"));
        LoginResponse nullBalance = new LoginResponse(2, "bob", "SELLER", null);

        assertEquals(BaseResponse.STATUS_SUCCESS, response.getStatus());
        assertEquals(1, response.getUserId());
        assertEquals("alice", response.getUsername());
        assertEquals("BIDDER", response.getRole());
        assertEquals(new BigDecimal("100"), response.getBalance());
        assertEquals(BigDecimal.ZERO, nullBalance.getBalance());
    }
}
