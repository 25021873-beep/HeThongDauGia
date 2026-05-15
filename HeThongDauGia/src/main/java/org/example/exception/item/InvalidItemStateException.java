package org.example.exception.item;

import org.example.exception.AuctionSystemException;

public class InvalidItemStateException extends AuctionSystemException {
    public InvalidItemStateException(String message) {
        super(message);
    }
}
