package org.example.exception.item;

import org.example.exception.AuctionSystemException;

public class ItemNotFoundException extends AuctionSystemException {
    public ItemNotFoundException(String message){
        super(message);
    }
}
