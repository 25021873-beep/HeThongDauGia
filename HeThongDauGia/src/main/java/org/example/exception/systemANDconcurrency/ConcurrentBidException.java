package org.example.exception.systemANDconcurrency;

import org.example.entity.Auction;
import org.example.exception.AuctionSystemException;

public class ConcurrentBidException extends AuctionSystemException {
    public ConcurrentBidException(String message){
        super(message);
    }
}
