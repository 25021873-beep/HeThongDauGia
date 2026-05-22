package org.example.exception.auth;

import org.example.exception.AuctionSystemException;

public class UserNotFoundException extends AuctionSystemException {
    public UserNotFoundException(String message) {
        super(message);
    }
}