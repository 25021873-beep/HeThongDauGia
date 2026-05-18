package org.example.exception.bid;

import org.example.exception.AuctionSystemException;

public class SelfBidException extends AuctionSystemException {
    public SelfBidException(String message) {
        super(message);
    }
}
