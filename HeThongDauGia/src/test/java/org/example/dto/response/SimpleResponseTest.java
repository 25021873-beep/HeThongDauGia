package org.example.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleResponseTest {

    @Test
    void factoriesUseExpectedStatuses() {
        SimpleResponse success = SimpleResponse.success("ok");
        SimpleResponse error = SimpleResponse.error("bad");
        SimpleResponse info = SimpleResponse.info("note");

        assertTrue(success.isSuccess());
        assertEquals(BaseResponse.STATUS_ERROR, error.getStatus());
        assertEquals(BaseResponse.STATUS_INFO, info.getStatus());
        assertEquals("bad", error.getMessage());
        assertTrue(error.serialize().contains("\"message\":\"bad\""));
    }
}
