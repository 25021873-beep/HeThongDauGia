package org.example.exception.auction;

import org.example.exception.AuctionSystemException;

public class InvalidAuctionTimeException extends AuctionSystemException {
    public InvalidAuctionTimeException(String message){
        super(message);
    }
}
