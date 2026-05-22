package org.example.exception.systemANDconcurrency;

import org.example.exception.AuctionSystemException;

public class InvalidRequestException extends AuctionSystemException {
    public InvalidRequestException(String message){
        super(message);
    }
}
