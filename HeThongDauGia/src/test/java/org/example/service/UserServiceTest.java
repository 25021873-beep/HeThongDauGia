package org.example.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void topUpBalanceRejectsNullAndNonPositiveAmountsBeforeDatabaseLookup() {
        UserService service = new UserService();

        assertAll(
                () -> assertFalse(service.topUpBalance(1, null)),
                () -> assertFalse(service.topUpBalance(1, BigDecimal.ZERO)),
                () -> assertFalse(service.topUpBalance(1, new BigDecimal("-1")))
        );
    }

    @Test
    void changePasswordRejectsBlankNewPasswordBeforeDatabaseLookup() {
        UserService service = new UserService();

        assertAll(
                () -> assertFalse(service.changePassword("alice", "old", null)),
                () -> assertFalse(service.changePassword("alice", "old", " "))
        );
    }

    @Test
    void updateSellerRatingRejectsOutOfRangeRatingBeforeDatabaseLookup() {
        UserService service = new UserService();

        assertAll(
                () -> assertFalse(service.updateSellerRating(1, 0.9)),
                () -> assertFalse(service.updateSellerRating(1, 5.1))
        );
    }
}
