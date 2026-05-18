package org.example.exception.item;

import org.example.exception.AuctionSystemException;

public class UnauthorizedAccessException extends AuctionSystemException {
    public UnauthorizedAccessException(String message){
        super(message);
    }
}
