package org.example.exception.auth;

import org.example.exception.AuctionSystemException;

public class SellersRatingException extends AuctionSystemException {
    public SellersRatingException(String message){
        super(message);
    }
}
