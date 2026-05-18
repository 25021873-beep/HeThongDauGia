package org.example.entity.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    @Test
    void constructorSetsAdminRole() {
        Admin admin = new Admin();

        assertEquals("ADMIN", admin.getRole());
        assertDoesNotThrow(admin::doSomething);
    }
}
