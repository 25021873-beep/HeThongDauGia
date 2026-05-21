package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainTest {

    @Test
    void mainRunsWithoutError() {
        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }
}
