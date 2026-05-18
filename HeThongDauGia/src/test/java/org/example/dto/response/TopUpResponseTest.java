package org.example.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TopUpResponseTest {

    @Test
    void constructorExposesValues() {
        TopUpResponse response = new TopUpResponse(
                6, new BigDecimal("500"), new BigDecimal("1500"));

        assertEquals(BaseResponse.STATUS_SUCCESS, response.getStatus());
        assertEquals("Nap tien thanh cong", response.getMessage());
        assertEquals(6, response.getUserId());
        assertEquals(new BigDecimal("500"), response.getAmountAdded());
        assertEquals(new BigDecimal("1500"), response.getNewBalance());
    }
}
