package org.example.exception.auction;

import org.example.exception.AuctionSystemException;

public class AuctionClosedException extends AuctionSystemException {
    public AuctionClosedException(String message) {
        super(message);
    }
}
