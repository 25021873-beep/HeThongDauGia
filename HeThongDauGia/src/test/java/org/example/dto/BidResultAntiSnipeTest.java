package org.example.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BidResultAntiSnipeTest {

    @Test
    void successResultMeansBidAcceptedWithoutExtension() {
        BidResult result = BidResult.success();

        assertTrue(result.isSuccess());
        assertFalse(result.isExtended());
        assertEquals(0, result.getExtendedSeconds());
        assertNull(result.getNewEndTime());
        assertNull(result.getErrorMessage());
    }

    @Test
    void successWithExtensionCarriesAntiSnipeInformation() {
        LocalDateTime newEndTime = LocalDateTime.of(2026, 6, 4, 20, 1);

        BidResult result = BidResult.successWithExtension(120, newEndTime);

        assertTrue(result.isSuccess());
        assertTrue(result.isExtended());
        assertEquals(120, result.getExtendedSeconds());
        assertEquals(newEndTime, result.getNewEndTime());
    }

    @Test
    void failureResultCarriesBusinessErrorMessage() {
        BidResult result = BidResult.failure("Gia dat thap hon gia hien tai");

        assertFalse(result.isSuccess());
        assertFalse(result.isExtended());
        assertEquals("Gia dat thap hon gia hien tai", result.getErrorMessage());
    }
}
