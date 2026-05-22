package org.example.exception.item;

import org.example.exception.AuctionSystemException;

public class InvalidItemNameException extends AuctionSystemException {
    public InvalidItemNameException(String message){
        super(message);
    }
}
