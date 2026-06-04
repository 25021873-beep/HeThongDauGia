package org.example.exception.auth;

import org.example.exception.AuctionSystemException;

public class AccountLockedException extends AuctionSystemException {
    public AccountLockedException(String message) {
        super(message);
    }
}
