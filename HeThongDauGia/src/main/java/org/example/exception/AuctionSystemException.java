package org.example.exception;

public class AuctionSystemException extends RuntimeException {

    public AuctionSystemException(String message) {
        super(message);
    }

    public AuctionSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}