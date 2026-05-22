package org.example.exception.auth;

import org.example.exception.AuctionSystemException;

public class DuplicateUsernameException extends AuctionSystemException {
    public DuplicateUsernameException(String message) {
        super(message);
    }
}
