package org.example.exception.auth;

import org.example.exception.AuctionSystemException;

public class NotLoggedInException extends AuctionSystemException {
    public NotLoggedInException(String message){
        super(message);
    }
}
