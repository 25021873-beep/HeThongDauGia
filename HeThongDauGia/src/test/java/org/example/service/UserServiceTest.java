package org.example.service;

import org.example.exception.AuctionSystemException;
import org.example.exception.auth.SellersRatingException;
import org.example.exception.balance.InvalidTopUpAmountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void topUpBalanceRejectsNullAndNonPositiveAmountsBeforeDatabaseLookup() {
        UserService service = UserService.getInstance();

        assertAll(
                () -> assertThrows(InvalidTopUpAmountException.class, () -> service.topUpBalance(1, null)),
                () -> assertThrows(InvalidTopUpAmountException.class, () -> service.topUpBalance(1, BigDecimal.ZERO)),
                () -> assertThrows(InvalidTopUpAmountException.class, () -> service.topUpBalance(1, new BigDecimal("-1")))
        );
    }

    @Test
    void changePasswordRejectsBlankNewPasswordBeforeDatabaseLookup() {
        UserService service = UserService.getInstance();

        assertAll(
                () -> assertThrows(AuctionSystemException.class,
                        () -> service.changePassword("alice", "old", null)),
                () -> assertThrows(AuctionSystemException.class,
                        () -> service.changePassword("alice", "old", " "))
        );
    }

    @Test
    void updateSellerRatingRejectsOutOfRangeRatingBeforeDatabaseLookup() {
        UserService service = UserService.getInstance();

        assertAll(
                () -> assertThrows(SellersRatingException.class, () -> service.updateSellerRating(1, 0.9)),
                () -> assertThrows(SellersRatingException.class, () -> service.updateSellerRating(1, 5.1))
        );
    }
}
