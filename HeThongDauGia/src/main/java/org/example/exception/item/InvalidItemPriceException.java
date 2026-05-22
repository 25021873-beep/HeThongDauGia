package org.example.exception.item;

import org.example.exception.AuctionSystemException;

public class InvalidItemPriceException extends AuctionSystemException {
    public InvalidItemPriceException(String message){
        super(message);
    }
}
