package org.example.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChangePasswordRequestTest {

    @Test
    void constructorAndSettersExposeValuesWithoutLeakingPasswordsInToString() {
        ChangePasswordRequest request = new ChangePasswordRequest("alice", "old", "new");

        assertEquals("alice", request.getUsername());
        assertEquals("old", request.getOldPassword());
        assertEquals("new", request.getNewPassword());

        request.setUsername("bob");
        request.setOldPassword("old2");
        request.setNewPassword("new2");

        assertEquals("bob", request.getUsername());
        assertEquals("old2", request.getOldPassword());
        assertEquals("new2", request.getNewPassword());
        assertTrue(request.toString().contains("username='bob'"));
        assertFalse(request.toString().contains("old2"));
        assertFalse(request.toString().contains("new2"));
    }
}
