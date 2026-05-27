package org.example.exception.bid;

import org.example.exception.AuctionSystemException;

public class InsufficientBalanceException extends AuctionSystemException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}