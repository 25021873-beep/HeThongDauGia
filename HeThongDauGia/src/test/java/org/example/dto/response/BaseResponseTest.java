package org.example.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseResponseTest {

    @Test
    void successReflectsStatusAndSerializesSingleLineJson() {
        BaseResponse response = new TestResponse(BaseResponse.STATUS_SUCCESS, "ok");

        assertEquals(BaseResponse.STATUS_SUCCESS, response.getStatus());
        assertEquals("ok", response.getMessage());
        assertTrue(response.isSuccess());
        assertEquals(response.serialize(), response.toString());
        assertFalse(response.serialize().contains("\n"));
        assertTrue(response.serialize().contains("\"status\":\"SUCCESS\""));
    }

    private static final class TestResponse extends BaseResponse {
        private TestResponse(String status, String message) {
            super(status, message);
        }
    }
}
