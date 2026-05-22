package org.example.exception.database;

import org.example.exception.AuctionSystemException;

public class DatabaseException extends AuctionSystemException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}