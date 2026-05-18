package org.example.exception.auction;

import org.example.exception.AuctionSystemException;

public class AuctionNotFoundException extends AuctionSystemException {
    public AuctionNotFoundException(String message) {
        super(message);
    }
}
