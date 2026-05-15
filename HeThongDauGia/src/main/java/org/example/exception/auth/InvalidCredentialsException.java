package org.example.exception.auth;

import org.example.exception.AuctionSystemException;

public class InvalidCredentialsException extends AuctionSystemException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
